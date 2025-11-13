package sd_04.datn_fstore.service;

import jakarta.servlet.http.HttpServletRequest;
import sd_04.datn_fstore.dto.CreateOrderRequest;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface VnPayService {

    /**
     * Tạo URL thanh toán VNPAY từ DTO đơn hàng
     */
    String createOrder(CreateOrderRequest request, HttpServletRequest httpReq)
            throws UnsupportedEncodingException;

    /**
     * Xử lý kết quả VNPAY trả về (IPN / Return)
     */
    int orderReturn(Map<String, String> vnpParams);
}