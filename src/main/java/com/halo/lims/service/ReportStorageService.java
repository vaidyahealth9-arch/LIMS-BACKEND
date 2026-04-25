package com.halo.lims.service;

public interface ReportStorageService {
    String uploadFile(String objectName, byte[] content, String contentType);
    byte[] downloadFile(String storedReference);
    void deleteFile(String storedReference);
}
