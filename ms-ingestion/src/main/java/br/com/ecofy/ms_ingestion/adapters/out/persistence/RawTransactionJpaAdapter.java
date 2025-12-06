package br.com.ecofy.ms_ingestion.adapters.out.persistence;

import br.com.ecofy.ms_ingestion.adapters.out.persistence.entity.ImportFileEntity;
import br.com.ecofy.ms_ingestion.adapters.out.persistence.entity.ImportJobEntity;
import br.com.ecofy.ms_ingestion.adapters.out.persistence.repository.ImportFileRepository;
import br.com.ecofy.ms_ingestion.adapters.out.persistence.repository.ImportJobRepository;
import br.com.ecofy.ms_ingestion.adapters.out.persistence.repository.RawTransactionRepository;
import br.com.ecofy.ms_ingestion.core.domain.RawTransaction;
import br.com.ecofy.ms_ingestion.core.port.out.SaveRawTransactionPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class RawTransactionJpaAdapter implements SaveRawTransactionPort {

    private final RawTransactionRepository rawTransactionRepository;
    private final ImportJobRepository importJobRepository;
    private final ImportFileRepository importFileRepository;

    public RawTransactionJpaAdapter(RawTransactionRepository rawTransactionRepository,
                                    ImportJobRepository importJobRepository,
                                    ImportFileRepository importFileRepository) {
        this.rawTransactionRepository = rawTransactionRepository;
        this.importJobRepository = importJobRepository;
        this.importFileRepository = importFileRepository;
    }

    @Override
    @Transactional
    public RawTransaction save(RawTransaction transaction) {
        ImportJobEntity job = importJobRepository.findById(transaction.importJobId())
                .orElseThrow(() -> new IllegalArgumentException("ImportJob not found: " + transaction.importJobId()));

        ImportFileEntity file = importFileRepository.findById(transaction.importFileId())
                .orElseThrow(() -> new IllegalArgumentException("ImportFile not found: " + transaction.importFileId()));

        var entity = PersistenceMapper.toEntity(transaction, job, file);
        var saved = rawTransactionRepository.save(entity);
        return PersistenceMapper.toDomain(saved);
    }
}
