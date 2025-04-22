// index.js
require("dotenv").config();
const express = require("express");
const mysql = require("mysql2");
const cors = require("cors");
const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const { encrypt, decrypt } = require("./crypto");

const app = express();

// 1) Parser JSON com capture do rawBody (para decifrar e logar)
app.use(
  express.json({
    verify: (req, res, buf) => {
      req.rawBody = buf.toString();
    },
  })
);
app.use(cors());

// 2) Middleware global de decifragem e cifragem + logs
app.use((req, res, next) => {
  // --- LOG do payload cru recebido ---
  console.log(">>> RAW RECEIVED PAYLOAD:", req.rawBody);

  // --- Decifra entrada (se existir campo data) ---
  if (req.rawBody) {
    try {
      const wrapper = JSON.parse(req.rawBody);
      if (wrapper.data) {
        const json = decrypt(wrapper.data);
        // Log do JSON após decifragem
        console.log(">>> DECRYPTED BODY:", json);
        req.body = JSON.parse(json);
      }
    } catch (e) {
      console.error("Falha ao decifrar payload:", e);
      return res
        .status(400)
        .json({ message: "Payload criptografado inválido" });
    }
  }

  // --- Intercepta res.json para cifrar saída ---
  const originalJson = res.json.bind(res);
  res.json = (obj) => {
    // Log do objeto antes de cifrar
    console.log(">>> RESPONSE OBJECT (plain):", obj);
    const ciphertext = encrypt(JSON.stringify(obj));
    // Log do ciphertext enviado
    console.log(">>> RESPONSE ENCRYPTED:", ciphertext);
    return originalJson({ data: ciphertext });
  };

  next();
});

// ----- Configuração do DB -----
const db = mysql.createPool({
  host: process.env.DB_HOST,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
  ssl: { rejectUnauthorized: true },
  connectionLimit: 10,
  waitForConnections: true,
});

db.getConnection((err, connection) => {
  if (err) console.error("Erro ao obter conexão do pool:", err);
  else {
    console.log("Conectado ao banco de dados via pool!");
    connection.release();
  }
});

// ----- Criação de tabelas -----
const createUsersTable = `
CREATE TABLE IF NOT EXISTS \`User\` (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL,
  email VARCHAR(150) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  type ENUM('driver', 'passenger') NOT NULL,
  phone VARCHAR(15),
  registration_date DATETIME DEFAULT CURRENT_TIMESTAMP
)`;
db.query(createUsersTable, (err) => {
  if (err) console.error("Erro ao criar tabela User:", err);
  else console.log("Tabela User pronta");
});

const createSafeScoreTable = `
CREATE TABLE IF NOT EXISTS SafeScore (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  score INT DEFAULT 0,
  last_updated DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE
)`;
db.query(createSafeScoreTable, (err) => {
  if (err) console.error("Erro ao criar tabela SafeScore:", err);
  else console.log("Tabela SafeScore pronta");
});

// ----- Middleware de autenticação JWT -----
const authMiddleware = (req, res, next) => {
  const auth = req.headers.authorization;
  if (!auth) return res.status(401).json({ message: "Token não fornecido" });
  const token = auth.split(" ")[1];
  jwt.verify(token, process.env.JWT_SECRET, (err, user) => {
    if (err) return res.status(403).json({ message: "Token inválido" });
    req.user = user;
    next();
  });
};

// ----- Função auxiliar SafeScore -----
const checkSafeScoreEntry = (user_id) =>
  new Promise((resolve, reject) => {
    db.query(
      "SELECT 1 FROM SafeScore WHERE user_id = ?",
      [user_id],
      (err, results) => {
        if (err) return reject(err);
        if (results.length === 0) {
          db.query(
            "INSERT INTO SafeScore (user_id, score) VALUES (?, 0)",
            [user_id],
            (e) => (e ? reject(e) : resolve())
          );
        } else resolve();
      }
    );
  });

// ----- Rotas -----

// Signup
app.post("/api/auth/signup", async (req, res) => {
  const { username, email, password, type, phone } = req.body;
  if (!username || !email || !password || !type) {
    return res.status(400).json({ message: "Campos obrigatórios faltando" });
  }
  try {
    const hashed = await bcrypt.hash(password, 10);
    db.query(
      "INSERT INTO `User` (username, email, password, type, phone) VALUES (?, ?, ?, ?, ?)",
      [username, email, hashed, type, phone || null],
      (err) => {
        if (err)
          return res
            .status(500)
            .json({ message: "Erro no cadastro", error: err.message });
        res.status(201).json({ message: "Usuário cadastrado" });
      }
    );
  } catch (e) {
    res.status(500).json({ message: "Erro interno", error: e.message });
  }
});

// Login
app.post("/api/auth/login", (req, res) => {
  const { email, password } = req.body;
  if (!email || !password)
    return res.status(400).json({ message: "Email e senha são obrigatórios" });

  db.query(
    "SELECT * FROM `User` WHERE email = ?",
    [email],
    async (err, results) => {
      if (err || results.length === 0)
        return res.status(400).json({ message: "Credenciais inválidas" });

      const user = results[0];
      const ok = await bcrypt.compare(password, user.password);
      if (!ok) return res.status(401).json({ message: "Senha incorreta" });

      const token = jwt.sign(
        { user_id: user.user_id, username: user.username, type: user.type },
        process.env.JWT_SECRET,
        { expiresIn: "1h" }
      );
      res.json({ message: "Login bem-sucedido", token });
    }
  );
});

// Update SafeScore
app.post("/api/user/safescore/update", authMiddleware, async (req, res) => {
  const userId = req.user.user_id;
  const { scoreChange } = req.body;
  try {
    await checkSafeScoreEntry(userId);
    db.query(
      "SELECT score FROM SafeScore WHERE user_id = ?",
      [userId],
      (err, rows) => {
        if (err || rows.length === 0)
          return res
            .status(500)
            .json({ success: false, message: "Erro ao buscar histórico" });

        const newScore = Math.min(rows[0].score + scoreChange, 100);
        db.query(
          "UPDATE SafeScore SET score = ?, last_updated = CURRENT_TIMESTAMP WHERE user_id = ?",
          [newScore, userId],
          (e) => {
            if (e)
              return res.status(500).json({
                success: false,
                message: "Erro ao atualizar histórico",
              });

            res.json({
              success: true,
              message: "SafeScore registrado",
              newScore,
            });
          }
        );
      }
    );
  } catch (error) {
    res
      .status(500)
      .json({ success: false, message: "Erro interno", error: error.message });
  }
});

// Get SafeScore
app.get("/api/user/safescore", authMiddleware, async (req, res) => {
  const userId = req.user.user_id;
  try {
    await checkSafeScoreEntry(userId);
    db.query(
      "SELECT score FROM SafeScore WHERE user_id = ?",
      [userId],
      (err, rows) => {
        if (err || rows.length === 0)
          return res.status(500).json({ message: "Erro ao buscar safescore" });
        res.json({ safescore: rows[0].score });
      }
    );
  } catch (e) {
    res.status(500).json({ message: "Erro interno", error: e.message });
  }
});

// Profile
app.get("/api/profile", authMiddleware, (req, res) => {
  db.query(
    "SELECT username, email, phone, type FROM `User` WHERE user_id = ?",
    [req.user.user_id],
    (err, results) => {
      if (err) return res.status(500).json({ message: "Erro interno" });
      if (results.length === 0)
        return res.status(404).json({ message: "Usuário não encontrado" });
      res.json(results[0]);
    }
  );
});

// Delete Account
app.delete("/api/user", authMiddleware, (req, res) => {
  const userId = req.user.user_id;
  db.query("DELETE FROM SafeScore WHERE user_id = ?", [userId], (err) => {
    if (err)
      return res
        .status(500)
        .json({ success: false, message: "Erro ao deletar histórico" });
    db.query(
      "DELETE FROM `User` WHERE user_id = ?",
      [userId],
      (error, result) => {
        if (error)
          return res
            .status(500)
            .json({ success: false, message: "Erro ao deletar usuário" });
        if (result.affectedRows === 0)
          return res
            .status(404)
            .json({ success: false, message: "Usuário não encontrado" });
        res.json({ success: true, message: "Conta deletada com sucesso" });
      }
    );
  });
});

// Inicia o servidor
const PORT = process.env.PORT || 3001;
app.listen(PORT, () => console.log(`Servidor rodando na porta ${PORT}`));
