package com.pp.economia_circular.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {

    @PutMapping("/echo")
    public ResponseEntity<?> echo(@RequestBody(required = true) Object body) {
        System.out.println(">>> Llego body: " + body);
        return ResponseEntity.ok(body);
    }
}
