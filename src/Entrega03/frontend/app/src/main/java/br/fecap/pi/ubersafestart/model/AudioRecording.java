package br.fecap.pi.ubersafestart.model;

/**
 * Modelo para representar um arquivo de gravação de áudio
 */
public class AudioRecording {
    private String filePath;  // Caminho completo do arquivo
    private String timestamp; // Timestamp formatado
    private String duration;  // Duração formatada (mm:ss)
    private String fileSize;  // Tamanho formatado (ex: "1.2 MB")
    private long lastModified; // Timestamp de última modificação para ordenação

    public AudioRecording(String filePath, String timestamp, String duration, String fileSize, long lastModified) {
        this.filePath = filePath;
        this.timestamp = timestamp;
        this.duration = duration;
        this.fileSize = fileSize;
        this.lastModified = lastModified;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getDuration() {
        return duration;
    }

    public String getFileSize() {
        return fileSize;
    }

    public long getLastModified() {
        return lastModified;
    }

    @Override
    public String toString() {
        return "Gravação: " + timestamp;
    }
}