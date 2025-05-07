require("dotenv").config();
const express = require("express");
const cors = require("cors");
const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const { encrypt, decrypt } = require("./crypto");
const { db, ensureSafeScoreEntry } = require("./db");

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
        ensureSafeScoreEntry(id)
          .then(() => res.status(201).json({ userId: id }))
          .catch(() => res.status(201).json({ userId: id, warning: "SafeScore init failed" }));
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
      ensureSafeScoreEntry(user.user_id)
        .finally(() => res.json({ token }));
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
          err => err ? res.status(500).json({ message: "Score update error" }) : res.json({ newScore })
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

// Health check
app.get("/", (req, res) => res.sendStatus(200));

// Global error handler
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ message: "Internal Server Error" });
});

// Start server and graceful shutdown
const server = app.listen(process.env.PORT || 3001);
const shutdown = () => server.close(() => db.close());
process.on('SIGTERM', shutdown);
process.on('SIGINT', shutdown);
