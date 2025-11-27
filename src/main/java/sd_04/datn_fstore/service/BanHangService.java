package sd_04.datn_fstore.service;

import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.dto.CreateOrderRequest;
import sd_04.datn_fstore.dto.VnPayResponseDTO;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.model.PhieuGiamGia;

import java.util.List;

public interface BanHangService {

    // --- CÁC HÀM CŨ (Giữ nguyên) ---
    @Transactional
    HoaDon thanhToanTienMat(CreateOrderRequest request);

    @Transactional
    HoaDon luuHoaDonTam(CreateOrderRequest request);

    List<HoaDon> getDraftOrders();
    HoaDon getDraftOrderDetail(Integer id);

    // --- 🚀 CÁC HÀM MỚI (ĐÃ CẬP NHẬT) ---

    /**
     * Tạo thanh toán VNPay
     */
    @Transactional(rollbackFor = Exception.class)
    VnPayResponseDTO taoThanhToanVnPay(CreateOrderRequest request, String ipAddress);

    /**
     * SỬA LỖI: Cập nhật tham số từ List<Item> thành List<SanPhamItem>
     * để khớp với DTO CreateOrderRequest mới.
     */
    @Transactional(rollbackFor = Exception.class)
    void decrementInventory(List<CreateOrderRequest.SanPhamItem> items);

    /**
     * Trừ voucher
     */
    @Transactional(rollbackFor = Exception.class)
    void decrementVoucher(PhieuGiamGia pgg);
}