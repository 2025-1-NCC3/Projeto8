const sqlite3 = require("sqlite3").verbose();
const path = require("path");

const dbPath = path.join(__dirname, 'safestart.db');

// Cria e exporta a instância do banco de dados
const db = new sqlite3.Database(dbPath, (err) => {
  if (err) {
    console.error("Erro ao conectar ao banco de dados SQLite:", err.message);
    // Considerar encerrar o processo se a conexão falhar
    // process.exit(1); 
  } else {
    console.log("Conectado ao banco de dados SQLite:", dbPath);
    // Habilita chaves estrangeiras assim que a conexão é estabelecida
    db.run('PRAGMA foreign_keys = ON', (pragmaErr) => {
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
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  )`;

  const createSafeScoreTable = `
  CREATE TABLE IF NOT EXISTS SafeScore (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL UNIQUE, -- Garante um SafeScore por usuário
    score INTEGER DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE
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
    console.log("Criação/verificação de tabelas concluída.");
  });
}

const ensureSafeScoreEntry = (userId) => {
    return new Promise((resolve, reject) => {
      // Tenta inserir ignorando se já existir (mais eficiente que SELECT + INSERT)
      db.run("INSERT OR IGNORE INTO SafeScore (user_id, score) VALUES (?, 0)", [userId], (err) => {
        if (err) {
          console.error(`Erro ao garantir SafeScore para user_id ${userId}:`, err.message);
          return reject(new Error("Erro ao garantir SafeScore"));
        }
        // Não precisamos verificar se inseriu ou não, apenas que não houve erro.
        console.log(`Entrada SafeScore garantida para user_id ${userId}`);
        resolve();
      });
    });
};

// Exporta a instância do DB para ser usada em outros módulos
module.exports = {db, ensureSafeScoreEntry};