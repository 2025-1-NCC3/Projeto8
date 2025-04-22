require("dotenv").config();
const express = require("express");
const mysql = require("mysql2");
const cors = require("cors");
const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");

const app = express();
app.use(cors());
app.use(express.json());

// Pool de conexão com o banco de dados
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

// Criação da tabela User
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

// Criação da tabela SafeScore para histórico
const createSafeScoreTable = `
CREATE TABLE IF NOT EXISTS SafeScore (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    score INT DEFAULT 100,
    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(user_id)
);`;

db.query(createSafeScoreTable, (err) => {
  if (err) console.error("Erro ao criar tabela SafeScore:", err);
  else console.log("Tabela SafeScore pronta");
});

// Middleware de autenticação JWT
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

// Rota de cadastro
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

// Rota de login
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

// Garante existência de registro no histórico
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

// Rota: registrar + acumular SafeScore
app.post("/api/user/safescore/update", authMiddleware, async (req, res) => {
  const userId = req.user.user_id;
  const { scoreChange } = req.body;

  if (!Number.isInteger(scoreChange) || scoreChange < 0 || scoreChange > 100) {
    return res
      .status(400)
      .json({ message: "scoreChange deve ser inteiro 0-100" });
  }

  try {
    await checkSafeScoreEntry(userId);
    db.query(
      "SELECT score FROM SafeScore WHERE user_id = ?",
      [userId],
      (err, rows) => {
        if (err || rows.length === 0)
          return res.status(500).json({
            success: false,
            message: "Erro ao buscar histórico",
          });

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

            db.query(
              "UPDATE SafeScore SET score = ? WHERE user_id = ?",
              [newScore, userId],
              (e2) => {
                if (e2)
                  return res.status(500).json({
                    success: false,
                    message: "Erro ao atualizar User",
                  });

                res.json({
                  success: true,
                  message: "SafeScore registrado",
                  newScore: newScore,
                });
              }
            );
          }
        );
      }
    );
  } catch (error) {
    res.status(500).json({
      success: false,
      message: "Erro interno",
      error: error.message,
    });
  }
});

// Rota: obter SafeScore
app.get("/api/user/safescore", authMiddleware, async (req, res) => {
  const userId = req.user.user_id;
  try {
    await checkSafeScoreEntry(userId);
    db.query(
      "SELECT score FROM `SafeScore` WHERE user_id = ?",
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

// Rota de perfil do usuário
app.get("/api/profile", authMiddleware, (req, res) => {
  db.query(
    "SELECT username, email, phone, type FROM `User` WHERE user_id = ?",
    [req.user.user_id],
    (err, results) => {
      if (err) {
        console.error("Erro no SELECT /api/profile:", err);
        return res.status(500).json({ message: "Erro interno" });
      }
      if (results.length === 0) {
        console.warn(
          "Profile: usuário não encontrado para id",
          req.user.user_id
        );
        return res.status(404).json({ message: "Usuário não encontrado" });
      }
      res.json(results[0]);
    }
  );
});

// Inicia o servidor
const PORT = process.env.PORT || 3001;
app.listen(PORT, () => console.log(`Servidor rodando na porta ${PORT}`));
