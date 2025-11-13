package sd_04.datn_fstore.controller; // (Package controller)

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import sd_04.datn_fstore.service.VnPayService;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/vnpay") // Khớp với vnp_ReturnUrl
@RequiredArgsConstructor
public class VnPayController {

    private final VnPayService vnPayService;

    @GetMapping("/payment-result")
    public String paymentResult(HttpServletRequest request, Model model) {
        Map<String, String> vnpParams = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                vnpParams.put(fieldName, fieldValue);
            }
        }

        int result = vnPayService.orderReturn(vnpParams);

        if (result == 0) {
            // TODO: Cập nhật Hóa Đơn và Trừ Tồn Kho
            // (Bạn cần gọi banHangService.confirmPayment(vnpParams.get("vnp_TxnRef")))
            model.addAttribute("message", "Thanh toán VNPAY thành công!");
        } else if (result == 1) {
            model.addAttribute("message", "Lỗi: Chữ ký VNPAY không hợp lệ.");
        } else {
            model.addAttribute("message", "Thanh toán VNPAY thất bại.");
        }

        return "view/vnpay/payment_result"; // Trả về trang thông báo
    }
}