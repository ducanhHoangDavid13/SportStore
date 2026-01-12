package sd_04.datn_fstore.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sd_04.datn_fstore.service.HoaDonService;
import sd_04.datn_fstore.service.VnPayService;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/vnpay") // Khớp với URL trên trình duyệt bạn đã chụp ảnh (localhost:8080/vnpay/...)
@RequiredArgsConstructor
public class VnPayController {

    private final VnPayService vnPayService;
    private final HoaDonService hoaDonService;

    // Các trạng thái hóa đơn (Theo quy ước của bạn)
    private static final int TT_CHO_THANH_TOAN = 6; // Hoặc 0, tùy quy ước
    private static final int TT_DA_XAC_NHAN = 1;    // Đã thanh toán thành công
    private static final int TT_DA_HUY = 5;         // Đã hủy

    @GetMapping("/payment-result-view") // Khớp với cấu hình Return URL trên VNPAY
    public String paymentResult(HttpServletRequest request, Model model) {
        // 1. Lấy toàn bộ tham số từ VNPAY trả về
        Map<String, String> vnpParams = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                vnpParams.put(fieldName, fieldValue);
            }
        }

        // 2. Kiểm tra chữ ký bảo mật (Checksum)
        // Lưu ý: Đảm bảo hàm orderReturn của bạn trả về 1 nếu hash đúng, ngược lại là sai
        int checkSignature = vnPayService.orderReturn(vnpParams);

        // 3. Lấy các thông tin quan trọng
        String orderIdStr = request.getParameter("vnp_TxnRef");
        String responseCode = request.getParameter("vnp_ResponseCode");
        String transactionStatus = request.getParameter("vnp_TransactionStatus");
        String amountStr = request.getParameter("vnp_Amount");

        Long orderId = null;
        try {
            orderId = Long.parseLong(orderIdStr);
        } catch (NumberFormatException e) {
            System.err.println("Lỗi parse ID hóa đơn: " + orderIdStr);
        }

        String msg = "";
        String iconClass = "";
        String paymentStatus = "";

        // 4. Xử lý Logic trạng thái
        if (checkSignature == 1) { // Chữ ký hợp lệ
            if ("00".equals(responseCode)) {
                // --- TRƯỜNG HỢP: THANH TOÁN THÀNH CÔNG ---
                // Cập nhật trạng thái: Đã xác nhận (1)
                if (orderId != null) {
                    hoaDonService.updatePaymentStatus(orderId, TT_DA_XAC_NHAN);
                }
                msg = "Thanh toán thành công! Đơn hàng đã được xác nhận.";
                iconClass = "success";
                paymentStatus = "success";

            } else if ("24".equals(responseCode)) {
                // --- TRƯỜNG HỢP: KHÁCH HỦY GIAO DỊCH (Tại màn hình VNPAY) ---
                // Cập nhật trạng thái: Hủy (5)
                if (orderId != null) {
                    hoaDonService.updatePaymentStatus(orderId, TT_DA_HUY);
                }
                msg = "Bạn đã hủy giao dịch thanh toán.";
                iconClass = "warning";
                paymentStatus = "cancel";

            } else {
                // --- TRƯỜNG HỢP: LỖI HOẶC CHƯA THANH TOÁN XONG (FAIL) ---
                // Giữ nguyên hoặc đưa về Chờ thanh toán (6)
                if (orderId != null) {
                    hoaDonService.updatePaymentStatus(orderId, TT_CHO_THANH_TOAN);
                }
                msg = "Giao dịch thất bại hoặc chưa hoàn tất.";
                iconClass = "error";
                paymentStatus = "fail";
            }
        } else {
            // --- TRƯỜNG HỢP: SAI CHỮ KÝ (CÓ DẤU HIỆU GIẢ MẠO) ---
            msg = "Cảnh báo: Dữ liệu trả về không hợp lệ (Sai chữ ký).";
            iconClass = "error";
            paymentStatus = "fail";
        }

        // 5. Xử lý hiển thị số tiền (Chia 100 vì VNPAY nhân 100)
        long displayAmount = 0;
        if (amountStr != null && !amountStr.isEmpty()) {
            displayAmount = Long.parseLong(amountStr) / 100;
        }

        // 6. Truyền dữ liệu ra View
        model.addAttribute("message", msg);
        model.addAttribute("icon", iconClass);
        model.addAttribute("paymentStatus", paymentStatus);
        model.addAttribute("orderId", orderIdStr);
        model.addAttribute("totalPrice", displayAmount);

        // Thêm lại các tham số khác để debug nếu cần
        model.addAllAttributes(vnpParams);

        return "view/vnpay/payment_result";
    }
}