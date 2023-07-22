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
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private AzureBlobServiceImpl azureBlobServiceImpl;

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
        if (originalFilename != null && !originalFilename.matches(".+\\.(jpg|jpeg|png|mp4|pdf)$")) {
            throw new IllegalArgumentException("Invalid file extension. Allowed extensions: jpg, jpeg, png, mp4, pdf.");
        }

        // Check if the blob name already exists in the container
        if (azureBlobServiceImpl.blobExists(containerName, blobName)) {
            throw new IllegalArgumentException("Blob name already exists in the container.");
        }

        azureBlobServiceImpl.uploadFile(containerName, blobName, file);
    }

    @GetMapping("/{containerName}/{blobName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String containerName, @PathVariable String blobName) throws IOException {
        // Check if the file exists
        BlobProperties blobProperties = azureBlobServiceImpl.getBlobProperties(containerName, blobName);
        if (blobProperties == null) {
            // Return 404 Not Found if the blob doesn't exist
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // File exists, proceed with downloading
        byte[] fileContent = azureBlobServiceImpl.downloadFile(containerName, blobName);
        // Set appropriate headers and return the file content in the response
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(fileContent);
    }

    @GetMapping("/images/{containerName}/{blobName}")
    public ResponseEntity<byte[]> downloadImage(@PathVariable String containerName, @PathVariable String blobName) throws IOException {
        // Check if the file exists
        BlobProperties blobProperties = azureBlobServiceImpl.getBlobProperties(containerName, blobName);
        if (blobProperties == null) {
            // Return 404 Not Found if the blob doesn't exist
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // File exists, proceed with downloading
        byte[] fileContent = azureBlobServiceImpl.downloadFile(containerName, blobName);

        if (blobName.matches(".+\\.(jpg|jpeg)$")) {
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(fileContent);
        }else if(blobName.matches(".+\\.(png)$")){
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(fileContent);
        }else{
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(fileContent);
        }

    }

    @GetMapping("/videos/{containerName}/{blobName}")
    public ResponseEntity<byte[]> downloadVideo(@PathVariable String containerName, @PathVariable String blobName) throws IOException {
        // Check if the file exists
        BlobProperties blobProperties = azureBlobServiceImpl.getBlobProperties(containerName, blobName);
        if (blobProperties == null) {
            // Return 404 Not Found if the blob doesn't exist
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // File exists, proceed with downloading
        byte[] fileContent = azureBlobServiceImpl.downloadFile(containerName, blobName);

        if (blobName.matches(".+\\.(mp4)$")) {
            return ResponseEntity.ok().contentType(MediaType.valueOf("video/mp4")).body(fileContent);
        }else{
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(fileContent);
        }

    }

    @DeleteMapping("/{containerName}/{blobName}")
    public ResponseEntity<Void> deleteFile(@PathVariable String containerName, @PathVariable String blobName) {
        // Check if the file exists
        BlobProperties blobProperties = azureBlobServiceImpl.getBlobProperties(containerName, blobName);
        if (blobProperties == null) {
            // Return 404 Not Found if the blob doesn't exist
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // File exists, proceed with deletion
        azureBlobServiceImpl.deleteFile(containerName, blobName);
        // Return 204 No Content as the response after successful deletion
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/url/{containerName}/{blobName}")
    public String getFileUrl(@PathVariable String containerName, @PathVariable String blobName) {
        String sasToken = azureBlobServiceImpl.generateSasToken(containerName, blobName);
        String encodedBlobName = URLEncoder.encode(blobName, StandardCharsets.UTF_8);
        return String.format("https://%s.blob.core.windows.net/%s/%s?%s", azureBlobServiceImpl.getAccountName(), containerName, encodedBlobName, sasToken);
    }

    @GetMapping("/{containerName}")
    public List<String> listFiles(@PathVariable String containerName) {
        return azureBlobServiceImpl.listFiles(containerName);
    }
}
