package com.sloyardms.stashbox.security;

import com.sloyardms.stashbox.security.utils.AuthUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RestController
@RequestMapping("/api/v1/")
public class TestController {

    @GetMapping("/public/test")
    public ResponseEntity<String> publicInfo() {
        return ResponseEntity.ok("Hello World");
    }

    @GetMapping("/user/test")
    public ResponseEntity<String> userInfo() {
        String username = AuthUtils.getCurrentUsername();
        return ResponseEntity.ok(username);
    }

    @GetMapping("/admin/test")
    public ResponseEntity<String> adminInfo() {
        String username = AuthUtils.getCurrentUsername();
        return ResponseEntity.ok(username);
    }

}
