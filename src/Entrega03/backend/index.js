require("dotenv").config();
const express = require("express");
const cors = require("cors");
const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const { encrypt, decrypt } = require("./crypto");
const { db, ensureSafeScoreEntry, ensureUserAchievements, initializeUserAchievements } = require("./db");

const app = express();

// 1. JSON parser with raw-body capture for decryption
app.use(express.json({ verify: (req, res, buf) => { req.rawBody = buf.toString(); } }));

// 2. Enable CORS
app.use(cors());

// 3. Decryption middleware (unwrap incoming encrypted payloads)
app.use((req, res, next) => {
  if (req.rawBody && req.is('application/json')) {
    try {
      const wrapper = JSON.parse(req.rawBody);
      req.body = wrapper.data ? JSON.parse(decrypt(wrapper.data)) : wrapper;
    } catch {
      return res.status(400).json({ message: "Invalid payload" });
    }
  }
  next();
});

// 4. Encryption middleware (wrap outgoing JSON responses)
const skipEncryption = req => req.headers['x-raw-response'] === 'true';
app.use((req, res, next) => {
  if (skipEncryption(req)) return next();
  const originalJson = res.json.bind(res);
  res.json = obj => {
    try {
      const ciphertext = encrypt(JSON.stringify(obj));
      return originalJson({ data: ciphertext });
    } catch {
      return res.status(500).json({ message: "Encryption error" });
    }
  };
  next();
});

// 5. JWT authentication middleware
const auth = (req, res, next) => {
  const header = req.headers.authorization;
  if (!header || !header.startsWith('Bearer ')) {
    return res.status(401).json({ message: "Unauthorized" });
  }
  const token = header.split(' ')[1];
  jwt.verify(token, process.env.JWT_SECRET, (err, user) => {
    if (err) return res.status(403).json({ message: "Forbidden" });
    req.user = user;
    next();
  });
};

// --- Routes ---

// Signup
app.post("/api/auth/signup", async (req, res) => {
  const { username, email, password, type, phone, gender } = req.body;
  if (!username || !email || !password || !type) {
    return res.status(400).json({ message: "Missing required fields" });
  }
  const hashed = await bcrypt.hash(password, 10);
  db.get("SELECT user_id FROM User WHERE email = ?", [email], (err, row) => {
    if (err) return res.status(500).json({ message: "DB error" });
    if (row) return res.status(409).json({ message: "Email exists" });
    db.run(
      "INSERT INTO User (username,email,password,type,phone,gender) VALUES (?,?,?,?,?,?)",
      [username, email, hashed, type, phone || null, gender || null],
      function(err) {
        if (err) return res.status(500).json({ message: "Insert error" });
        const id = this.lastID;
        
        // Promise chain to initialize user data
        ensureSafeScoreEntry(id)
          .then(() => {
            // Inicializar conquistas para o novo usuário
            initializeUserAchievements(id)
              .then(() => {
                res.status(201).json({ 
                  success: true, 
                  message: "Registration successful", 
                  userId: id 
                });
              })
              .catch(() => {
                res.status(201).json({ 
                  success: true, 
                  message: "Registration successful but achievements init failed", 
                  userId: id 
                });
              });
          })
          .catch(() => {
            res.status(201).json({ 
              success: true, 
              message: "Registration successful but data init failed", 
              userId: id, 
              warning: "SafeScore init failed" 
            });
          });
      }
    );
  });
});

// Login
app.post("/api/auth/login", (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ message: "Missing credentials" });
  }
  db.get(
    "SELECT user_id,username,email,password,type FROM User WHERE email = ?",
    [email],
    async (err, user) => {
      if (err) return res.status(500).json({ message: "DB error" });
      if (!user) return res.status(401).json({ message: "Invalid credentials" });
      const match = await bcrypt.compare(password, user.password);
      if (!match) return res.status(401).json({ message: "Invalid credentials" });
      const token = jwt.sign(
        { user_id: user.user_id, username: user.username, email: user.email, type: user.type },
        process.env.JWT_SECRET,
        { expiresIn: process.env.JWT_EXPIRES_IN || '1h' }
      );
      
      // Inicializar dados do usuário no login
      ensureSafeScoreEntry(user.user_id)
        .then(() => {
          // Garantir que as conquistas estejam inicializadas
          ensureUserAchievements(user.user_id)
            .finally(() => {
              res.json({ token });
            });
        })
        .catch(() => {
          res.status(500).json({ message: "Error initializing user data" });
        });
    }
  );
});

// Update SafeScore
app.post("/api/user/safescore/update", auth, (req, res) => {
  const userId = req.user.user_id;
  const { scoreChange } = req.body;
  if (typeof scoreChange !== 'number') {
    return res.status(400).json({ message: "Invalid scoreChange" });
  }
  ensureSafeScoreEntry(userId)
    .then(() => {
      db.get("SELECT score FROM SafeScore WHERE user_id = ?", [userId], (e, row) => {
        if (e || !row) return res.status(500).json({ message: "Score fetch error" });
        const newScore = Math.max(0, Math.min(100, row.score + scoreChange));
        db.run(
          "UPDATE SafeScore SET score = ?, last_updated = CURRENT_TIMESTAMP WHERE user_id = ?",
          [newScore, userId],
          err => {
            if (err) {
              return res.status(500).json({ message: "Score update error" });
            }
            
            // Atualizar conquistas com base na ação
            if (scoreChange > 0) {
              updateUserAchievementProgress(userId, 'safety', 1)
                .catch(error => console.error("Failed to update achievement:", error));
            }
            
            res.json({ newScore });
          }
        );
      });
    })
    .catch(() => res.status(500).json({ message: "SafeScore init error" }));
});

// Get SafeScore
app.get("/api/user/safescore", auth, (req, res) => {
  const userId = req.user.user_id;
  ensureSafeScoreEntry(userId)
    .then(() => {
      db.get("SELECT score FROM SafeScore WHERE user_id = ?", [userId], (err, row) => {
        if (err || !row) return res.status(500).json({ message: "Score fetch error" });
        res.json({ score: row.score });
      });
    })
    .catch(() => res.status(500).json({ message: "SafeScore init error" }));
});

// Get Profile
app.get("/api/profile", auth, (req, res) => {
  db.get(
    "SELECT user_id,username,email,phone,type,registration_date FROM User WHERE user_id = ?",
    [req.user.user_id],
    (err, row) => err ? res.status(500).json({ message: "DB error" }) : row ? res.json(row) : res.status(404).json({ message: "User not found" })
  );
});

// Get Gender
app.get("/api/user/gender", auth, (req, res) => {
  db.get("SELECT gender FROM User WHERE user_id = ?", [req.user.user_id], (err, row) => {
    if (err || !row) return res.status(500).json({ message: "Gender fetch error" });
    res.json({ gender: row.gender });
  });
});

// Get User Achievements
app.get("/api/user/achievements", auth, (req, res) => {
  const userId = req.user.user_id;
  
  ensureUserAchievements(userId)
    .then(() => {
      // Obter todo o progresso de conquistas do usuário
      db.all(
        `SELECT up.achievement_id, up.progress, up.completed, 
                a.title, a.description, a.points, a.icon_resource, a.type, a.target
         FROM UserAchievementProgress up
         JOIN Achievement a ON up.achievement_id = a.id
         WHERE up.user_id = ?`,
        [userId],
        (err, rows) => {
          if (err) {
            console.error("Erro ao buscar conquistas:", err);
            return res.status(500).json({ message: "Failed to fetch achievements" });
          }

          // Calcular pontos totais e nível do usuário
          let totalPoints = 0;
          const achievementProgress = [];

          rows.forEach(row => {
            if (row.completed) {
              totalPoints += row.points;
            }

            achievementProgress.push({
              achievementId: row.achievement_id,
              progress: row.progress,
              completed: row.completed === 1
            });
          });

          // Calcular nível do usuário (1 nível a cada 100 pontos, começando no nível 1)
          const userLevel = Math.max(1, Math.floor(totalPoints / 100) + 1);

          res.json({
            success: true,
            achievementProgress,
            totalPoints,
            userLevel
          });
        }
      );
    })
    .catch(error => {
      console.error("Erro de inicialização de conquistas:", error);
      res.status(500).json({ message: "Error initializing achievements" });
    });
});

// Update User Achievement Progress
app.post("/api/user/achievements/update", auth, (req, res) => {
  const userId = req.user.user_id;
  const { achievementProgress } = req.body;

  if (!Array.isArray(achievementProgress)) {
    return res.status(400).json({ message: "Invalid achievement progress data" });
  }

  ensureUserAchievements(userId)
    .then(() => {
      // Criar uma promise para cada atualização de conquista
      const updatePromises = achievementProgress.map(progress => {
        return new Promise((resolve, reject) => {
          if (!progress.achievementId || typeof progress.progress !== 'number') {
            return reject(new Error("Invalid achievement data"));
          }

          db.get(
            "SELECT target, points FROM Achievement WHERE id = ?",
            [progress.achievementId],
            (err, achievement) => {
              if (err || !achievement) {
                return reject(new Error("Achievement not found"));
              }

              const completed = progress.progress >= achievement.target ? 1 : 0;
              
              db.run(
                `UPDATE UserAchievementProgress 
                 SET progress = ?, completed = ?, last_updated = CURRENT_TIMESTAMP
                 WHERE user_id = ? AND achievement_id = ?`,
                [progress.progress, completed, userId, progress.achievementId],
                function(err) {
                  if (err) {
                    return reject(err);
                  }
                  resolve({
                    achievementId: progress.achievementId,
                    progress: progress.progress,
                    completed: completed === 1,
                    points: completed === 1 ? achievement.points : 0
                  });
                }
              );
            }
          );
        });
      });

      Promise.all(updatePromises)
        .then(results => {
          // Calcular pontos totais e nível do usuário
          let totalPoints = 0;
          
          // Primeiro obter pontos de conquistas existentes não sendo atualizadas
          const placeholders = achievementProgress.length > 0 
            ? achievementProgress.map(() => '?').join(',') 
            : '0';
          
          db.all(
            `SELECT a.points 
             FROM UserAchievementProgress up
             JOIN Achievement a ON up.achievement_id = a.id
             WHERE up.user_id = ? AND up.completed = 1 AND up.achievement_id NOT IN (${placeholders})`,
            [userId, ...achievementProgress.map(p => p.achievementId)],
            (err, rows) => {
              if (err) {
                return res.status(500).json({ message: "Error calculating points" });
              }
              
              // Adicionar pontos de conquistas completas existentes
              rows.forEach(row => {
                totalPoints += row.points;
              });
              
              // Adicionar pontos de conquistas recém-atualizadas
              results.forEach(result => {
                if (result.completed) {
                  totalPoints += result.points;
                }
              });
              
              // Calcular nível do usuário
              const userLevel = Math.max(1, Math.floor(totalPoints / 100) + 1);
              
              res.json({
                success: true,
                achievementProgress: results.map(r => ({
                  achievementId: r.achievementId,
                  progress: r.progress,
                  completed: r.completed
                })),
                totalPoints,
                userLevel
              });
            }
          );
        })
        .catch(error => {
          console.error("Erro ao atualizar conquistas:", error);
          res.status(500).json({ message: "Failed to update achievements" });
        });
    })
    .catch(error => {
      console.error("Erro de inicialização de conquistas:", error);
      res.status(500).json({ message: "Error initializing achievements" });
    });
});

// Track Achievement Progress by Type
app.post("/api/user/achievements/track", auth, (req, res) => {
  const userId = req.user.user_id;
  const { type, increment = 1 } = req.body;
  
  if (!type || typeof type !== 'string') {
    return res.status(400).json({ message: "Invalid achievement type" });
  }
  
  updateUserAchievementProgress(userId, type, increment)
    .then(result => {
      res.json({
        success: true,
        ...result
      });
    })
    .catch(error => {
      console.error("Erro ao rastrear progresso de conquista:", error);
      res.status(500).json({ message: "Failed to track achievement progress" });
    });
});

// Atualizar progresso de conquista por tipo (Função auxiliar)
function updateUserAchievementProgress(userId, type, increment) {
  return new Promise((resolve, reject) => {
    ensureUserAchievements(userId)
      .then(() => {
        // Obter todas as conquistas do tipo especificado
        db.all(
          "SELECT id, target, points FROM Achievement WHERE type = ?",
          [type],
          (err, achievements) => {
            if (err) {
              console.error("Erro ao buscar conquistas por tipo:", err);
              return reject(err);
            }
            
            if (achievements.length === 0) {
              return resolve({ updatedAchievements: [] });
            }
            
            const achievementIds = achievements.map(a => a.id);
            const placeholders = achievementIds.map(() => '?').join(',');
            
            // Obter progresso atual para essas conquistas
            db.all(
              `SELECT achievement_id, progress, completed 
               FROM UserAchievementProgress 
               WHERE user_id = ? AND achievement_id IN (${placeholders})`,
              [userId, ...achievementIds],
              (err, progressRecords) => {
                if (err) {
                  console.error("Erro ao buscar progresso de conquistas:", err);
                  return reject(err);
                }
                
                // Preparar atualizações para cada conquista
                const updates = [];
                const updatedAchievements = [];
                
                achievements.forEach(achievement => {
                  const progressRecord = progressRecords.find(p => p.achievement_id === achievement.id);
                  
                  if (progressRecord && progressRecord.completed !== 1) {
                    const newProgress = progressRecord.progress + increment;
                    const completed = newProgress >= achievement.target ? 1 : 0;
                    
                    updates.push({
                      achievementId: achievement.id,
                      progress: newProgress,
                      completed,
                      target: achievement.target,
                      points: achievement.points
                    });
                    
                    updatedAchievements.push({
                      achievementId: achievement.id,
                      progress: newProgress,
                      completed: completed === 1
                    });
                  }
                });
                
                if (updates.length === 0) {
                  return resolve({ updatedAchievements: [] });
                }
                
                // Executar todas as atualizações
                const updatePromises = updates.map(update => {
                  return new Promise((resolve, reject) => {
                    db.run(
                      `UPDATE UserAchievementProgress 
                       SET progress = ?, completed = ?, last_updated = CURRENT_TIMESTAMP
                       WHERE user_id = ? AND achievement_id = ?`,
                      [update.progress, update.completed, userId, update.achievementId],
                      function(err) {
                        if (err) {
                          return reject(err);
                        }
                        resolve();
                      }
                    );
                  });
                });
                
                Promise.all(updatePromises)
                  .then(() => {
                    // Verificar conquistas recém-completadas
                    const newlyCompleted = updates.filter(u => u.completed === 1);
                    
                    if (newlyCompleted.length > 0) {
                      // Adicionar pontos ao SafeScore para conquistas recém-completadas
                      const totalNewPoints = newlyCompleted.reduce((sum, a) => sum + a.points, 0);
                      
                      if (totalNewPoints > 0) {
                        // Atualizar SafeScore
                        db.get("SELECT score FROM SafeScore WHERE user_id = ?", [userId], (err, row) => {
                          if (!err && row) {
                            const newScore = Math.min(100, row.score + totalNewPoints);
                            db.run(
                              "UPDATE SafeScore SET score = ?, last_updated = CURRENT_TIMESTAMP WHERE user_id = ?",
                              [newScore, userId],
                              err => {
                                if (err) {
                                  console.error("Erro ao atualizar SafeScore com pontos de conquistas:", err);
                                }
                              }
                            );
                          }
                        });
                      }
                    }
                    
                    resolve({ 
                      updatedAchievements,
                      newlyCompleted: newlyCompleted.map(a => ({
                        achievementId: a.achievementId,
                        points: a.points
                      }))
                    });
                  })
                  .catch(err => {
                    console.error("Erro ao atualizar progresso de conquista:", err);
                    reject(err);
                  });
              }
            );
          }
        );
      })
      .catch(reject);
  });
}

// Health check
app.get("/", (req, res) => res.sendStatus(200));

// Global error handler
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ message: "Internal Server Error" });
});

// Start server and graceful shutdown
const server = app.listen(process.env.PORT || 3001, () => {
  console.log(`Servidor rodando na porta ${process.env.PORT || 3001}`);
});

const shutdown = () => server.close(() => db.close());
process.on('SIGTERM', shutdown);
process.on('SIGINT', shutdown);