const sqlite3 = require("sqlite3").verbose();
const path = require("path");
const dbPath = path.join(__dirname, "safestart.db");

// Create and export the database instance
const db = new sqlite3.Database(dbPath, (err) => {
  if (err) {
    console.error("Erro ao conectar ao banco de dados SQLite:", err.message);
  } else {
    console.log("Conectado ao banco de dados SQLite:", dbPath);
    // Habilita chaves estrangeiras assim que a conexão é estabelecida
    db.run("PRAGMA foreign_keys = ON", (pragmaErr) => {
      if (pragmaErr) {
        console.error("Erro ao habilitar foreign keys:", pragmaErr.message);
      } else {
        console.log("Foreign keys habilitadas.");
        initializeDatabase(); // Chama a inicialização após habilitar FKs
      }
    });
  }
});

// Função para inicializar o banco de dados (criar tabelas)
function initializeDatabase() {
  const createUsersTable = `
  CREATE TABLE IF NOT EXISTS User (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    type TEXT CHECK(type IN ('driver', 'passenger')) NOT NULL,
    phone TEXT,
    gender TEXT CHECK(gender IN ('male', 'female', 'other', 'prefer_not_to_say')),
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  )`;

  const createSafeScoreTable = `
  CREATE TABLE IF NOT EXISTS SafeScore (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL UNIQUE,
    score INTEGER DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE
  )`;

  const createAchievementsTable = `
  CREATE TABLE IF NOT EXISTS Achievement (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    points INTEGER NOT NULL DEFAULT 0,
    icon_resource TEXT NOT NULL,
    type TEXT NOT NULL,
    target INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  )`;

  const createUserAchievementProgress = `
  CREATE TABLE IF NOT EXISTS UserAchievementProgress (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    achievement_id INTEGER NOT NULL,
    progress INTEGER NOT NULL DEFAULT 0,
    completed BOOLEAN NOT NULL DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE,
    FOREIGN KEY (achievement_id) REFERENCES Achievement(id) ON DELETE CASCADE,
    UNIQUE(user_id, achievement_id)
  )`;

  // Execução sequencial para garantir a criação correta das tabelas
  db.serialize(() => {
    console.log("Iniciando criação/verificação de tabelas...");

    db.run(createUsersTable, (err) => {
      if (err) {
        console.error("Erro ao criar/verificar tabela User:", err.message);
      } else {
        console.log("Tabela User pronta.");
      }
    });

    db.run(createSafeScoreTable, (err) => {
      if (err) {
        console.error("Erro ao criar/verificar tabela SafeScore:", err.message);
      } else {
        console.log("Tabela SafeScore pronta.");
      }
    });

    db.run(createAchievementsTable, (err) => {
      if (err) {
        console.error(
          "Erro ao criar/verificar tabela Achievement:",
          err.message
        );
      } else {
        console.log("Tabela Achievement pronta.");
        // Inicializar conquistas padrão após a criação da tabela
        initializeDefaultAchievements();
      }
    });

    db.run(createUserAchievementProgress, (err) => {
      if (err) {
        console.error(
          "Erro ao criar/verificar tabela UserAchievementProgress:",
          err.message
        );
      } else {
        console.log("Tabela UserAchievementProgress pronta.");
      }
    });

    console.log("Criação/verificação de tabelas concluída.");
  });
}

// Dados das conquistas padrão
const DEFAULT_ACHIEVEMENTS = [
  {
    title: "Viajante Iniciante",
    description: "Complete sua primeira viagem usando o app",
    points: 10,
    icon_resource: "R.drawable.ic_achievement_trip",
    type: "trip",
    target: 1,
  },
  {
    title: "Viajante Experiente",
    description: "Complete 10 viagens com segurança",
    points: 25,
    icon_resource: "R.drawable.ic_achievement_trip",
    type: "trip",
    target: 10,
  },
  {
    title: "Viajante Mestre",
    description: "Complete 50 viagens com segurança",
    points: 50,
    icon_resource: "R.drawable.ic_achievement_trip",
    type: "trip",
    target: 50,
  },
  {
    title: "Checklist de Segurança",
    description: "Complete o checklist de segurança antes da viagem",
    points: 5,
    icon_resource: "R.drawable.ic_achievement_checklist",
    type: "checklist",
    target: 1,
  },
  {
    title: "Sempre Alerta",
    description: "Complete o checklist de segurança 10 vezes",
    points: 20,
    icon_resource: "R.drawable.ic_achievement_checklist",
    type: "checklist",
    target: 10,
  },
  {
    title: "Compartilhador Seguro",
    description: "Compartilhe sua rota com um contato de segurança",
    points: 15,
    icon_resource: "R.drawable.ic_achievement_share",
    type: "share",
    target: 1,
  },
  {
    title: "Gravação Útil",
    description: "Autorize a gravação de áudio durante uma viagem",
    points: 15,
    icon_resource: "R.drawable.ic_achievement_audio",
    type: "audio",
    target: 1,
  },
  {
    title: "Feedback Construtivo",
    description: "Envie feedback após 5 viagens",
    points: 25,
    icon_resource: "R.drawable.ic_achievement_feedback",
    type: "feedback",
    target: 5,
  },
  {
    title: "Especialista em Segurança",
    description: "Acumule 100 pontos de SafeScore",
    points: 50,
    icon_resource: "R.drawable.ic_achievement_safety",
    type: "safety",
    target: 20,
  },
];

// Inicializar conquistas padrão no banco de dados
function initializeDefaultAchievements() {
  db.get("SELECT COUNT(*) as count FROM Achievement", [], (err, row) => {
    if (err) {
      console.error("Erro ao verificar conquistas:", err);
      return;
    }

    // Se não existirem conquistas, adicionar as padrões
    if (row.count === 0) {
      console.log("Inicializando conquistas padrão...");

      // Preparar statement para inserção de conquistas
      const stmt = db.prepare(
        "INSERT INTO Achievement (title, description, points, icon_resource, type, target) VALUES (?, ?, ?, ?, ?, ?)"
      );

      // Inserir cada conquista padrão
      DEFAULT_ACHIEVEMENTS.forEach((achievement) => {
        stmt.run(
          achievement.title,
          achievement.description,
          achievement.points,
          achievement.icon_resource,
          achievement.type,
          achievement.target,
          (err) => {
            if (err) {
              console.error(
                `Erro ao inserir conquista ${achievement.title}:`,
                err
              ); // Corrigido
            }
          }
        );
      });

      // Finalizar o statement preparado
      stmt.finalize((err) => {
        if (err) {
          console.error("Erro ao finalizar inserção de conquistas:", err);
        } else {
          console.log("Conquistas padrão inicializadas com sucesso");
        }
      });
    } else {
      console.log(`${row.count} conquistas já existem no banco de dados`); // Corrigido
    }
  });
}

// Inicializar conquistas para um novo usuário
function initializeUserAchievements(userId) {
  return new Promise((resolve, reject) => {
    db.get("SELECT COUNT(*) as count FROM Achievement", [], (err, row) => {
      if (err) {
        console.error("Erro ao verificar conquistas:", err);
        return reject(err);
      }

      if (row.count === 0) {
        return reject(new Error("Nenhuma conquista definida no sistema"));
      }

      // Obter todas as conquistas
      db.all("SELECT id FROM Achievement", [], (err, achievements) => {
        if (err) {
          console.error("Erro ao buscar conquistas:", err);
          return reject(err);
        }

        // Preparar statement para inserir progresso de conquistas do usuário
        const stmt = db.prepare(
          "INSERT OR IGNORE INTO UserAchievementProgress (user_id, achievement_id, progress, completed) VALUES (?, ?, 0, 0)"
        );

        // Inicializar registros de progresso para todas as conquistas
        achievements.forEach((achievement) => {
          stmt.run(userId, achievement.id, (err) => {
            if (err) {
              console.error(
                `Erro ao inicializar conquista ${achievement.id} para usuário ${userId}:`,
                err
              ); // Corrigido
            }
          });
        });

        // Finalizar o statement preparado
        stmt.finalize((err) => {
          if (err) {
            console.error(
              "Erro ao finalizar inicialização de conquistas do usuário:",
              err
            );
            reject(err);
          } else {
            console.log(`Conquistas inicializadas para usuário ${userId}`); // Corrigido
            resolve();
          }
        });
      });
    });
  });
}

// Garantir que as conquistas do usuário estejam inicializadas
function ensureUserAchievements(userId) {
  return new Promise((resolve, reject) => {
    // Verificar se o usuário já possui registros de progresso de conquistas
    db.get(
      "SELECT COUNT(*) as count FROM UserAchievementProgress WHERE user_id = ?",
      [userId],
      (err, row) => {
        if (err) {
          console.error("Erro ao verificar conquistas do usuário:", err);
          return reject(err);
        }

        // Se o usuário não tem registros de progresso de conquistas, inicializá-los
        if (row.count === 0) {
          initializeUserAchievements(userId).then(resolve).catch(reject);
        } else {
          resolve();
        }
      }
    );
  });
}

// Garantir que a entrada do SafeScore exista para um usuário
const ensureSafeScoreEntry = (userId) => {
  return new Promise((resolve, reject) => {
    // Tenta inserir ignorando se já existir (mais eficiente que SELECT + INSERT)
    db.run(
      "INSERT OR IGNORE INTO SafeScore (user_id, score) VALUES (?, 0)",
      [userId],
      (err) => {
        if (err) {
          console.error(
            `Erro ao garantir SafeScore para user_id ${userId}:`, // Corrigido
            err.message
          );
          return reject(new Error("Erro ao garantir SafeScore"));
        }
        // Não precisamos verificar se inseriu ou não, apenas que não houve erro.
        console.log(`Entrada SafeScore garantida para user_id ${userId}`); // Corrigido
        resolve();
      }
    );
  });
};

// Exporta a instância do DB e funções utilitárias
module.exports = {
  db,
  ensureSafeScoreEntry,
  initializeDefaultAchievements,
  initializeUserAchievements,
  ensureUserAchievements, // Certifique-se de que esta função está sendo exportada
};
