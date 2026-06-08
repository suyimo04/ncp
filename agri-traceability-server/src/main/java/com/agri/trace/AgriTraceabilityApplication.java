package com.agri.trace;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.agri.trace.mapper")
@SpringBootApplication
public class AgriTraceabilityApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgriTraceabilityApplication.class, args);
    }
}
