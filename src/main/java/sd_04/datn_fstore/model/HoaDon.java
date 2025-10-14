package sd_04.datn_fstore.model;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "HoaDon")
public class HoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "maHoaDon", length = 100)
    private String maHoaDon;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ngayTao")
    private Date ngayTao;

    @Column(name = "tongTien", precision = 18, scale = 2)
    private BigDecimal tongTien;

    @Column(name = "moTa")
    private String moTa;

    @Column(name = "trangThai")
    private Integer trangThai;

    @Column(name = "hinhThucThanhToan")
    private Integer hinhThucThanhToan;

    @Column(name = "hinhThucBanHang")
    private Integer hinhThucBanHang;

    // Foreign Keys
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idNhanVien")
    private NhanVien nhanVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idKhachHang")
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPhieuGiamGia")
    private PhieuGiamGia phieuGiamGia;

    // Mối quan hệ: Một hóa đơn có nhiều giỏ hàng (?? - theo FK)
    @OneToMany(mappedBy = "hoaDon", fetch = FetchType.LAZY)
    private List<GioHang> gioHangs;

    // Mối quan hệ: Một hóa đơn có nhiều hóa đơn chi tiết
    @OneToMany(mappedBy = "hoaDon", fetch = FetchType.LAZY)
    private List<HoaDonChiTiet> hoaDonChiTiets;
}
