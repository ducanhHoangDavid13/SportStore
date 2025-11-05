package sd_04.datn_fstore.service;

import sd_04.datn_fstore.model.HoaDon;
import java.util.Map;

public interface BanHangService {

    /**
     * Xử lý nghiệp vụ "Hoàn tất Thanh toán" tại POS.
     * Sẽ trừ tồn kho, tính toán, và tạo hóa đơn.
     */
    HoaDon createPosPayment(Map<String, Object> requestBody);

    /**
     * Xử lý nghiệp vụ "Lưu Tạm" tại POS.
     * Sẽ KHÔNG trừ tồn kho và tạo hóa đơn (Hóa đơn Tạm = Giỏ hàng đã lưu).
     */
    HoaDon saveDraftOrder(Map<String, Object> requestBody);
}