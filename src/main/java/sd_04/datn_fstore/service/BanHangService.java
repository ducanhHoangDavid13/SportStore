package sd_04.datn_fstore.service;

import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.dto.CreateOrderRequest; // <-- SỬA Ở ĐÂY
import sd_04.datn_fstore.model.HoaDon;

import java.util.List;

public interface BanHangService {

    /**
     * Xử lý thanh toán TIỀN MẶT (Trạng thái 1).
     * Sẽ tạo HĐ, tạo HĐCT, và TRỪ tồn kho.
     */
    @Transactional
    HoaDon thanhToanTienMat(CreateOrderRequest request); // <-- SỬA Ở ĐÂY

    /**
     * Xử lý Hóa đơn CHỜ (Trạng thái 5) hoặc TẠM (Trạng thái 0).
     * Sẽ tạo HĐ, tạo HĐCT, và KHÔNG trừ tồn kho.
     */
    @Transactional
    HoaDon luuHoaDonTam(CreateOrderRequest request); // <-- SỬA Ở ĐÂY

    // (Các hàm khác như confirmPaymentByOrderCode giữ nguyên...)
    List<HoaDon> getDraftOrders();
    HoaDon getDraftOrderDetail(Integer id);
}