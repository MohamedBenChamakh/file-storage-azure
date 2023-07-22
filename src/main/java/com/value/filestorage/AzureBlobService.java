package com.value.filestorage;

import com.azure.storage.blob.models.BlobProperties;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AzureBlobService {
    boolean blobExists(String containerName, String blobName);

    BlobProperties getBlobProperties(String containerName, String blobName);

    String generateSasToken(String containerName, String blobName);

    String getAccountName();


    void uploadFile(String containerName, String blobName, MultipartFile file) throws IOException;

    byte[] downloadFile(String containerName, String blobName) throws IOException;

    void deleteFile(String containerName, String blobName);

    List<String> listFiles(String containerName);
}
