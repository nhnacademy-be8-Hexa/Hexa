package com.nhnacademy.hello.image.impl;

import com.nhnacademy.hello.exception.objectstorage.DeleteFailException;
import com.nhnacademy.hello.exception.LocalImageException;
import com.nhnacademy.hello.exception.objectstorage.SaveFailException;
import com.nhnacademy.hello.image.ImageStore;
import com.nhnacademy.hello.image.tool.FileExtensionVaildation;
import com.nhnacademy.hello.service.StorageService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@Profile("objectStorage")
public class ObjectStorageStore implements ImageStore {

    private final StorageService storageService;

    public ObjectStorageStore(StorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public boolean saveImages(List<MultipartFile> files, String fileName) throws LocalImageException {

        FileExtensionVaildation fileExtensionVaildation = new FileExtensionVaildation(files);
        fileExtensionVaildation.vaildate();

        StorageService.UploadResult uploadResult = storageService.uploadFiles(files, fileName);
        if (!uploadResult.getFailedFiles().isEmpty()) {
            throw new SaveFailException("Some files failed to upload: " + uploadResult.getFailedFiles());
        }
        return true;
    }

    @Override
    public boolean deleteImages(String fileName) throws LocalImageException {
        boolean success = storageService.deleteObject(fileName);
        if (!success) {
            throw new DeleteFailException("Failed to delete image: " + fileName);
        }
        return true;
    }

    @Override
    public List<String> getImage(String fileName) {
        return storageService.getImage(fileName);
    }
}