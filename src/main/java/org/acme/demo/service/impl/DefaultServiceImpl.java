package org.acme.demo.service.impl;

import org.acme.demo.service.DefaultService;
import org.springframework.stereotype.Service;

@Service
public class DefaultServiceImpl implements DefaultService {
    @Override
    public String hello() {
        return "Hello World";
    }
}
