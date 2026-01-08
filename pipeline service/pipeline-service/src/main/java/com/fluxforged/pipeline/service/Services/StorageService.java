package com.fluxforged.pipeline.service.Services;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageService {
    String saveFile(MultipartFile file);

    String saveFromStream(InputStream inputStream, String fileName);

    String  saveFromUrl(String url);
}
