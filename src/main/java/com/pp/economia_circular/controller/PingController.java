package com.pp.economia_circular.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {
        /** Health check: /ping es público (lo usa Render para saber si el servicio está vivo). */
        @GetMapping({"/", "/ping"})
        public String health() {
            return "OK";
        }

}
