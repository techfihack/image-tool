package com.example.imageprocesspip.entity;

public class ProcessedImage {

    String fileName;
    byte[] fileImage;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getFileImage() {
        return fileImage;
    }

    public void setFileImage(byte[] fileImage) {
        this.fileImage = fileImage;
    }

    public ProcessedImage(String fileName, byte[] fileImage){
            this.fileName = fileName;
            this.fileImage = fileImage;
    }
}
