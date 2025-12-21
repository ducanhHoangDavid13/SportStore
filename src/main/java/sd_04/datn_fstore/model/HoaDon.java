package sd_04.datn_fstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data // @Data đã bao gồm Getter, Setter, ToString, HashCode...
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

    // --- QUAN TRỌNG: Đã khai báo đúng ---
    @Column(name = "tien_khach_dua")
    private BigDecimal tienKhachDua;

    @Column(name = "phi_van_chuyen")
    private BigDecimal phiVanChuyen;

    // --- CÁC MỐI QUAN HỆ (RELATIONSHIPS) ---

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

    // SỬA 1: Bỏ @JsonIgnore để API trả về thông tin địa chỉ cho JS hiển thị
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dia_chi_giao_hang")
    @JsonIgnoreProperties({"hoaDons", "khachHang", "hibernateLazyInitializer", "handler"})
    private DiaChi diaChiGiaoHang;

    // SỬA 2: Giữ lại list này (có @JsonManagedReference là tốt nhất cho API)
    @OneToMany(mappedBy = "hoaDon", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<HoaDonChiTiet> hoaDonChiTiets;

    public BigDecimal getTienKhachDua() {
        return this.tienKhachDua;
    }

    public void setTienKhachDua(BigDecimal tienKhachDua) {
        this.tienKhachDua = tienKhachDua;
    }
    // ĐÃ XÓA: List "chiTietHoaDon" bị thừa (duplicate) gây lỗi.
}