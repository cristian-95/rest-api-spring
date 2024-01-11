package com.cristian.restapi.service;

import com.cristian.restapi.config.FileStorageConfig;
import com.cristian.restapi.exception.FileStorageException;
import com.cristian.restapi.exception.MyFileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageConfig storageConfig) throws IOException {
        Path path = Paths.get(storageConfig.getUploadDir()).normalize();
        System.out.println(path.getFileName().toString());

        this.fileStorageLocation = path;

        try {
            Files.createDirectories(this.fileStorageLocation);

        } catch (Exception e) {
            throw new FileStorageException("Could not create the directory to store the files", e);
        }
    }


    public String storeFile(MultipartFile file) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (filename.contains("..")) {
                throw new FileStorageException("filename contains invalid path sequence " + filename);
            }

            Path targetLocation = this.fileStorageLocation.resolve(filename);

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (Exception e) {
            throw new FileStorageException("Could not store file " + filename + ". please try again", e);
        }
    }

    public Resource loadFileAsResource(String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) return resource;
            else throw new MyFileNotFoundException("File not Found");
        } catch (Exception e) {
            throw new MyFileNotFoundException("File not found: " + filename, e);
        }
    }
}

