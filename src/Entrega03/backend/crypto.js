const crypto = require("crypto");

const ALGORITHM = "aes-256-cbc";
// Chave em hex (32 bytes)
const KEY = Buffer.from(process.env.ENCRYPTION_KEY, "hex");
// IV de 16 bytes gerado aleatoriamente a cada criptografia

function encrypt(plainText) {
  const iv = crypto.randomBytes(16);
  const cipher = crypto.createCipheriv(ALGORITHM, KEY, iv);
  const encrypted = Buffer.concat([
    cipher.update(Buffer.from(plainText, "utf8")),
    cipher.final(),
  ]);
  // Prefixa IV + dados e codifica em base64
  return Buffer.concat([iv, encrypted]).toString("base64");
}

function decrypt(b64) {
  const input = Buffer.from(b64, "base64");
  const iv = input.slice(0, 16);
  const data = input.slice(16);
  const decipher = crypto.createDecipheriv(ALGORITHM, KEY, iv);
  const decrypted = Buffer.concat([decipher.update(data), decipher.final()]);
  return decrypted.toString("utf8");
}

module.exports = { encrypt, decrypt };
