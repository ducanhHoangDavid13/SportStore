package sd_04.datn_fstore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "GioHang")
public class GioHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "trangThai")
    private Integer trangThai;

    @Column(name = "tongTien", precision = 18, scale = 2)
    private BigDecimal tongTien;

    @Column(name = "ngayTao")
    private LocalDateTime ngayTao;

    @Column(name = "ngaySua")
    private LocalDateTime ngaySua;

    @Column(name = "soLuong")
    @Min(value = 1, message = "Số lượng trong giỏ hàng phải lớn hơn 0")
    private Integer soLuong;

    @Column(name = "maGioHang", length = 50)
    private String maGioHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSanPham")
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idKhachHang")
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idHoaDon")
    private HoaDon hoaDon;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSanPhamChiTiet")
    private SanPhamChiTiet SanphamChiTiet;
}