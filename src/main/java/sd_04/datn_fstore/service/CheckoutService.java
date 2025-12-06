package sd_04.datn_fstore.service;

import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.dto.*;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.model.PhieuGiamGia;

import java.util.List;

public interface CheckoutService {
    CalculateTotalResponse calculateOrderTotal(CalculateTotalRequest request);
    CheckoutResponse placeOrder(CheckoutRequest request, String clientIp);

    @Transactional(rollbackFor = Exception.class)
    VnPayResponseDTO taoThanhToanVnPay(CheckoutRequest request, String ipAddress);

    @Transactional(rollbackFor = Exception.class)
    void decrementInventory(List<CreateOrderRequest.SanPhamItem> items);

    @Transactional(rollbackFor = Exception.class)
    void decrementVoucher(PhieuGiamGia pgg);
//    HoaDon hoanTatThanhToanVnPay(String maHoaDon, String vnpResponseCode);
//
//    void capNhatTrangThaiVnPayThatBai(String maHoaDon, String vnpResponseCode, String message);
}
