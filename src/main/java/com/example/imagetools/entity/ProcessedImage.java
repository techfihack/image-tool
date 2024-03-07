package com.example.imagetools.entity;

public class ProcessedImage {

    String fileName;
    byte[] fileImage;
    private String taskUUID;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getFileImage() {
        return fileImage;
    }

    public String getTaskUUID() {
        return taskUUID;
    }

    public void setTaskUUID(String taskUUID) {
        this.taskUUID = taskUUID;
    }


    public void setFileImage(byte[] fileImage) {
        this.fileImage = fileImage;
    }

    public ProcessedImage(String fileName, byte[] fileImage){
        this.fileName = fileName;
        this.fileImage = fileImage;
    }
}
