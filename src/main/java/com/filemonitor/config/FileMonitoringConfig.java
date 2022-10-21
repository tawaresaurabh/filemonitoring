package com.filemonitor.config;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FileMonitoringConfig {


    @Value("${filemonitor.folder.to.monitor}")
    private String folderPath;

    @Value("${filemonitor.post.api.on.file.change.path}")
    private String postRequestPath;

    @Bean
    public WatchService watchService() {
        log.debug("MONITORING_FOLDER: {}", folderPath);
        WatchService watchService = null;
        boolean isInitialFolder = false;
        boolean isInitialPostUrl = false;


        if("c:/folder/to/monitor/path".equalsIgnoreCase(folderPath)){
            log.error("Please change the value of  filemonitor.folder.to.monitor in the application.properties file to valid path");
            isInitialFolder = true;
        }

        if("http://localhost:port/some/post/url".equalsIgnoreCase(postRequestPath)){
            log.error("Please change  the value of filemonitor.post.api.on.file.change.path in the application.properties file to valid post url");
            isInitialPostUrl = true;
        }

        if(isInitialFolder || isInitialPostUrl){
            throw new IllegalArgumentException("Please correct the configuration");
        }

        try {
            watchService = FileSystems.getDefault().newWatchService();

            WatchService finalWatchService = watchService;

            if (!Files.isDirectory(Path.of(folderPath))) {
                log.error("No such directory found. {}", folderPath);
                throw new IllegalArgumentException("Folder not found , Please check the path");
            }


            Files.walkFileTree(Path.of(folderPath) , new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    dir.register(finalWatchService, ENTRY_MODIFY);
                    return FileVisitResult.CONTINUE;
                }
            });


        } catch (IOException e) {
            log.error("exception for watch service creation:", e);
        }
        return watchService;
    }

    @Bean
    public String postRequestPath(){
        return postRequestPath;
    }
}
