package br.com.ecofy.ms_ingestion.core.application.service;

import br.com.ecofy.ms_ingestion.config.StorageProperties;
import br.com.ecofy.ms_ingestion.core.domain.ImportFile;
import br.com.ecofy.ms_ingestion.core.domain.enums.ImportFileType;
import br.com.ecofy.ms_ingestion.core.port.in.UploadFileUseCase;
import br.com.ecofy.ms_ingestion.core.port.out.SaveImportFilePort;
import br.com.ecofy.ms_ingestion.core.port.out.StoreFilePort;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class FileUploadService implements UploadFileUseCase {

    private final SaveImportFilePort saveImportFilePort;
    private final StoreFilePort storeFilePort;
    private final StorageProperties storageProperties;

    public FileUploadService(SaveImportFilePort saveImportFilePort,
                             StoreFilePort storeFilePort,
                             StorageProperties storageProperties) {
        this.saveImportFilePort = Objects.requireNonNull(saveImportFilePort);
        this.storeFilePort = Objects.requireNonNull(storeFilePort);
        this.storageProperties = Objects.requireNonNull(storageProperties);
    }

    @Override
    public ImportFile upload(UploadFileUseCase.UploadFileCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        log.info("[FileUploadService] - [upload] -> Recebendo arquivo name={} sizeBytes={}",
                command.originalFileName(), command.sizeBytes());

        if (command.sizeBytes() > storageProperties.getMaxFileSizeBytes()) {
            throw new IllegalArgumentException("File too large");
        }

        ImportFileType type = command.type();
        ImportFile file = ImportFile.create(
                command.originalFileName(),
                "TO_BE_DEFINED",
                type,
                command.sizeBytes()
        );

        // primeiro salva metadados para obter ID etc
        ImportFile persisted = saveImportFilePort.save(file);

        // grava o conteúdo em storage
        String storedPath = storeFilePort.store(persisted, command.content());

        // recria objeto com path definitivo
        ImportFile withPath = new ImportFile(
                persisted.id(),
                persisted.originalFileName(),
                storedPath,
                persisted.type(),
                persisted.sizeBytes(),
                persisted.uploadedAt()
        );

        ImportFile finalFile = saveImportFilePort.save(withPath);
        log.info("[FileUploadService] - [upload] -> Arquivo salvo com sucesso id={} path={}",
                finalFile.id(), finalFile.storedPath());

        return finalFile;
    }

}
