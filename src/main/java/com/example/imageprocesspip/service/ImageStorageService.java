package com.example.imageprocesspip.service;

import java.io.IOException;

public interface ImageStorageService {
    void saveImage(String path, byte[] data) throws IOException;
    byte[] getImage(String path) throws IOException;
}