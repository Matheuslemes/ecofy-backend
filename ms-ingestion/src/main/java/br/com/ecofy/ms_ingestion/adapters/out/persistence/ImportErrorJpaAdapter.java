package br.com.ecofy.ms_ingestion.adapters.out.persistence;

import br.com.ecofy.ms_ingestion.adapters.out.persistence.entity.ImportJobEntity;
import br.com.ecofy.ms_ingestion.adapters.out.persistence.entity.ImportErrorEntity;
import br.com.ecofy.ms_ingestion.adapters.out.persistence.repository.ImportErrorRepository;
import br.com.ecofy.ms_ingestion.adapters.out.persistence.repository.ImportJobRepository;
import br.com.ecofy.ms_ingestion.core.domain.ImportError;
import br.com.ecofy.ms_ingestion.core.port.out.SaveImportErrorPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ImportErrorJpaAdapter implements SaveImportErrorPort {

    private final ImportErrorRepository errorRepository;
    private final ImportJobRepository jobRepository;

    public ImportErrorJpaAdapter(ImportErrorRepository errorRepository,
                                 ImportJobRepository jobRepository) {
        this.errorRepository = errorRepository;
        this.jobRepository = jobRepository;
    }

    @Override
    @Transactional
    public ImportError save(ImportError error) {
        ImportJobEntity jobEntity = jobRepository.findById(error.importJobId())
                .orElseThrow(() -> new IllegalArgumentException("ImportJob not found: " + error.importJobId()));

        ImportErrorEntity entity = PersistenceMapper.toEntity(error, jobEntity);
        ImportErrorEntity saved = errorRepository.save(entity);
        return PersistenceMapper.toDomain(saved);
    }
}
