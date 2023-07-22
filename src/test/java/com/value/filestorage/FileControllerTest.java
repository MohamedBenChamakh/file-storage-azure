package com.value.filestorage;

import com.azure.storage.blob.models.BlobProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private AzureBlobServiceImpl service;

    @InjectMocks
    private FileController fileController;

    @DisplayName("upload file to blob test succeed")
    @ParameterizedTest
    @ValueSource(strings = {"jpeg", "png", "pdf", "mp4"})
    void uploadFileSucceed(String extension) throws IOException {
        String container = "container";
        String blobName = "blobName." + extension;
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(10 * 1024 * 1024L);
        when(file.getOriginalFilename()).thenReturn(blobName);
        when(service.blobExists(container, blobName)).thenReturn(false);

        fileController.uploadFile(container, blobName, file);

        verify(service, times(1)).uploadFile(anyString(), anyString(), any());

    }

    @DisplayName("upload file to blob test failed - file empty")
    @Test
    void uploadFileFailed_FileEmpty() throws IOException {
        String container = "container";
        String blobName = "blobName.jpg";
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> fileController.uploadFile(container, blobName, file));

        assertThat(exception.getMessage()).isEqualTo("File is empty.");
        verify(service, never()).uploadFile(anyString(), anyString(), any());

    }

    @DisplayName("upload file to blob test failed - file size exceeded")
    @Test
    void uploadFileFailed_FileSizeExceeded() throws IOException {
        String container = "container";
        String blobName = "blobName.jpg";
        MultipartFile file = mock(MultipartFile.class);

        when(file.getSize()).thenReturn(11 * 1024 * 1024L);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> fileController.uploadFile(container, blobName, file));

        assertThat(exception.getMessage()).isEqualTo("File size exceeds the maximum allowed size (10 MB).");
        verify(service, never()).uploadFile(anyString(), anyString(), any());

    }

    @DisplayName("download file from blob test")
    @Test
    void downloadFile() throws IOException {
        when(service.getBlobProperties(anyString(), anyString())).thenReturn(mock(BlobProperties.class));
        when(service.downloadFile(anyString(), anyString())).thenReturn(new byte[]{});

        ResponseEntity<byte[]> responseEntity = fileController.downloadFile("container", "fileName");

        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @DisplayName("download image jpg, jpeg or png test")
    @ParameterizedTest
    @ValueSource(strings = {"jpeg", "png"})
    void downloadImage(String extension) throws IOException {
        String container = "container";
        String blobName = "image." + extension;
        when(service.getBlobProperties(container, blobName)).thenReturn(mock(BlobProperties.class));
        when(service.downloadFile(container, blobName)).thenReturn(new byte[]{});

        ResponseEntity<byte[]> responseEntity = fileController.downloadImage(container, blobName);

        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(responseEntity.getHeaders().get("Content-type").get(0)).isEqualTo("image/" + extension);
    }

    @DisplayName("download mp4 video test")
    @Test
    void downloadVideo() throws IOException {
        String container = "container";
        String blobName = "video.mp4";
        when(service.getBlobProperties(container, blobName)).thenReturn(mock(BlobProperties.class));
        when(service.downloadFile(container, blobName)).thenReturn(new byte[]{});

        ResponseEntity<byte[]> responseEntity = fileController.downloadVideo(container, blobName);

        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(responseEntity.getHeaders().get("Content-type")).startsWith("video/mp4");
    }

    @DisplayName("delete file from blob test")
    @Test
    void deleteFile() {
        String container = "container";
        String blobName = "video.mp4";
        when(service.getBlobProperties(container, blobName)).thenReturn(mock(BlobProperties.class));

        ResponseEntity responseEntity = fileController.deleteFile(container, blobName);

        verify(service).deleteFile(container, blobName);
        assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @DisplayName("delete file from blob test failed - file don't exist")
    @Test
    void deleteFileFailed_FileDontExist() {
        String container = "container";
        String blobName = "video.mp4";
        when(service.getBlobProperties(container, blobName)).thenReturn(null);

        ResponseEntity responseEntity = fileController.deleteFile(container, blobName);

        verify(service,never()).deleteFile(container, blobName);
        assertThat(responseEntity.getStatusCode().is4xxClientError()).isTrue();
    }

    @DisplayName("get file url from blob test")
    @Test
    void getFileUrl() {
        String container = "container";
        String blobName = "video.mp4";
        String returnValue = new String();
        when(service.generateSasToken(container,blobName)).thenReturn(returnValue);
        when(service.getAccountName()).thenReturn(returnValue);


        String response = fileController.getFileUrl(container,blobName);

        assertThat(response).isNotBlank();
    }

    @DisplayName("list files in blob test")
    @Test
    void listFiles() {
        String container = "containerName";
        List<String> fileList = new ArrayList<>();
        when(service.listFiles(anyString())).thenReturn(fileList);

        List<String> list = fileController.listFiles(container);

        verify(service).listFiles(anyString());
        assertThat(list).isNotNull();
    }
}