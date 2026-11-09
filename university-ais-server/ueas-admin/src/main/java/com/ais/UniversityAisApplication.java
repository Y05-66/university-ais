package com.ais;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 大学教务系统启动类。
 *
 * <p>作为 Spring Boot 应用的入口点，扫描所有子模块的 Bean。
 *
 * <p>扫描范围：
 * <ul>
 *   <li>{@code scanBasePackages = "com.ais"} — 扫描所有 com.ais 包下的组件</li>
 *   <li>{@code @MapperScan("com.ais.*.mapper")} — 扫描所有子模块的 Mapper 接口</li>
 * </ul>
 */
@SpringBootApplication(scanBasePackages = "com.ais")
@MapperScan("com.ais.*.mapper")
public class UniversityAisApplication {

    /**
     * 应用启动入口。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(UniversityAisApplication.class, args);
    }
}
