package sd_04.datn_fstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "HoaDon")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class HoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "maHoaDon", length = 100)
    private String maHoaDon;

    @Column(name = "ngayTao")
    private LocalDateTime ngayTao;

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

    @Column(name = "tien_giam_gia")
    private BigDecimal tienGiamGia;

    @Column(name = "tong_tien_sau_giam")
    private BigDecimal tongTienSauGiam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idNhanVien")
    @JsonIgnoreProperties({"hoaDons", "hibernateLazyInitializer", "handler"})
    private NhanVien nhanVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idKhachHang")
    @JsonIgnoreProperties({"hoaDons", "hibernateLazyInitializer", "handler"})
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPhieuGiamGia")
    @JsonIgnoreProperties({"hoaDons", "hibernateLazyInitializer", "handler"})
    private PhieuGiamGia phieuGiamGia;

    @OneToMany(mappedBy = "hoaDon", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<HoaDonChiTiet> hoaDonChiTiets;
    @Column(name = "phi_van_chuyen")
    private BigDecimal phiVanChuyen;

    // 2. Thêm liên kết đến bảng Địa Chỉ
    // (Lưu ý: Đây là object DiaChi, không phải String)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dia_chi_giao_hang")
    private DiaChi diaChiGiaoHang;
}