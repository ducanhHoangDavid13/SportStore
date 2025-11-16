package sd_04.datn_fstore.service;

import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.dto.CreateOrderRequest;
import sd_04.datn_fstore.dto.VnPayResponseDTO; // <-- THÊM IMPORT
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.model.PhieuGiamGia; // <-- THÊM IMPORT

import java.util.List;

public interface BanHangService {

    // --- CÁC HÀM CŨ (Giữ nguyên) ---
    @Transactional
    HoaDon thanhToanTienMat(CreateOrderRequest request);

    @Transactional
    HoaDon luuHoaDonTam(CreateOrderRequest request);

    List<HoaDon> getDraftOrders();
    HoaDon getDraftOrderDetail(Integer id);

    // --- 🚀 THÊM 3 HÀM MỚI VÀO ĐÂY ---

    /**
     * THÊM MỚI: Luồng VNPAY
     */
    @Transactional(rollbackFor = Exception.class)
    VnPayResponseDTO taoThanhToanVnPay(CreateOrderRequest request, String ipAddress);

    /**
     * THÊM MỚI: Interface để trừ tồn kho
     */
    @Transactional(rollbackFor = Exception.class)
    void decrementInventory(List<CreateOrderRequest.Item> items);

    /**
     * THÊM MỚI: Interface để trừ lượt dùng voucher
     */
    @Transactional(rollbackFor = Exception.class)
    void decrementVoucher(PhieuGiamGia pgg);
}