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
<<<<<<< HEAD
  ssl: {
    rejectUnauthorized: false  // Try this for testing, but use proper SSL in production
  }
=======
  ssl: { rejectUnauthorized: true },
  connectionLimit: 20,
  waitForConnections: true,
>>>>>>> 9fffdf525ba8ecd2ea3103480e8db820b5002ddd
});

// Verifica conexão do pool
db.getConnection((err, connection) => {
  if (err) {
    console.error("Erro ao obter conexão do pool:", err);
  } else {
    console.log("Conectado ao banco de dados via pool!");
    connection.release();
  }
});

// Criação da tabela User
const createUsersTable = `CREATE TABLE IF NOT EXISTS User (
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

// Criação da tabela SafeScore
const createSafeScoreTable = `CREATE TABLE IF NOT EXISTS SafeScore (
  safescore_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  score DECIMAL(3,2) NOT NULL CHECK (score BETWEEN 0 AND 5),
  last_updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES User(user_id)
)`;

db.query(createSafeScoreTable, (err) => {
  if (err) console.error("Erro ao criar tabela SafeScore:", err);
  else console.log("Tabela SafeScore pronta");
});

// Middleware de autenticação JWT
const authMiddleware = (req, res, next) => {
  const token = req.headers.authorization;
  if (!token) {
    return res.status(401).json({ message: "Token não fornecido" });
  }
  jwt.verify(token.split(" ")[1], process.env.JWT_SECRET, (err, user) => {
    if (err) {
      return res.status(403).json({ message: "Token inválido" });
    }
    req.user = user;
    next();
  });
};

// Rota de cadastro
app.post("/api/auth/signup", async (req, res) => {
  const { username, email, password, type, phone } = req.body;
  if (!username || !email || !password || !type) {
    return res
      .status(400)
      .json({ message: "Todos os campos obrigatórios devem ser preenchidos" });
  }

  try {
    const hashedPassword = await bcrypt.hash(password, 10);
    db.query(
      "INSERT INTO User (username, email, password, type, phone) VALUES (?, ?, ?, ?, ?)",
      [username, email, hashedPassword, type, phone || null],
      (error) => {
        if (error) {
          return res
            .status(500)
            .json({
              message: "Erro ao registrar usuário",
              error: error.message,
            });
        }
        res.status(201).json({ message: "Usuário cadastrado com sucesso!" });
      }
    );
  } catch (error) {
    res
      .status(500)
      .json({ message: "Erro ao processar a senha", error: error.message });
  }
});

// Rota de login
app.post("/api/auth/login", (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ message: "Email e senha são obrigatórios" });
  }

  db.query(
    "SELECT * FROM User WHERE email = ?",
    [email],
    async (error, results) => {
      if (error || results.length === 0) {
        return res.status(400).json({ message: "Credenciais inválidas" });
      }

      const user = results[0];
      const match = await bcrypt.compare(password, user.password);
      if (match) {
        const token = jwt.sign(
          { user_id: user.user_id, username: user.username, type: user.type },
          process.env.JWT_SECRET,
          { expiresIn: "1h" }
        );
        return res.status(200).json({
          message: "Login bem-sucedido",
          token,
          username: user.username,
          email: user.email,
          phone: user.phone,
          type: user.type,
        });
      } else {
        return res.status(401).json({ message: "Senha incorreta" });
      }
    }
  );
});

// Rota protegida para inserir ou atualizar SafeScore
app.post("/api/safescore", authMiddleware, (req, res) => {
  const { score } = req.body;
  const userId = req.user.user_id;

  if (score === undefined || score < 0 || score > 5) {
    return res.status(400).json({ message: "Nota deve ser entre 0 e 5" });
  }

  const checkQuery = "SELECT * FROM SafeScore WHERE user_id = ?";
  db.query(checkQuery, [userId], (err, results) => {
    if (err)
      return res
        .status(500)
        .json({ message: "Erro ao verificar nota", error: err.message });

    if (results.length > 0) {
      const updateQuery = "UPDATE SafeScore SET score = ? WHERE user_id = ?";
      db.query(updateQuery, [score, userId], (err) => {
        if (err)
          return res
            .status(500)
            .json({ message: "Erro ao atualizar nota", error: err.message });
        res.status(200).json({ message: "Nota atualizada com sucesso" });
      });
    } else {
      const insertQuery =
        "INSERT INTO SafeScore (user_id, score) VALUES (?, ?)";
      db.query(insertQuery, [userId, score], (err) => {
        if (err)
          return res
            .status(500)
            .json({ message: "Erro ao salvar nota", error: err.message });
        res.status(201).json({ message: "Nota cadastrada com sucesso" });
      });
    }
  });
});

// Rota para exibir média do SafeScore do usuário autenticado
app.get("/api/safescore/media", authMiddleware, (req, res) => {
  const userId = req.user.user_id;

  const query = "SELECT AVG(score) AS media FROM SafeScore WHERE user_id = ?";
  db.query(query, [userId], (err, results) => {
    if (err) {
      return res
        .status(500)
        .json({ message: "Erro ao calcular a média", error: err.message });
    }

    const media = results[0].media;
    if (media === null) {
      return res
        .status(404)
        .json({ message: "Nenhuma nota encontrada para este usuário" });
    }

    res
      .status(200)
      .json({ user_id: userId, media: parseFloat(media.toFixed(2)) });
  });
});

// Rota protegida para retornar dados do usuário logado
app.get("/api/profile", authMiddleware, (req, res) => {
  const userId = req.user.user_id;

  const query =
    "SELECT username, email, phone, type FROM User WHERE user_id = ?";
  db.query(query, [userId], (err, results) => {
    if (err || results.length === 0) {
      return res.status(404).json({ message: "Usuário não encontrado" });
    }

    res.status(200).json(results[0]);
  });
});

// Porta do servidor
const PORT = process.env.PORT || 3001;
app.listen(PORT, () => {
  console.log(`Servidor rodando na porta ${PORT}`);
});
