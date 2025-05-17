package br.fecap.pi.ubersafestart.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.fecap.pi.ubersafestart.model.AudioRecording;

/**
 * Utilitário para gerenciar gravações de áudio no aplicativo Uber SafeStart
 */
public class AudioRecordingManager {
    private static final String TAG = "AudioRecordingManager";
    private static final String RECORDINGS_DIR = "SafeStartRecordings";
    private static final String FILE_PREFIX = "recording_";
    private static final String FILE_EXTENSION = ".3gp";

    private MediaRecorder mediaRecorder;
    private String currentFilePath;
    private boolean isRecording = false;
    private Context context;

    public AudioRecordingManager(Context context) {
        this.context = context;
    }

    /**
     * Inicia a gravação de áudio
     * @return true se a gravação foi iniciada com sucesso, false caso contrário
     */
    public boolean startRecording() {
        if (isRecording) {
            Log.w(TAG, "Tentativa de iniciar uma gravação enquanto outra está em andamento");
            return false;
        }

        // Cria o diretório para gravações se não existir
        File recordingsDir = getRecordingsDirectory();
        if (!recordingsDir.exists()) {
            if (!recordingsDir.mkdirs()) {
                Log.e(TAG, "Falha ao criar diretório de gravações");
                return false;
            }
        }

        // Define o caminho do arquivo de gravação com timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
        String timestamp = dateFormat.format(new Date());
        currentFilePath = recordingsDir.getAbsolutePath() + "/" + FILE_PREFIX + timestamp + FILE_EXTENSION;

        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(currentFilePath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            Log.d(TAG, "Gravação iniciada: " + currentFilePath);
            return true;
        } catch (IOException | IllegalStateException e) {
            Log.e(TAG, "Erro ao iniciar gravação: " + e.getMessage(), e);
            releaseMediaRecorder();
            return false;
        }
    }

    /**
     * Para a gravação atual
     * @return o caminho do arquivo gravado, ou null se houver erro
     */
    public String stopRecording() {
        if (!isRecording) {
            Log.w(TAG, "Tentativa de parar uma gravação que não está em andamento");
            return null;
        }

        try {
            mediaRecorder.stop();
            Log.d(TAG, "Gravação interrompida");
            String filePath = currentFilePath;
            releaseMediaRecorder();
            return filePath;
        } catch (IllegalStateException e) {
            Log.e(TAG, "Erro ao parar gravação: " + e.getMessage(), e);
            releaseMediaRecorder();
            return null;
        }
    }

    /**
     * Libera recursos do MediaRecorder
     */
    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.release();
            } catch (Exception e) {
                Log.e(TAG, "Erro ao liberar MediaRecorder: " + e.getMessage());
            }
            mediaRecorder = null;
            isRecording = false;
            currentFilePath = null;
        }
    }

    /**
     * Verifica se há uma gravação em andamento
     * @return true se estiver gravando, false caso contrário
     */
    public boolean isRecording() {
        return isRecording;
    }

    /**
     * Retorna todas as gravações armazenadas
     * @return lista de objetos AudioRecording
     */
    public List<AudioRecording> getAllRecordings() {
        List<AudioRecording> recordings = new ArrayList<>();
        File recordingsDir = getRecordingsDirectory();

        if (recordingsDir.exists() && recordingsDir.isDirectory()) {
            File[] files = recordingsDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isFile() && file.getName().endsWith(FILE_EXTENSION);
                }
            });

            if (files != null) {
                for (File file : files) {
                    String filename = file.getName();
                    long fileSize = file.length();
                    long lastModified = file.lastModified();

                    // Extrair timestamp do nome do arquivo
                    String timestamp = "Unknown";
                    if (filename.startsWith(FILE_PREFIX) && filename.endsWith(FILE_EXTENSION)) {
                        timestamp = filename.substring(
                                FILE_PREFIX.length(),
                                filename.length() - FILE_EXTENSION.length()
                        );
                        // Formatar para exibição
                        timestamp = timestamp.replace('_', ' ');
                    }

                    // Calcular duração aproximada com base no tamanho do arquivo
                    // Usando ~12kb por segundo como aproximação para AMR_NB
                    int durationSecs = (int) (fileSize / 12000);
                    String duration = String.format(Locale.getDefault(),
                            "%d:%02d", durationSecs / 60, durationSecs % 60);

                    recordings.add(new AudioRecording(
                            file.getAbsolutePath(),
                            timestamp,
                            duration,
                            formatFileSize(fileSize),
                            lastModified
                    ));
                }

                // Ordenar por data de modificação (mais recente primeiro)
                Collections.sort(recordings, new Comparator<AudioRecording>() {
                    @Override
                    public int compare(AudioRecording o1, AudioRecording o2) {
                        return Long.compare(o2.getLastModified(), o1.getLastModified());
                    }
                });
            }
        }

        return recordings;
    }

    /**
     * Reproduz o áudio no caminho especificado
     * @param filePath caminho do arquivo a ser reproduzido
     * @return MediaPlayer instanciado ou null se houver erro
     */
    public MediaPlayer playRecording(String filePath) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            return mediaPlayer;
        } catch (IOException e) {
            Log.e(TAG, "Erro ao reproduzir gravação: " + e.getMessage(), e);
            mediaPlayer.release();
            return null;
        }
    }

    /**
     * Exclui uma gravação específica
     * @param filePath caminho do arquivo a ser excluído
     * @return true se a exclusão foi bem-sucedida, false caso contrário
     */
    public boolean deleteRecording(String filePath) {
        File file = new File(filePath);
        boolean deleted = file.delete();
        if (deleted) {
            Log.d(TAG, "Gravação excluída: " + filePath);
        } else {
            Log.e(TAG, "Falha ao excluir gravação: " + filePath);
        }
        return deleted;
    }

    /**
     * Obtém o diretório onde as gravações são armazenadas
     * @return objeto File representando o diretório
     */
    private File getRecordingsDirectory() {
        return new File(context.getExternalFilesDir(null), RECORDINGS_DIR);
    }

    /**
     * Formata o tamanho do arquivo para exibição
     * @param bytes tamanho em bytes
     * @return string formatada (ex: "1.2 MB")
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0);
        } else {
            return String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024));
        }
    }
}