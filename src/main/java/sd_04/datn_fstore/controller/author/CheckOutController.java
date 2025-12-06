package sd_04.datn_fstore.controller.author;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sd_04.datn_fstore.model.KhachHang;
import sd_04.datn_fstore.service.KhachhangService;

import java.security.Principal;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckOutController {

    private final KhachhangService khachhangService;

    /**
     * Trang Thanh Toán
     */
    @GetMapping
    public String viewCheckoutPage(Model model, Principal principal) {
//        if (principal != null) {
//            String email = principal.getName();
//            // Lấy thông tin khách hàng
//            KhachHang kh = khachhangService.findByEmail(email);
//
//            if (kh != null) {
//                model.addAttribute("user", kh);
//                // CHÚ Ý: Kiểm tra lại tên hàm get list địa chỉ trong Model KhachHang của bạn
//                // Ví dụ: getDiaChiList() hoặc getAddresses()
//                // model.addAttribute("addresses", kh.getDiaChiList());
//            }
//        }
        return "view/author/checkout";
    }

    /**
     * Trang Thông Báo Thành Công
     */
    @GetMapping("/success")
    public String viewSuccessPage(@RequestParam(value = "id", required = false) Integer orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "view/author/orders";
    }
}