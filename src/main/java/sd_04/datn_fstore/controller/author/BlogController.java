package sd_04.datn_fstore.controller.author;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BlogController {

    @GetMapping("/blog")
    public String blogPage() {
        return "view/author/block";
    }
}
