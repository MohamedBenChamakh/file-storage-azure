package com.value.filestorage;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AzureBlobService {

    @Value("${spring.azure.storage.connection-string}")
    private String connectionString;

    public boolean blobExists(String containerName, String blobName) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        return containerClient.getBlobClient(blobName).exists();
    }

    public BlobProperties getBlobProperties(String containerName, String blobName) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
        return blobServiceClient.getBlobContainerClient(containerName).getBlobClient(blobName).getProperties();
    }

    public String generateSasToken(String containerName, String blobName) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();

        BlobServiceSasSignatureValues sasSignatureValues = new BlobServiceSasSignatureValues(
                OffsetDateTime.now().plusMinutes(5), // Token expiration time (5 minutes from now)
                BlobSasPermission.parse("r") // Blob-level read permission
        );

        String encodedBlobName = URLEncoder.encode(blobName, StandardCharsets.UTF_8);

        return blobServiceClient.getBlobContainerClient(containerName).getBlobClient(encodedBlobName)
                .generateSas(sasSignatureValues);
    }

    public String getAccountName() {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
        return blobServiceClient.getAccountName();
    }

    public void uploadFile(String containerName, String blobName, MultipartFile file) throws IOException {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
        blobServiceClient.getBlobContainerClient(containerName).getBlobClient(blobName)
                .upload(file.getInputStream(), file.getSize());
    }

    public byte[] downloadFile(String containerName, String blobName) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
        blobServiceClient.getBlobContainerClient(containerName).getBlobClient(blobName)
                .downloadToFile(blobName);
        return new byte[0];
    }

    public void deleteFile(String containerName, String blobName) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
        blobServiceClient.getBlobContainerClient(containerName).getBlobClient(blobName).delete();
    }

    public List<String> listFiles(String containerName) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
        List<String> fileList = new ArrayList<>();
        for (BlobItem blobItem : blobServiceClient.getBlobContainerClient(containerName).listBlobs()) {
            fileList.add(blobItem.getName());
        }
        return fileList;
    }
}
