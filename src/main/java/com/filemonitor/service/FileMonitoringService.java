package com.filemonitor.service;

import java.io.IOException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@AllArgsConstructor
public class FileMonitoringService {
    private final WatchService watchService;

    private final String postRequestPath;



    @Async
    @PostConstruct
    public void launchMonitoring() {
        log.info("Start Monitoring");
        try {
            WatchKey key;
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    log.info("Event kind: {}; File affected: {}", event.kind(), event.context());
                    doReload();
                }
                key.reset();
            }
        } catch (InterruptedException e) {
            log.warn("interrupted exception for monitoring service");
        }
    }

    @PreDestroy
    public void stopMonitoring() {
        log.info("Stop Monitoring");

        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                log.error("exception while closing the monitoring service");
            }
        }
    }

    private static RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
        messageConverters.add(converter);
        restTemplate.setMessageConverters(messageConverters);
        return restTemplate;
    }


    private void doReload(){
        RestTemplate restTemplate = getRestTemplate();
        try{
            restTemplate.postForObject(postRequestPath, null, Void.class);
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }

}
