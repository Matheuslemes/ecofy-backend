package br.com.ecofy.ms_insights.core.domain.report;

import java.time.Instant;
import java.util.UUID;

public class Report {
    private final UUID id;
    private final UUID userId;
    private final String type;
    private final ReportStatus status;
    private final String artifactRef;
    private final Instant createdAt;

    public Report(UUID id, UUID userId, String type, ReportStatus status, String artifactRef, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.status = status;
        this.artifactRef = artifactRef;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getType() { return type; }
    public ReportStatus getStatus() { return status; }
    public String getArtifactRef() { return artifactRef; }
    public Instant getCreatedAt() { return createdAt; }
}
