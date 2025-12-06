package sd_04.datn_fstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
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

    @Column(name = "maSanPham", length = 100, unique = true)
    @NotBlank(message = "Mã sản phẩm không được để trống")
    private String maSanPham;

    @Column(name = "tenSanPham", length = 500)
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 500, message = "Tên sản phẩm không được quá 500 ký tự")
    private String tenSanPham;

    @Column(name = "ngayTao")
    private LocalDateTime ngayTao;

    @Column(name = "trangThai")
    private Integer trangThai;

    @Column(name = "moTa", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @Column(name = "giaTien", precision = 18, scale = 2)
    @NotNull(message = "Giá tiền không được để trống")
    @DecimalMin(value = "0.0", message = "Giá tiền phải lớn hơn hoặc bằng 0")
    private BigDecimal giaTien;

    @Column(name = "soLuong")
    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải lớn hơn hoặc bằng 0")
    private Integer soLuong;

    // ================= MỐI QUAN HỆ =================

    // 1. Hình ảnh: SanPham (One) -> HinhAnh (Many)
    @OneToMany(mappedBy = "sanPham", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private List<HinhAnh> hinhAnh;

    // Field phụ để xử lý hiển thị ảnh chính (không lưu DB)
    @Transient
    private String tenHinhAnhChinh;

    // 2. Biến thể (Sản phẩm chi tiết): SanPham (One) -> SanPhamChiTiet (Many)
    @OneToMany(mappedBy = "sanPham", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private List<SanPhamChiTiet> sanPhamChiTiets;

    // 3. Giỏ hàng:
    // ⚠️ ĐÃ XÓA MỐI QUAN HỆ TRỰC TIẾP TỪ SANPHAM -> GIOHANG.
    // Lỗi 'mappedBy' xảy ra do GioHang giờ liên kết với SanPhamChiTiet.
    // Nếu bạn cần truy vấn giỏ hàng theo Sản phẩm, hãy làm qua SanPhamChiTiet.

    // ... (Các mối quan hệ tùy chọn khác nếu có)
}