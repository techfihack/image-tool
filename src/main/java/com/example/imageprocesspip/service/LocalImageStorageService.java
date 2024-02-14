package com.example.imageprocesspip.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service("localImageStorageService")
public class LocalImageStorageService implements ImageStorageService {

    @Override
    public void saveImage(String path, byte[] data) throws IOException {
        Files.write(Paths.get(path), data);
    }

    @Override
    public byte[] getImage(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }
}