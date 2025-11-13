package sd_04.datn_fstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal; // <-- Import

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "HoaDonChiTiet")
public class HoaDonChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "maHoaDonChiTiet", length = 50)
    private String maHoaDonChiTiet;

    @Column(name = "soLuong")
    private Integer soLuong;

    @Column(name = "moTa")
    private String moTa;

    @Column(name = "trangThai")
    private Integer trangThai;

    // ----- CÁC CỘT THÊM VÀO ĐỂ FIX LỖI -----
    @Column(name = "don_gia", precision = 18, scale = 2)
    private BigDecimal donGia; // <-- THÊM CỘT NÀY

    @Column(name = "thanh_tien", precision = 18, scale = 2)
    private BigDecimal thanhTien; // <-- THÊM CỘT NÀY
    // ------------------------------------

    // Foreign Keys
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSanPhamChiTiet")
    private SanPhamChiTiet sanPhamChiTiet;

    // ----- XÓA LIÊN KẾT THỪA TỚI PGG -----
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "idPhieuGiamGia")
    // private PhieuGiamGia phieuGiamGia; // <-- XÓA CÁI NÀY

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idHoaDon")
    private HoaDon hoaDon;
}