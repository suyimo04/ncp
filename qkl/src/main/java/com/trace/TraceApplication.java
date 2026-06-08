package com.trace;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 食品溯源系统启动类
 */
@SpringBootApplication
@MapperScan("com.trace.mapper")
@EnableAsync
public class TraceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TraceApplication.class, args);
        System.out.println("========== 食品溯源系统启动成功 ==========");
    }
}
