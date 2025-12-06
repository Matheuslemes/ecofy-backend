package br.com.ecofy.ms_ingestion.adapters.out.persistence;

import br.com.ecofy.ms_ingestion.adapters.out.persistence.entity.ImportFileEntity;
import br.com.ecofy.ms_ingestion.adapters.out.persistence.entity.ImportJobEntity;
import br.com.ecofy.ms_ingestion.adapters.out.persistence.repository.ImportFileRepository;
import br.com.ecofy.ms_ingestion.adapters.out.persistence.repository.ImportJobRepository;
import br.com.ecofy.ms_ingestion.core.domain.ImportJob;
import br.com.ecofy.ms_ingestion.core.port.out.LoadImportJobPort;
import br.com.ecofy.ms_ingestion.core.port.out.SaveImportJobPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class ImportJobJpaAdapter implements SaveImportJobPort, LoadImportJobPort {

    private final ImportJobRepository importJobRepository;
    private final ImportFileRepository importFileRepository;

    public ImportJobJpaAdapter(ImportJobRepository importJobRepository,
                               ImportFileRepository importFileRepository) {
        this.importJobRepository = importJobRepository;
        this.importFileRepository = importFileRepository;
    }

    @Override
    @Transactional
    public ImportJob save(ImportJob job) {
        ImportFileEntity fileEntity = importFileRepository.findById(job.importFileId())
                .orElseThrow(() -> new IllegalArgumentException("ImportFile not found: " + job.importFileId()));

        ImportJobEntity entity = PersistenceMapper.toEntity(job, fileEntity);
        ImportJobEntity saved = importJobRepository.save(entity);
        return PersistenceMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ImportJob> loadById(java.util.UUID jobId) {
        return importJobRepository.findById(jobId)
                .map(PersistenceMapper::toDomain);
    }
}
