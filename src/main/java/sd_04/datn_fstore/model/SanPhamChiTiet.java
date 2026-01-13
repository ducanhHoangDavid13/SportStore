package sd_04.datn_fstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "SanPhamChiTiet")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SanPhamChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "maSanPhamChiTiet")
    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    private String maSanPhamChiTiet;

    @Column(name = "giaTien", precision = 18, scale = 2)
    @NotNull(message = "Giá tiền không được để trống")
    @DecimalMin(value = "0.0", message = "Giá tiền phải lớn hơn hoặc bằng 0")
    private BigDecimal giaTien;

    @Column(name = "soLuong")
    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải lớn hơn hoặc bằng 0")
    private Integer soLuong;

    @Column(name = "moTa")
    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    private String moTa;

    @Column(name = "trangThai")
    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai;

    // =========================================================
    // KHAI BÁO CÁC CỘT ID THÔ (ĐÃ BỔ SUNG 6 TRƯỜNG CÒN THIẾU)
    // Service sẽ ghi giá trị vào các trường này để khắc phục lỗi NULL.
    // =========================================================
    @Column(name = "idKichThuoc")
    private Integer idKichThuoc;

    @Column(name = "idPhanLoai")
    private Integer idPhanLoai;

    @Column(name = "idXuatXu")
    private Integer idXuatXu;

    @Column(name = "idChatLieu")
    private Integer idChatLieu;

    @Column(name = "idMauSac")
    private Integer idMauSac;

    @Column(name = "idTheLoai")
    private Integer idTheLoai;

    @Column(name = "idSanPham")
    private Integer idSanPham; // Đã có sẵn

    // =========================================================
    // Mối quan hệ Many-to-One
    // GIỮ NGUYÊN CÁC LỖI insertable/updatable = false (theo yêu cầu)
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idKichThuoc", insertable = false, updatable = false)
    private KichThuoc kichThuoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPhanLoai", insertable = false, updatable = false)
    private PhanLoai phanLoai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idXuatXu", insertable = false, updatable = false)
    private XuatXu xuatXu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idChatLieu", insertable = false, updatable = false)
    private ChatLieu chatLieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idMauSac", insertable = false, updatable = false)
    private MauSac mauSac;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTheLoai", insertable = false, updatable = false)
    private TheLoai theLoai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSanPham", insertable = false, updatable = false)
    private SanPham sanPham;

    // Mối quan hệ List
    @JsonIgnore
    @OneToMany(mappedBy = "sanPhamChiTiet", fetch = FetchType.LAZY)
    private List<HoaDonChiTiet> hoaDonChiTiets;

    public BigDecimal getDonGia() {
        return this.giaTien;
    }
}