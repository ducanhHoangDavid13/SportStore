package sd_04.datn_fstore.service;

// KHÔNG CẦN @Service hay @RequiredArgsConstructor ở đây
public interface KhoService {

    /**
     * Trừ tồn kho khi bán. Sẽ ném Exception nếu hết hàng.
     */
    void truTonKho(Integer sanPhamChiTietId, Integer soLuongBan);

    /**
     * Hoàn tồn kho khi đơn hàng bị hủy.
     */
    void hoanTonKho(Integer sanPhamChiTietId, Integer soLuongTra);
}