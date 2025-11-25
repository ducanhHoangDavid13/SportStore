package sd_04.datn_fstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*; // Import bộ validation
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "SanPham")
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "maSanPham", length = 100, unique = true) // Thêm unique để check trùng DB
    @NotBlank(message = "Mã sản phẩm không được để trống")
    private String maSanPham;

    @Column(name = "tenSanPham", length = 500)
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 500, message = "Tên sản phẩm không được quá 500 ký tự")
    private String tenSanPham;

    // 🟢 Bỏ @Temporal, LocalDateTime tự động map
    @Column(name = "ngayTao")
    private LocalDateTime ngayTao;

    @Column(name = "trangThai")
    private Integer trangThai;

    @Column(name = "moTa", columnDefinition = "NVARCHAR(MAX)") // Hoặc Text tùy DB
    private String moTa;

    // 🟢 Validate Giá tiền
    @Column(name = "giaTien", precision = 18, scale = 2)
    @NotNull(message = "Giá tiền không được để trống")
    @DecimalMin(value = "0.0", message = "Giá tiền phải lớn hơn hoặc bằng 0")
    private BigDecimal giaTien;

    // 🟢 Validate Số lượng
    @Column(name = "soLuong")
    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải lớn hơn hoặc bằng 0")
    private Integer soLuong;

    // ================= MỐI QUAN HỆ =================

    // 1. Hình ảnh
    @OneToMany(mappedBy = "sanPham", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<HinhAnh> hinhAnh;

    // Field phụ để xử lý hiển thị ảnh chính (không lưu DB)
    @Transient
    private String tenHinhAnhChinh;

    // 2. Biến thể (Sản phẩm chi tiết)
    @OneToMany(mappedBy = "sanPham", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SanPhamChiTiet> sanPhamChiTiets;

    // 3. Giỏ hàng
    @OneToMany(mappedBy = "sanPham", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<GioHang> gioHangs;

    // 4. (Tùy chọn) Nếu bạn có liên kết với XuatXu và TheLoai ở bảng cha này
    /*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idXuatXu")
    private XuatXu xuatXu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTheLoai")
    private TheLoai theLoai;
    */
}