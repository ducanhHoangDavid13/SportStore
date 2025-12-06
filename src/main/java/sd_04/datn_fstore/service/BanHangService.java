package sd_04.datn_fstore.service;

import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.dto.CreateOrderRequest;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.model.PhieuGiamGia;

import java.util.List;

public interface BanHangService {

    // --- CÁC HÀM XỬ LÝ CHÍNH ---

    /**
     * Thanh toán tiền mặt (COD / Tại quầy)
     */
    @Transactional
    HoaDon thanhToanTienMat(CreateOrderRequest request);

    /**
     * Lưu hóa đơn chờ (Tạo hóa đơn tạm)
     */
    @Transactional
    HoaDon luuHoaDonTam(CreateOrderRequest request);

    // --- CÁC HÀM LẤY DỮ LIỆU ---

    /**
     * Lấy danh sách hóa đơn chờ
     */
    List<HoaDon> getDraftOrders();

    /**
     * Lấy chi tiết hóa đơn chờ theo MÃ HÓA ĐƠN (String)
     */
    HoaDon getDraftOrderByCode(String maHoaDon);

    // --- CÁC HÀM HỖ TRỢ (HELPER) ---

    /**
     * Trừ số lượng tồn kho
     */
    @Transactional(rollbackFor = Exception.class)
    void decrementInventory(List<CreateOrderRequest.SanPhamItem> items);

    /**
     * Trừ số lượng Voucher (nếu có dùng)
     */
    @Transactional(rollbackFor = Exception.class)
    void decrementVoucher(PhieuGiamGia pgg);
    void deleteByMaHoaDon(String maHoaDon);
}