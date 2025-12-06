package br.com.ecofy.ms_ingestion.adapters.in.web;

import br.com.ecofy.ms_ingestion.adapters.in.web.dto.ImportJobResponse;
import br.com.ecofy.ms_ingestion.adapters.in.web.dto.ImportJobStatusResponse;
import br.com.ecofy.ms_ingestion.core.domain.ImportFile;
import br.com.ecofy.ms_ingestion.core.domain.enums.ImportFileType;
import br.com.ecofy.ms_ingestion.core.port.in.GetImportJobStatusUseCase;
import br.com.ecofy.ms_ingestion.core.port.in.StartImportJobUseCase;
import br.com.ecofy.ms_ingestion.core.port.in.UploadFileUseCase;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/import")
@Slf4j
public class ImportController {

    private final UploadFileUseCase uploadFileUseCase;
    private final StartImportJobUseCase startImportJobUseCase;
    private final
    GetImportJobStatusUseCase getImportJobStatusUseCase;

    public ImportController(UploadFileUseCase uploadFileUseCase,
                            StartImportJobUseCase startImportJobUseCase,
                            GetImportJobStatusUseCase getImportJobStatusUseCase) {
        this.uploadFileUseCase = uploadFileUseCase;
        this.startImportJobUseCase = startImportJobUseCase;
        this.getImportJobStatusUseCase = getImportJobStatusUseCase;
    }

    @PostMapping("/file")
    public ResponseEntity<ImportJobResponse> uploadAndStart(
            @RequestParam("file") @NotNull MultipartFile file,
            @RequestParam(value = "type", required = false) ImportFileType type
    ) throws IOException {

        log.info("[ImportController] - [uploadAndStart] -> Recebendo upload fileName={} size={}",
                file.getOriginalFilename(), file.getSize());

        ImportFileType resolvedType = type != null ? type : guessType(file.getOriginalFilename());
        byte[] bytes = file.getBytes();

        ImportFile importFile = uploadFileUseCase.upload(
                new UploadFileUseCase.UploadFileCommand(
                        file.getOriginalFilename(),
                        resolvedType,
                        file.getSize(),
                        bytes
                )
        );

        var job = startImportJobUseCase.start(
                new StartImportJobUseCase.StartImportJobCommand(importFile.id())
        );

        return ResponseEntity.accepted().body(ImportJobResponse.fromDomain(job));
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<ImportJobStatusResponse> getJob(@PathVariable("id") UUID id) {
        log.debug("[ImportController] - [getJob] -> Consultando job id={}", id);

        var view = getImportJobStatusUseCase.getById(id);
        return ResponseEntity.ok(ImportJobStatusResponse.fromView(view));
    }

    private ImportFileType guessType(String filename) {
        String lower = filename != null ? filename.toLowerCase() : "";
        if (lower.endsWith(".csv")) {
            return ImportFileType.CSV;
        }
        if (lower.endsWith(".ofx")) {
            return ImportFileType.OFX;
        }
        throw new IllegalArgumentException("Could not infer file type from name: " + filename);
    }
}