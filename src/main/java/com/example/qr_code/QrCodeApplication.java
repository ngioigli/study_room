package com.example.qr_code;

import org.mybatis.spring.annotation.MapperScan; // 导入这个
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// ！！！关键：添加这个注解，扫描 Mapper 接口所在的包！！！
@MapperScan("com.example.qr_code.mapper")
public class QrCodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(QrCodeApplication.class, args);
    }
}