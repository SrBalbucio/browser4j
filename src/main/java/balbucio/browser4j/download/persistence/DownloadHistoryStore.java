package balbucio.browser4j.download.persistence;

import balbucio.browser4j.download.model.DownloadCategory;
import balbucio.browser4j.download.model.DownloadStatus;
import balbucio.browser4j.download.model.DownloadTask;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * JSON-based persistent store for download history.
 *
 * <p>Each profile gets its own {@code <profileDownloadDir>/history.json} file.
 * Writes are atomic (write-to-temp then rename) to survive crashes mid-write.
 */
public class DownloadHistoryStore {

    private static final Logger LOG = Logger.getLogger(DownloadHistoryStore.class.getName());
    private static final String HISTORY_FILE = "history.json";

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Instant.class,
                    (JsonSerializer<Instant>) (src, t, ctx) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(Instant.class,
                    (JsonDeserializer<Instant>) (json, t, ctx) -> Instant.parse(json.getAsString()))
            .create();

    private final Path storeFile;

    public DownloadHistoryStore(Path downloadDir) {
        this.storeFile = downloadDir.resolve(HISTORY_FILE);
        try { Files.createDirectories(downloadDir); } catch (IOException e) {
            LOG.warning("[Download] Cannot create download dir: " + downloadDir);
        }
    }

    /** Saves or updates a single task. If a task with the same downloadId exists, it is replaced. */
    public synchronized void save(DownloadTask task) {
        List<DownloadTask> all = loadRaw();
        all.removeIf(t -> t.getDownloadId().equals(task.getDownloadId()));
        all.add(task);
        persist(all);
    }

    /** Loads all tasks for this store (all profiles in this dir). */
    public synchronized List<DownloadTask> loadAll() {
        return new ArrayList<>(loadRaw());
    }

    /** Removes all tasks matching the given profileId. */
    public synchronized void clearByProfile(String profileId) {
        List<DownloadTask> all = loadRaw();
        all.removeIf(t -> profileId.equals(t.getProfileId()));
        persist(all);
    }

    /** Removes a single task by downloadId. */
    public synchronized void remove(String downloadId) {
        List<DownloadTask> all = loadRaw();
        all.removeIf(t -> t.getDownloadId().equals(downloadId));
        persist(all);
    }

    // ---- Internals ----

    private List<DownloadTask> loadRaw() {
        if (!Files.exists(storeFile)) return new ArrayList<>();
        try {
            String json = Files.readString(storeFile);
            Type listType = new TypeToken<List<DownloadTaskDto>>(){}.getType();
            List<DownloadTaskDto> dtos = GSON.fromJson(json, listType);
            if (dtos == null) return new ArrayList<>();
            return dtos.stream().map(DownloadTaskDto::toTask).collect(Collectors.toList());
        } catch (Exception e) {
            LOG.warning("[Download] Failed to read history: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void persist(List<DownloadTask> tasks) {
        try {
            List<DownloadTaskDto> dtos = tasks.stream().map(DownloadTaskDto::new).collect(Collectors.toList());
            String json = GSON.toJson(dtos);
            // Atomic write: temp file + rename
            Path tmp = storeFile.resolveSibling(HISTORY_FILE + ".tmp");
            Files.writeString(tmp, json);
            Files.move(tmp, storeFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            LOG.warning("[Download] Failed to persist history: " + e.getMessage());
        }
    }

    // ---- Flat DTO for Gson ----
    private static class DownloadTaskDto {
        String downloadId, url, fileName, fullPath, mimeType, status, category, createdAt, updatedAt, profileId, errorReason;
        long totalBytes, receivedBytes;
        int priority, retryCount;

        DownloadTaskDto(DownloadTask t) {
            this.downloadId    = t.getDownloadId();
            this.url           = t.getUrl();
            this.fileName      = t.getFileName();
            this.fullPath      = t.getFullPath();
            this.mimeType      = t.getMimeType();
            this.totalBytes    = t.getTotalBytes();
            this.receivedBytes = t.getReceivedBytes();
            this.status        = t.getStatus().name();
            this.category      = t.getCategory().name();
            this.createdAt     = t.getCreatedAt().toString();
            this.updatedAt     = t.getUpdatedAt().toString();
            this.profileId     = t.getProfileId();
            this.priority      = t.getPriority();
            this.retryCount    = t.getRetryCount();
            this.errorReason   = t.getErrorReason();
        }

        DownloadTask toTask() {
            return DownloadTask.builder()
                    .downloadId(downloadId).url(url).fileName(fileName).fullPath(fullPath)
                    .mimeType(mimeType).totalBytes(totalBytes).receivedBytes(receivedBytes)
                    .status(parseEnum(DownloadStatus.class, status, DownloadStatus.COMPLETED))
                    .category(parseEnum(DownloadCategory.class, category, DownloadCategory.OTHER))
                    .createdAt(Instant.parse(createdAt)).updatedAt(Instant.parse(updatedAt))
                    .profileId(profileId).priority(priority).retryCount(retryCount)
                    .errorReason(errorReason).build();
        }

        private <E extends Enum<E>> E parseEnum(Class<E> cls, String val, E fallback) {
            try { return Enum.valueOf(cls, val); } catch (Exception e) { return fallback; }
        }
    }
}
