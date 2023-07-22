package com.value.filestorage;

import com.azure.storage.blob.models.BlobProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private AzureBlobServiceImpl service;

    @InjectMocks
    private FileController fileController;

    @DisplayName("upload file to blob test succeed")
    @Test
    void uploadFileSucceed() throws IOException {
        String container = "container";
        String blobName = "blobName";
        given(service.blobExists(anyString(), anyString())).willReturn(false);

        fileController.uploadFile(container, blobName, mock(MultipartFile.class));

        verify(service, times(1)).uploadFile(anyString(), anyString(), any());

    }

    @DisplayName("download file from blob test")
    @Test
    void downloadFile() throws IOException {
        given(service.getBlobProperties(anyString(), anyString())).willReturn(mock(BlobProperties.class));
        given(service.downloadFile(anyString(), anyString())).willReturn(new byte[]{});

        ResponseEntity<byte[]> responseEntity = fileController.downloadFile("container", "fileName");

        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @DisplayName("download image jpg, jpeg or png test")
    @Test
    void downloadImage() throws IOException {
        given(service.getBlobProperties(anyString(), anyString())).willReturn(mock(BlobProperties.class));
        given(service.downloadFile(anyString(), anyString())).willReturn(new byte[]{});

        ResponseEntity<byte[]> responseEntity = fileController.downloadFile("container", "fileName");

        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @DisplayName("download mp4 video test")
    @Test
    void downloadVideo() {
    }

    @DisplayName("delete file from blob test")
    @Test
    void deleteFile() {
    }

    @DisplayName("get file url from blob test")
    @Test
    void getFileUrl() {
    }

    @DisplayName("list files in blob test")
    @Test
    void listFiles() {
        List<String> fileList = new ArrayList<>();
        given(service.listFiles(anyString())).willReturn(fileList);

        List<String> list = fileController.listFiles("containerName");

        then(service).should().listFiles(anyString());
        assertThat(list).isNotNull();
    }
}