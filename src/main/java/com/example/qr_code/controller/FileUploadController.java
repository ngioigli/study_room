package com.example.qr_code.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
public class FileUploadController {

    // 从配置文件 application.properties 中读取图片保存路径
    // 这样方便修改，而不用改代码
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    // 这是一个非常简单的安全令牌，防止任何人都能上传文件
    private final String SECRET_TOKEN = "hbut12345678";

    @PostMapping("/upload/qrcode") // 定义API的访问路径为 /upload/qrcode
    public ResponseEntity<String> handleFileUpload(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-Auth-Token") String token) { // 从请求头中获取安全令牌

        // 1. 安全校验
        if (token == null || !token.equals(SECRET_TOKEN)) {
            return new ResponseEntity<>("Unauthorized: Invalid Token", HttpStatus.UNAUTHORIZED);
        }

        // 2. 检查文件是否为空
        if (file.isEmpty()) {
            return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
        }

        try {
            // 3. 构造保存路径，这里我们硬编码文件名，因为我们总是希望覆盖旧的二维码
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs(); // 如果目录不存在，则创建
            }
            
            // 目标文件路径
            File dest = new File(directory.getAbsolutePath() + File.separator + "dynamic_qrcode.jpg");

            // 4. 将上传的文件保存到服务器的目标路径
            file.transferTo(dest);

            // 5. 返回成功响应
            return new ResponseEntity<>("File uploaded successfully: " + dest.getAbsolutePath(), HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("File upload failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}