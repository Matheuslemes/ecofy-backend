package br.com.ecofy.ms_ingestion.core.application.service;

import br.com.ecofy.ms_ingestion.config.StorageProperties;
import br.com.ecofy.ms_ingestion.core.domain.ImportFile;
import br.com.ecofy.ms_ingestion.core.domain.enums.ImportFileType;
import br.com.ecofy.ms_ingestion.core.port.in.UploadFileUseCase;
import br.com.ecofy.ms_ingestion.core.port.out.SaveImportFilePort;
import br.com.ecofy.ms_ingestion.core.port.out.StoreFilePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class FileUploadService implements UploadFileUseCase {

    private final SaveImportFilePort saveImportFilePort;
    private final StoreFilePort storeFilePort;
    private final StorageProperties storageProperties;

    public FileUploadService(SaveImportFilePort saveImportFilePort,
                             StoreFilePort storeFilePort,
                             StorageProperties storageProperties) {
        this.saveImportFilePort = Objects.requireNonNull(saveImportFilePort, "saveImportFilePort must not be null");
        this.storeFilePort = Objects.requireNonNull(storeFilePort, "storeFilePort must not be null");
        this.storageProperties = Objects.requireNonNull(storageProperties, "storageProperties must not be null");
    }

    @Override
    public ImportFile upload(UploadFileCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        log.info(
                "[FileUploadService] - [upload] -> Recebendo arquivo name={} sizeBytes={}",
                command.originalFileName(), command.sizeBytes()
        );

        if (command.sizeBytes() <= 0) {
            throw new IllegalArgumentException("File size must be greater than zero");
        }

        if (command.sizeBytes() > storageProperties.getMaxFileSizeBytes()) {
            throw new IllegalArgumentException("File too large");
        }

        ImportFileType type = Objects.requireNonNull(
                command.type(),
                "ImportFileType (type) must not be null for uploaded file"
        );

        // 1) Cria o agregado apenas com metadados (caminho ainda indefinido)
        ImportFile file = ImportFile.create(
                command.originalFileName(),
                "TO_BE_DEFINED",
                type,
                command.sizeBytes()
        );

        // 2) Persiste metadados iniciais para garantir ID
        ImportFile persisted = saveImportFilePort.save(file);

        // 3) Grava conteúdo físico usando o ID/camadas de storage
        String storedPath = storeFilePort.store(persisted, command.content());

        // 4) Recria o agregado com o caminho definitivo
        ImportFile withPath = new ImportFile(
                persisted.id(),
                persisted.originalFileName(),
                storedPath,
                persisted.type(),
                persisted.sizeBytes(),
                persisted.uploadedAt()
        );

        // 5) Atualiza o registro com o path final
        ImportFile finalFile = saveImportFilePort.save(withPath);

        log.info(
                "[FileUploadService] - [upload] -> Arquivo salvo com sucesso id={} path={}",
                finalFile.id(), finalFile.storedPath()
        );

        return finalFile;
    }
}
