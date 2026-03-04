package com.devportal.service;

import com.devportal.domain.enums.ModuleType;
import com.devportal.dto.response.FileDownloadResponse;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GridFsFileService {

    private final GridFsTemplate gridFsTemplate;
    private final GridFSBucket gridFSBucket;

    @Value("${file.upload.max-size:20971520}")
    private long maxFileSize;

    @Value("${file.upload.allowed-types:application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,image/png,image/jpeg,image/jpg,application/zip,text/plain}")
    private String allowedTypes;

    public String uploadFile(MultipartFile file, ModuleType moduleType, UUID moduleId, UUID uploadedBy) throws IOException {
        validateFile(file);

        Document metadata = new Document()
                .append("module_type", moduleType.name())
                .append("module_id", moduleId.toString())
                .append("uploaded_by", uploadedBy.toString())
                .append("original_filename", file.getOriginalFilename())
                .append("content_type", file.getContentType())
                .append("file_size", file.getSize());

        GridFSUploadOptions options = new GridFSUploadOptions()
                .chunkSizeBytes(1024 * 1024)
                .metadata(metadata);

        try (InputStream inputStream = file.getInputStream()) {
            ObjectId fileId = gridFSBucket.uploadFromStream(
                    file.getOriginalFilename(),
                    inputStream,
                    options
            );
            log.info("File uploaded to GridFS: {} with ID: {}", file.getOriginalFilename(), fileId.toHexString());
            return fileId.toHexString();
        }
    }

    public FileDownloadResponse downloadFile(String fileId) {
        ObjectId objectId = new ObjectId(fileId);
        GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(objectId)));
        
        if (gridFSFile == null) {
            throw new RuntimeException("File not found with ID: " + fileId);
        }

        try (GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(objectId);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = downloadStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            Document metadata = gridFSFile.getMetadata();
            String contentType = metadata != null ? metadata.getString("content_type") : "application/octet-stream";
            String fileName = gridFSFile.getFilename();

            return FileDownloadResponse.builder()
                    .data(outputStream.toByteArray())
                    .fileName(fileName)
                    .contentType(contentType)
                    .fileSize(gridFSFile.getLength())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Error downloading file: " + fileId, e);
        }
    }

    public void deleteFile(String fileId) {
        try {
            ObjectId objectId = new ObjectId(fileId);
            gridFSBucket.delete(objectId);
            log.info("File deleted from GridFS: {}", fileId);
        } catch (Exception e) {
            log.error("Error deleting file from GridFS: {}", fileId, e);
        }
    }

    public boolean fileExists(String fileId) {
        try {
            ObjectId objectId = new ObjectId(fileId);
            GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(objectId)));
            return file != null;
        } catch (Exception e) {
            return false;
        }
    }

    public InputStream getFileAsStream(String fileId) {
        try {
            ObjectId objectId = new ObjectId(fileId);
            return gridFSBucket.openDownloadStream(objectId);
        } catch (Exception e) {
            log.error("Error getting file stream: {}", fileId, e);
            return null;
        }
    }

    public String getFileContentType(String fileId) {
        try {
            ObjectId objectId = new ObjectId(fileId);
            GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(objectId)));
            if (gridFSFile != null && gridFSFile.getMetadata() != null) {
                return gridFSFile.getMetadata().getString("content_type");
            }
        } catch (Exception e) {
            log.error("Error getting file content type: {}", fileId, e);
        }
        return null;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of " + (maxFileSize / 1024 / 1024) + "MB");
        }

        String contentType = file.getContentType();
        List<String> allowedTypesList = Arrays.asList(allowedTypes.split(","));
        
        if (contentType == null || !allowedTypesList.contains(contentType)) {
            throw new IllegalArgumentException("File type not allowed: " + contentType + ". Allowed types: " + allowedTypes);
        }
    }
}
