package br.com.ecofy.ms_ingestion.adapters.out.persistence;

import br.com.ecofy.ms_ingestion.adapters.out.persistence.entity.ImportFileEntity;
import br.com.ecofy.ms_ingestion.adapters.out.persistence.repository.ImportFileRepository;
import br.com.ecofy.ms_ingestion.core.domain.ImportFile;
import br.com.ecofy.ms_ingestion.core.port.out.SaveImportFilePort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ImportFileJpaAdapter implements SaveImportFilePort {

    private final ImportFileRepository repository;

    public ImportFileJpaAdapter(ImportFileRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ImportFile save(ImportFile file) {
        ImportFileEntity entity = PersistenceMapper.toEntity(file);
        ImportFileEntity saved = repository.save(entity);
        return PersistenceMapper.toDomain(saved);
    }
}
