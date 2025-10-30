package com.sloyardms.stashbox.security;

import com.sloyardms.stashbox.security.utils.AuthUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("/public")
    public ResponseEntity<String> publicInfo() {
        return ResponseEntity.ok("Hello World");
    }

    @GetMapping("/user")
    public ResponseEntity<String> userInfo() {
        String username = AuthUtils.getCurrentUsername();
        return ResponseEntity.ok(username);
    }

    @GetMapping("/admin")
    public ResponseEntity<String> adminInfo() {
        String username = AuthUtils.getCurrentUsername();
        return ResponseEntity.ok(username);
    }

}
