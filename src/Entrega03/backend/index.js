require("dotenv").config();
const express = require("express");
const cors = require("cors");
const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const { encrypt, decrypt } = require("./crypto");
const { db, ensureSafeScoreEntry } = require("./db");

const app = express();

app.use(
  express.json({
    verify: (req, res, buf) => {
      req.rawBody = buf.toString();
    },
  })
);
app.use(cors());

// Middleware global para decifragem e cifragem
app.use((req, res, next) => {
  if (req.rawBody && req.headers['content-type'] === 'application/json') {
      try {
          const wrapper = JSON.parse(req.rawBody);
          // Decifra se o payload estiver encapsulado em 'data'
          req.body = wrapper.data ? JSON.parse(decrypt(wrapper.data)) : wrapper;
      } catch (e) {
          console.error("Falha ao decifrar/parsear payload:", e);
          if (!res.headersSent) {
              return res.status(400).send(JSON.stringify({ message: "Payload inválido ou não criptografado corretamente" }));
          }
          console.error("Headers já enviados, não foi possível retornar erro 400.");
      }
  }

  const originalJson = res.json.bind(res);
  const originalSend = res.send.bind(res);

  // Intercepta res.json para cifrar a resposta
  res.json = (obj) => {
    try {
        const ciphertext = encrypt(JSON.stringify(obj));
        return originalJson({ data: ciphertext });
    } catch (encErr) {
        console.error("Erro ao criptografar resposta:", encErr);
        res.status(500);
        return originalSend(JSON.stringify({ message: "Erro interno ao processar resposta" }));
    }
  };

  next();
});

// Middleware de autenticação JWT
const authMiddleware = (req, res, next) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return res.status(401).send(JSON.stringify({ message: "Token não fornecido ou mal formatado" }));
  }
  const token = authHeader.split(" ")[1];
  jwt.verify(token, process.env.JWT_SECRET, (err, user) => {
    if (err) {
        return res.status(403).send(JSON.stringify({ message: "Token inválido ou expirado" }));
    }
    req.user = user;
    next();
  });
};

// --- Rotas ---

// Signup
app.post("/api/auth/signup", async (req, res) => {
  const { username, email, password, type, phone } = req.body;

  if (!username || !email || !password || !type) {
    return res.status(400).json({ message: "Campos obrigatórios: username, email, password, type" });
  }
  if (!['driver', 'passenger'].includes(type)) {
      return res.status(400).json({ message: "Tipo de usuário inválido. Use 'driver' ou 'passenger'." });
  }

  try {
     db.get("SELECT user_id FROM User WHERE email = ?", [email], async (err, row) => {
        if (err) {
            console.error("Erro ao verificar email existente:", err.message);
            return res.status(500).json({ message: "Erro interno ao verificar email" });
        }
        if (row) {
            return res.status(409).json({ message: "Email já cadastrado" });
        }

        const hashedPassword = await bcrypt.hash(password, 10);

        db.run(
          "INSERT INTO User (username, email, password, type, phone) VALUES (?, ?, ?, ?, ?)",
          [username, email, hashedPassword, type, phone || null],
          async function(insertErr) {
            if (insertErr) {
              console.error("Erro ao inserir usuário:", insertErr.message);
              if (insertErr.code === 'SQLITE_CONSTRAINT') {
                  return res.status(409).json({ message: "Email já cadastrado (conflito)" });
              }
              return res.status(500).json({ message: "Erro ao realizar cadastro", error: insertErr.message });
            }
            const newUserId = this.lastID;
            console.log(`Usuário ${username} (ID: ${newUserId}) cadastrado com sucesso.`);

            try {
                await ensureSafeScoreEntry(newUserId);
                res.status(201).json({ message: "Usuário cadastrado com sucesso!", userId: newUserId });
            } catch (safeScoreErr) {
                console.error(`Falha ao criar SafeScore para novo usuário ${newUserId}:`, safeScoreErr.message);
                res.status(201).json({ message: "Usuário cadastrado, mas houve um problema ao inicializar o SafeScore.", userId: newUserId });
            }
          }
        );
     });
  } catch (e) {
    console.error("Erro inesperado no signup:", e);
    res.status(500).json({ message: "Erro interno no servidor", error: e.message });
  }
});


// Login
app.post("/api/auth/login", (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ message: "Email e senha são obrigatórios" });
  }

  db.get("SELECT user_id, username, email, password, type FROM User WHERE email = ?", [email], async (err, user) => {
    if (err) {
      console.error("Erro ao buscar usuário no login:", err.message);
      return res.status(500).json({ message: "Erro interno ao tentar logar" });
    }
    if (!user) {
      return res.status(401).json({ message: "Credenciais inválidas" });
    }

    try {
      const passwordMatch = await bcrypt.compare(password, user.password);
      if (!passwordMatch) {
        return res.status(401).json({ message: "Credenciais inválidas" });
      }

      const tokenPayload = {
        user_id: user.user_id,
        username: user.username,
        email: user.email,
        type: user.type
      };
      const token = jwt.sign(
        tokenPayload,
        process.env.JWT_SECRET,
        { expiresIn: process.env.JWT_EXPIRES_IN || "1h" }
      );

      console.log(`Login bem-sucedido para ${user.email}`);
      try {
          await ensureSafeScoreEntry(user.user_id);
      } catch (safeScoreErr) {
          console.error(`Falha ao garantir SafeScore no login para user ${user.user_id}:`, safeScoreErr.message);
      }
      res.json({ message: "Login bem-sucedido", token: token });

    } catch (compareError) {
      console.error("Erro ao comparar senhas:", compareError);
      return res.status(500).json({ message: "Erro interno durante a autenticação" });
    }
  });
});


// Update SafeScore
app.post("/api/user/safescore/update", authMiddleware, async (req, res) => {
    const userId = req.user.user_id;
    const { scoreChange } = req.body;

    if (typeof scoreChange !== 'number' || isNaN(scoreChange)) {
        return res.status(400).json({ message: "Valor 'scoreChange' inválido. Deve ser um número." });
    }

    try {
        await ensureSafeScoreEntry(userId);

        db.serialize(() => {
            db.run("BEGIN TRANSACTION");

            db.get("SELECT score FROM SafeScore WHERE user_id = ?", [userId], (err, row) => {
                if (err) {
                    console.error(`Erro ao buscar SafeScore para update (ID: ${userId}):`, err.message);
                    db.run("ROLLBACK");
                    return res.status(500).json({ message: "Erro ao buscar pontuação atual" });
                }

                if (!row) {
                    console.error(`SafeScore não encontrado para update (ID: ${userId}) mesmo após ensure.`);
                    db.run("ROLLBACK");
                     ensureSafeScoreEntry(userId).then(() => {
                         res.status(404).json({ message: "Registro de SafeScore não encontrado, tente novamente." });
                     }).catch(ensureErr => {
                         res.status(500).json({ message: "Erro crítico ao tentar recriar SafeScore." });
                     });
                    return;
                }

                let newScore = row.score + scoreChange;
                newScore = Math.max(0, Math.min(100, newScore));

                db.run(
                    "UPDATE SafeScore SET score = ?, last_updated = CURRENT_TIMESTAMP WHERE user_id = ?",
                    [newScore, userId],
                    (updateErr) => {
                        if (updateErr) {
                            console.error(`Erro ao atualizar SafeScore (ID: ${userId}):`, updateErr.message);
                            db.run("ROLLBACK");
                            return res.status(500).json({ message: "Erro ao atualizar pontuação" });
                        }

                        db.run("COMMIT", (commitErr) => {
                             if (commitErr) {
                                console.error(`Erro ao commitar transação SafeScore (ID: ${userId}):`, commitErr.message);
                                db.run("ROLLBACK");
                                return res.status(500).json({ message: "Erro ao finalizar atualização da pontuação" });
                             }
                             console.log(`SafeScore atualizado para user_id ${userId}. Novo score: ${newScore}`);
                             res.json({ message: "Pontuação atualizada com sucesso", newScore: newScore });
                        });
                    }
                );
            });
        });
    } catch (error) {
        console.error(`Erro geral ao atualizar SafeScore (ID: ${userId}):`, error.message);
        db.run("ROLLBACK", (rollbackErr) => {
            if (rollbackErr) console.error("Erro ao tentar reverter transação:", rollbackErr.message);
        });
        res.status(500).json({ message: "Erro interno no servidor ao atualizar pontuação", error: error.message });
    }
});


// Get SafeScore
app.get("/api/user/safescore", authMiddleware, async (req, res) => {
  const userId = req.user.user_id;
  try {
    await ensureSafeScoreEntry(userId);

    db.get("SELECT score FROM SafeScore WHERE user_id = ?", [userId], (err, row) => {
      if (err) {
        console.error(`Erro ao buscar SafeScore (ID: ${userId}):`, err.message);
        return res.status(500).json({ message: "Erro ao buscar pontuação" });
      }
      if (!row) {
         console.warn(`SafeScore não encontrado para user_id ${userId} mesmo após ensure.`);
         ensureSafeScoreEntry(userId).then(() => {
             db.get("SELECT score FROM SafeScore WHERE user_id = ?", [userId], (err2, row2) => {
                 if(err2 || !row2) {
                     console.error(`Erro crítico ao buscar SafeScore após recriação (ID: ${userId}):`, err2?.message);
                     return res.status(500).json({ message: "Erro persistente ao buscar pontuação." });
                 }
                 res.json({ safescore: row2.score });
             });
         }).catch(ensureErr => {
             console.error(`Erro ao tentar recriar SafeScore na busca (ID: ${userId}):`, ensureErr.message);
             res.status(500).json({ message: "Erro ao inicializar pontuação." });
         });
         return;
      }
      console.log(`SafeScore ${row.score} retornado para user_id ${userId}`);
      res.json({ safescore: row.score });
    });
  } catch (e) {
    console.error(`Erro geral ao buscar SafeScore (ID: ${userId}):`, e.message);
    res.status(500).json({ message: "Erro interno no servidor ao buscar pontuação", error: e.message });
  }
});


// Get Profile
app.get("/api/profile", authMiddleware, (req, res) => {
  const userId = req.user.user_id;
  db.get("SELECT user_id, username, email, phone, type, registration_date FROM User WHERE user_id = ?", [userId], (err, row) => {
    if (err) {
      console.error(`Erro ao buscar perfil (ID: ${userId}):`, err.message);
      return res.status(500).json({ message: "Erro interno ao buscar dados do perfil" });
    }
    if (!row) {
      console.warn(`Usuário do token (ID: ${userId}) não encontrado no banco.`);
      return res.status(404).send(JSON.stringify({ message: "Usuário não encontrado" }));
    }
    console.log(`Perfil retornado para user_id ${userId}`);
    res.json(row);
  });
});

// Rota raiz
app.get("/", (req, res) => {
    res.status(200).send("Servidor SafeStart está online.");
});


// Inicia o servidor
const PORT = process.env.PORT || 3001;
const server = app.listen(PORT, () => { // Guarda a instância do servidor
  console.log(`Servidor rodando na porta ${PORT}`);
});

// Tratamento de Encerramento Gracioso
const gracefulShutdown = (signal) => {
  console.log(`${signal} signal received: closing HTTP server`);
  server.close(() => {
    console.log('HTTP server closed');
    db.close((err) => {
      if (err) {
        console.error('Error closing SQLite database:', err.message);
        process.exit(1); // Sai com erro se não conseguir fechar o DB
      } else {
        console.log('SQLite database connection closed.');
        process.exit(0); // Sai com sucesso
      }
    });
  });
};

process.on('SIGTERM', () => gracefulShutdown('SIGTERM'));
process.on('SIGINT', () => gracefulShutdown('SIGINT'));