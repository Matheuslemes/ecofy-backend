package br.com.ecofy.ms_ingestion.adapters.out.persistence;

import br.com.ecofy.ms_ingestion.adapters.out.persistence.entity.ImportFileEntity;
import br.com.ecofy.ms_ingestion.adapters.out.persistence.repository.ImportFileRepository;
import br.com.ecofy.ms_ingestion.core.domain.ImportFile;
import br.com.ecofy.ms_ingestion.core.domain.enums.ImportFileType;
import br.com.ecofy.ms_ingestion.core.port.out.SaveImportFilePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Component
public class ImportFileJpaAdapter implements SaveImportFilePort {

    private final ImportFileRepository repository;

    public ImportFileJpaAdapter(ImportFileRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    @Override
    @Transactional
    public ImportFile save(ImportFile file) {
        Objects.requireNonNull(file, "file must not be null");

        ImportFileType type = Objects.requireNonNull(
                file.type(),
                "ImportFileType (type) must not be null for file id=" + file.id()
        );

        ImportFileEntity entity = PersistenceMapper.toEntity(file);
        ImportFileEntity saved = repository.save(entity);

        log.debug(
                "[ImportFileJpaAdapter] - [save] -> ImportFile persistido id={} type={}",
                saved.getId(), type
        );

        return PersistenceMapper.toDomain(saved);
    }
}
