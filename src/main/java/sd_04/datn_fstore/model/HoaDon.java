package sd_04.datn_fstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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

    // 🔹 Liên kết tới Nhân viên
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idNhanVien")
    @JsonIgnoreProperties({"hoaDons", "hibernateLazyInitializer", "handler"})
    private NhanVien nhanVien;

    // 🔹 Liên kết tới Khách hàng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idKhachHang")
    @JsonIgnoreProperties({"hoaDons", "hibernateLazyInitializer", "handler"})
    private KhachHang khachHang;

    // 🔹 Liên kết tới Phiếu giảm giá
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPhieuGiamGia")
    @JsonIgnoreProperties({"hoaDons", "hibernateLazyInitializer", "handler"})
    private PhieuGiamGia phieuGiamGia;

    // 🔹 Một hóa đơn có nhiều giỏ hàng
    @OneToMany(mappedBy = "hoaDon", fetch = FetchType.LAZY)
    @JsonIgnore // 🧩 ẩn danh sách giỏ hàng để tránh vòng lặp và dữ liệu nặng
    private List<GioHang> gioHangs;

    // 🔹 Một hóa đơn có nhiều chi tiết hóa đơn
    @OneToMany(mappedBy = "hoaDon", fetch = FetchType.LAZY)
    @JsonIgnore // 🧩 tránh vòng lặp và lỗi serialization
    private List<HoaDonChiTiet> hoaDonChiTiets;
}
