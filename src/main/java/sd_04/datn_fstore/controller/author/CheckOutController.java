package sd_04.datn_fstore.controller.author;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/checkout")
public class CheckOutController {

    /**
     * Trang Thanh Toán
     * URL: http://localhost:8080/checkout
     */
    @GetMapping
    public String viewCheckoutPage(Model model) {
        // Nếu cần lấy thông tin user đang đăng nhập để điền sẵn vào form, bạn thêm logic vào đây
        // VD: User user = userService.getCurrentUser();
        // model.addAttribute("user", user);

        return "view/author/checkout"; // Trỏ đến file templates/view/author/checkout.html
    }

    /**
     * Trang Thông Báo Thành Công
     * URL: http://localhost:8080/checkout/success?id=...
     */
    @GetMapping("/success")
    public String viewSuccessPage(@RequestParam(value = "id", required = false) Integer orderId, Model model) {
        model.addAttribute("orderId", orderId);
        // Trả về trang danh sách đơn hàng hoặc trang cảm ơn riêng
        return "view/author/orders";
    }
}