package br.com.ecofy.ms_ingestion.core.port.in;

import br.com.ecofy.ms_ingestion.core.domain.ImportFile;
import br.com.ecofy.ms_ingestion.core.domain.enums.ImportFileType;

public interface UploadFileUseCase {

    record UploadFileCommand(
            String originalFileName,
            ImportFileType type,
            long sizeBytes,
            byte[] content
    ) { }

    ImportFile upload(UploadFileCommand command);

}