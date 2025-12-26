package sd_04.datn_fstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@ControllerAdvice
@RequestMapping("/error-v1")
public class ErrorPageController {

    @GetMapping("/401")
    public String error401() {
        return "error/401";
    }

    @GetMapping("/403")
    public String error403() {
        return "error/403";
    }
}

