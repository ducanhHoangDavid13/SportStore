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

    // --- Mối quan hệ Many-to-One ---

    // ⭐️ KHẮC PHỤC LỖI HIBERNATE: Thêm trường sanPham để khớp với mappedBy="sanPham" trong SanPham.java
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSanPham", insertable = false, updatable = false)
    private SanPham sanPham;

    // ⭐️ Thêm mối quan hệ với SanPhamChiTiet để truy cập MauSac, KichThuoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSanPhamChiTiet", insertable = false, updatable = false)
    private SanPhamChiTiet sanPhamChiTiet;

    // --- Khóa Ngoại (Foreign Key) - Giữ lại theo DDL SQL ---

    @Column(name = "idSanPham")
    private Integer idSanPham;

    @Column(name = "idKhachHang")
    private Integer idKhachHang;

    @Column(name = "idHoaDon")
    private Integer idHoaDon;

    @Column(name = "idSanPhamChiTiet")
    private Integer idSanPhamChiTiet;
}