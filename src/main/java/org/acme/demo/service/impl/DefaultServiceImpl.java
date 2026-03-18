package org.acme.demo.service.impl;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.acme.demo.model.AppInfo;
import org.acme.demo.service.DefaultService;

@Service
public class DefaultServiceImpl implements DefaultService {
    @Override
    public AppInfo fetchInfo() {
        return AppInfo.builder()
            .name("Demo API")
            .version("1.0.0")
            .description("Demo API")
            .buildTime(LocalDateTime.now().toString())
            .buildNumber("1")
            .buildVersion("1.0.0")
            .buildDate(LocalDate.now().toString())
            .build();
    }
}
