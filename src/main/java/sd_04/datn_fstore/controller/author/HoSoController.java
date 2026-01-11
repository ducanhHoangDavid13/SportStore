package sd_04.datn_fstore.controller.author;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
public class HoSoController {
    @GetMapping("/profile")
    public String getLoginPage() {
        return "view/author/profile";
    }
    @GetMapping("/orders-user")
    public String getLoginPageOderUser() {
        return "view/author/orders";
    }
    @GetMapping("/address")
    public String getLoginPageAddress() {
        return "view/author/address";
    }
    
}
