package br.com.ecofy.ms_ingestion.core.domain;

import br.com.ecofy.ms_ingestion.core.domain.enums.ImportFileType;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class ImportFile {

    private final UUID id;
    private final String originalFileName;
    private final String storedPath;
    private final ImportFileType type;
    private final long sizeBytes;
    private final Instant uploadedAt;

    public ImportFile(UUID id,
                      String originalFileName,
                      String storedPath,
                      ImportFileType type,
                      long sizeBytes,
                      Instant uploadedAt) {

        this.id = Objects.requireNonNull(id);
        this.originalFileName = Objects.requireNonNull(originalFileName);
        this.storedPath = Objects.requireNonNull(storedPath);
        this.type = Objects.requireNonNull(type);
        this.sizeBytes = sizeBytes;
        this.uploadedAt = Objects.requireNonNull(uploadedAt);
    }

    public static ImportFile create(String originalFileName,
                                    String storedPath,
                                    ImportFileType type,
                                    long sizeBytes) {

        return new ImportFile(
                UUID.randomUUID(),
                originalFileName,
                storedPath,
                type,
                sizeBytes,
                Instant.now()
        );
    }

    public UUID id() {
        return id;
    }

    public String originalFileName() {
        return originalFileName;
    }

    public String storedPath() {
        return storedPath;
    }

    public ImportFileType type() {
        return type;
    }

    public long sizeBytes() {
        return sizeBytes;
    }

    public Instant uploadedAt() {
        return uploadedAt;
    }

}
