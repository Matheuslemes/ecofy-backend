package br.com.ecofy.ms_insights.core.port.out;

public interface PublishReportReadyEventPort {
    void publish(String reportId, String userId, String artifactRef);
}
