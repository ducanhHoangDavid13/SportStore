package sd_04.datn_fstore.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderRequest {

    // 1. Thông tin đơn hàng
    private Integer nhanVienId;
    private Integer khachHangId;
    private Integer phieuGiamGiaId;

    private String maHoaDon;

    // JS gửi lên: phuongThucThanhToan (String/Int) HOẶC hinhThucThanhToan (Int)
    private String phuongThucThanhToan;

    // ✅ THÊM: Hứng số 1 (Tiền mặt) hoặc 2 (CK) từ JS gửi lên để lưu vào DB chuẩn hơn
    private Integer hinhThucThanhToan;

    // 2. Thông tin tiền
    private BigDecimal tongTien;
    private BigDecimal tienGiamGia;

    private BigDecimal tienKhachDua;

    // 3. Danh sách sản phẩm
    private List<SanPhamItem> danhSachSanPham;

    @Data
    public static class SanPhamItem {
        private Integer sanPhamChiTietId;
        private Integer soLuong;
        private BigDecimal donGia;

        public SanPhamItem(Integer sanPhamChiTietId, Integer soLuong, BigDecimal donGia) {
            this.sanPhamChiTietId = sanPhamChiTietId;
            this.soLuong = soLuong;
            this.donGia = donGia;
        }

        // ❌ XÓA: private BigDecimal tienKhachDua; (Sai vị trí)
    }
}