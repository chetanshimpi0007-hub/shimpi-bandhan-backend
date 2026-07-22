package com.shimpimilan.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import java.util.List;
import java.util.Arrays;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

@Service
public class LocalStorageServiceImpl implements StorageService {

    private final String uploadDir = "c:\\Users\\Administrator\\Desktop\\shimpi vivah\\backend\\uploads";
    private final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");
    private final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public LocalStorageServiceImpl() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Failed to store empty file.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 5MB limit.");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("File name is invalid.");
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("Only JPG, PNG and WEBP files are allowed.");
        }
    }

    @Override
    public String storeFile(MultipartFile file, String subDirectory) throws IOException {
        validateFile(file);
        
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        String newFilename = UUID.randomUUID().toString() + "." + ext;
        
        Path dirPath = Paths.get(uploadDir, subDirectory != null ? subDirectory : "");
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        
        Path targetPath = dirPath.resolve(newFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        return "/uploads/" + (subDirectory != null ? subDirectory + "/" : "") + newFilename;
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith("/uploads/")) {
            return;
        }
        try {
            String filePath = fileUrl.replace("/uploads/", "");
            Path targetPath = Paths.get(uploadDir).resolve(filePath).normalize();
            Files.deleteIfExists(targetPath);
        } catch (IOException e) {
            // Ignore error on deletion
        }
    }

    @Override
    public String generateThumbnail(MultipartFile file, String subDirectory) throws IOException {
        validateFile(file);
        
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        String newFilename = UUID.randomUUID().toString() + "_thumb." + ext;
        
        Path dirPath = Paths.get(uploadDir, subDirectory != null ? subDirectory : "");
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        
        Path targetPath = dirPath.resolve(newFilename);
        
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            return storeFile(file, subDirectory); // fallback to original
        }
        
        int targetWidth = 200;
        int targetHeight = (int) (originalImage.getHeight() * ((double) targetWidth / originalImage.getWidth()));
        
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(resultingImage, 0, 0, null);
        g2d.dispose();
        
        String formatName = ext.equals("jpg") ? "jpeg" : ext;
        ImageIO.write(outputImage, formatName, targetPath.toFile());
        
        return "/uploads/" + (subDirectory != null ? subDirectory + "/" : "") + newFilename;
    }
}
