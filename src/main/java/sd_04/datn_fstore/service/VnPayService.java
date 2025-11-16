package sd_04.datn_fstore.service;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface VnPayService {

    /**
     * SỬA LẠI:
     * Khai báo hàm mới (nhận 4 tham số)
     * @param amount Số tiền (đã nhân 100)
     * @param orderInfo Thông tin đơn hàng
     * @param orderCode Mã hóa đơn
     * @param ipAddress Địa chỉ IP của khách
     * @return URL thanh toán
     */
    String createOrder(long amount, String orderInfo, String orderCode, String ipAddress)
            throws UnsupportedEncodingException; // <-- Phải có "throws" ở đây

    /**
     * Xử lý kết quả VNPAY trả về (IPN / Return)
     */
    int orderReturn(Map<String, String> vnpParams);
}