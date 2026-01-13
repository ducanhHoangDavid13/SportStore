//package sd_04.datn_fstore.controller; // Đặt nó trong package controller web
//
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//import java.util.Map;
//
//@Controller
//public class VnPayViewController {
//
//    /**
//     * Đây là phương thức xử lý vnp_ReturnUrl
//     * Nó sẽ nhận các tham số, thêm vào Model, và trả về trang HTML.
//     */
//    @GetMapping("/vnpay/payment-result-view") // <-- Sử dụng một URL mới
//    public String vnpayReturnPage(
//            @RequestParam Map<String, String> vnpParams,
//            Model model) {
//
//        // 1. Lấy mã phản hồi
//        String vnpResponseCode = vnpParams.get("vnp_ResponseCode");
//
//        // 2. Thêm tất cả các tham số vào Model để Thymeleaf có thể đọc
//        model.addAllAttributes(vnpParams);
//
//        // 3. SỬA LỖI VNP_AMOUNT BỊ NULL
//        //    Đảm bảo vnp_Amount luôn tồn tại, ngay cả khi nó không được gửi
//        String vnpAmountStr = vnpParams.get("vnp_Amount");
//        if (vnpAmountStr == null || vnpAmountStr.isEmpty()) {
//            // Nếu không có, đặt giá trị mặc định là 0
//            model.addAttribute("vnp_Amount", 0L);
//        } else {
//            try {
//                // Nếu có, chuyển đổi nó sang Long (LƯU Ý: VNPAY trả về x100)
//                model.addAttribute("vnp_Amount", Long.parseLong(vnpAmountStr));
//            } catch (NumberFormatException e) {
//                model.addAttribute("vnp_Amount", 0L); // Nếu lỗi, cũng về 0
//            }
//        }
//
//        // 4. Thêm trạng thái thành công/thất bại để dễ dàng hiển thị
//        if ("00".equals(vnpResponseCode)) {
//            model.addAttribute("paymentStatus", "success");
//            model.addAttribute("message", "Giao dịch thành công!");
//        } else {
//            model.addAttribute("paymentStatus", "fail");
//            model.addAttribute("message", "Giao dịch thất bại.");
//        }
//
//        // 5. Trả về tên của file
//        //    (đây chính là file templates/view/vnpay/payment_result.html)
//        return "view/vnpay/payment_result";
//    }
//}