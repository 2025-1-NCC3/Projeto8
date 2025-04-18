require("dotenv").config();
const express = require("express");
const mysql = require("mysql2");
const cors = require("cors");
const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");

const app = express();
app.use(cors());
app.use(express.json());

    // Conexão com o banco
const db = mysql.createConnection({
  host: process.env.DB_HOST,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
  ssl: {
    rejectUnauthorized: true,
  },
});

db.connect((err) => {
  if (err) {
    console.error("Erro ao conectar ao banco de dados:", err);
  } else {
    console.log("Conectado ao banco de dados!");
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

db.query(createUsersTable, (err, result) => {
  if (err) console.error("Erro ao criar tabela:", err);
  else console.log("Tabela de usuários pronta");
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
    return res.status(400).json({ message: "Todos os campos obrigatórios devem ser preenchidos" });
  }

  try {
    const hashedPassword = await bcrypt.hash(password, 10);
    db.query(
      "INSERT INTO User (username, email, password, type, phone) VALUES (?, ?, ?, ?, ?)",
      [username, email, hashedPassword, type, phone || null],
      (error, results) => {
        if (error) {
          return res.status(500).json({
            message: "Erro ao registrar usuário",
            error: error.message,
          });
        }
        res.status(201).json({ message: "Usuário cadastrado com sucesso!" });
      }
    );
  } catch (error) {
    res.status(500).json({ message: "Erro ao processar a senha", error: error.message });
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
        res.status(200).json({ message: "Login bem-sucedido", token });
      } else {
        res.status(401).json({ message: "Credenciais inválidas" });
      }
    }
  );
});

// Porta do servidor
const PORT = process.env.PORT || 3001;
app.listen(PORT, () => {
  console.log(`Servidor rodando na porta ${PORT}`);
});
