package com.devportal.controller;

import com.devportal.dto.response.FileDownloadResponse;
import com.devportal.service.GridFsFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "File Management", description = "APIs for file download and preview")
public class FileController {

    private final GridFsFileService gridFsFileService;

    @GetMapping("/{fileId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Download file by MongoDB file ID")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileId) {
        FileDownloadResponse file = gridFsFileService.downloadFile(fileId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .contentLength(file.getFileSize())
                .body(file.getData());
    }

    @GetMapping("/{fileId}/preview")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Preview file inline (for images and PDFs)")
    public ResponseEntity<byte[]> previewFile(@PathVariable String fileId) {
        FileDownloadResponse file = gridFsFileService.downloadFile(fileId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .contentLength(file.getFileSize())
                .body(file.getData());
    }

    @GetMapping("/{fileId}/exists")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Check if file exists")
    public ResponseEntity<Boolean> fileExists(@PathVariable String fileId) {
        return ResponseEntity.ok(gridFsFileService.fileExists(fileId));
    }
}
