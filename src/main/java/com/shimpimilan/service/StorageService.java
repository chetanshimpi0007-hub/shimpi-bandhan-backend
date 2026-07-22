package com.shimpimilan.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface StorageService {
    String storeFile(MultipartFile file, String subDirectory) throws IOException;
    void deleteFile(String fileUrl);
    String generateThumbnail(MultipartFile file, String subDirectory) throws IOException;
}
