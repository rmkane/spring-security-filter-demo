package org.acme.demo.api;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.acme.demo.service.DefaultService;

@RestController
@RequestMapping("/api/default")
@RequiredArgsConstructor
public class DefaultController {

    private final DefaultService defaultService;

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok(defaultService.hello());
    }
}
