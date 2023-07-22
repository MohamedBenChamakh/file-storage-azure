package com.value.filestorage;

import com.azure.storage.blob.models.BlobProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private AzureBlobService azureBlobService;

    @PostMapping("/{containerName}/{blobName}")
    public void uploadFile(@PathVariable String containerName, @PathVariable String blobName,
                           @RequestParam("file") MultipartFile file) throws IOException {
        // Check if the file is not empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty.");
        }

        // Check if the file size exceeds the maximum allowed size (e.g., 10 MB)
        long maxSizeBytes = 10 * 1024 * 1024; // 10 MB in bytes
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("File size exceeds the maximum allowed size (10 MB).");
        }

        // Check if the file extension is valid (allowing only images and videos in this example)
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && !originalFilename.matches(".+\\.(jpg|jpeg|png|gif|mp4|avi|mkv)$")) {
            throw new IllegalArgumentException("Invalid file extension. Allowed extensions: jpg, jpeg, png, gif, mp4, avi, mkv.");
        }

        // Check if the blob name already exists in the container
        if (azureBlobService.blobExists(containerName, blobName)) {
            throw new IllegalArgumentException("Blob name already exists in the container.");
        }

        azureBlobService.uploadFile(containerName, blobName, file);
    }

    @GetMapping("/{containerName}/{blobName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String containerName, @PathVariable String blobName) throws IOException {
        // Check if the file exists
        BlobProperties blobProperties = azureBlobService.getBlobProperties(containerName, blobName);
        if (blobProperties == null) {
            // Return 404 Not Found if the blob doesn't exist
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // File exists, proceed with downloading
        byte[] fileContent = azureBlobService.downloadFile(containerName, blobName);

        // Set appropriate headers and return the file content in the response
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(fileContent);
    }

    @DeleteMapping("/{containerName}/{blobName}")
    public ResponseEntity<Void> deleteFile(@PathVariable String containerName, @PathVariable String blobName) {
        // Check if the file exists
        BlobProperties blobProperties = azureBlobService.getBlobProperties(containerName, blobName);
        if (blobProperties == null) {
            // Return 404 Not Found if the blob doesn't exist
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // File exists, proceed with deletion
        azureBlobService.deleteFile(containerName, blobName);
        // Return 204 No Content as the response after successful deletion
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/url/{containerName}/{blobName}")
    public String getFileUrl(@PathVariable String containerName, @PathVariable String blobName) {
        String sasToken = azureBlobService.generateSasToken(containerName, blobName);
        String encodedBlobName = URLEncoder.encode(blobName, StandardCharsets.UTF_8);
        return String.format("https://%s.blob.core.windows.net/%s/%s?%s", azureBlobService.getAccountName(), containerName, encodedBlobName, sasToken);
    }

    @GetMapping("/{containerName}")
    public List<String> listFiles(@PathVariable String containerName) {
        return azureBlobService.listFiles(containerName);
    }
}
