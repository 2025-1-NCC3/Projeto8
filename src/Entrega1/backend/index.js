// index.js
require('dotenv').config();
const express = require('express');
const mysql = require('mysql2');
const cors = require('cors');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');

const app = express();
app.use(cors());
app.use(express.json());

// Configuração do banco de dados
const db = mysql.createConnection({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,         // ssadmin
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
    ssl: {                             // Obrigatório no Azure
        rejectUnauthorized: true
    }
});


db.connect((err) => {
    if (err) {
        console.error("Erro ao conectar ao banco de dados:", err);
    } else {
        console.log("Conectado ao banco de dados!");
    }
});

// Criar tabela de usuários se não existir
const createUsersTable = `CREATE TABLE IF NOT EXISTS Usuario (
    id_user INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    type ENUM('Motorista', 'Passageiro') NOT NULL,
    telefone VARCHAR(15),
    data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP
)`;

db.query(createUsersTable, (err, result) => {
    if (err) console.error('Erro ao criar tabela:', err);
    else console.log('Tabela de usuários pronta');
});

// Middleware para verificar token JWT
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

// Rota de Cadastro
app.post('/api/auth/signup', async (req, res) => {
    const { username, email, password, type, telefone } = req.body;
    if (!username || !email || !password || !type) {
        return res.status(400).json({ message: "Todos os campos obrigatórios devem ser preenchidos" });
    }

    try {
        const hashedPassword = await bcrypt.hash(password, 10);
        db.query(
            'INSERT INTO Usuario (username, email, password, type, telefone) VALUES (?, ?, ?, ?, ?)',
            [username, email, hashedPassword, type, telefone || null],
            (error, results) => {
                if (error) {
                    return res.status(500).json({ message: "Erro ao registrar usuário", error: error.message });
                }
                res.status(201).json({ message: "Usuário cadastrado com sucesso!" });
            }
        );
    } catch (error) {
        res.status(500).json({ message: "Erro ao processar a senha", error: error.message });
    }
});

// Rota de Login
app.post('/api/auth/login', (req, res) => {
    const { email, password } = req.body;
    if (!email || !password) {
        return res.status(400).json({ message: "Email e senha são obrigatórios" });
    }

    db.query(
        'SELECT * FROM Usuario WHERE email = ?',
        [email],
        async (error, results) => {
            if (error || results.length === 0) {
                return res.status(400).json({ message: "Credenciais inválidas" });
            }

            const user = results[0];
            const match = await bcrypt.compare(password, user.password);
            if (match) {
                const token = jwt.sign(
                    { id_user: user.id_user, username: user.username, type: user.type },
                    process.env.JWT_SECRET,
                    { expiresIn: '1h' }
                );
                res.status(200).json({ message: "Login bem-sucedido", token });
            } else {
                res.status(401).json({ message: "Credenciais inválidas" });
            }
        }
    );
});

// Porta do servidor
const PORT = process.env.PORT;
app.listen(PORT, () => {
    console.log(`Servidor rodando na porta ${PORT}`);
});
