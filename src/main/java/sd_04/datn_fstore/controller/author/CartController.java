package sd_04.datn_fstore.controller.author;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequiredArgsConstructor
public class CartController {
    @GetMapping("cart")
    public String index() {
        return "view/author/cart";
    }
}
