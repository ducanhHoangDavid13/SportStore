package sd_04.datn_fstore.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class GioHangDTO {

    private Integer id;
    private Integer idSanPham;
    private Integer idSanPhamChiTiet;
    private String tenSanPham;
    private String tenMau;
    private String tenKichCo;
    private BigDecimal donGia;
    private Integer soLuong;
    private String tenHinhAnh;

    // ⭐️ ADD BACK: Trường soLuongTon (Lấy từ SanPhamChiTiet)
    private Integer soLuongTon;

    // Phương thức tính thành tiền cho một mục giỏ hàng
    public BigDecimal getThanhTien() {
        if (donGia == null || soLuong == null) {
            return BigDecimal.ZERO;
        }
        return this.donGia.multiply(BigDecimal.valueOf(this.soLuong));
    }
}