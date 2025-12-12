package sd_04.datn_fstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*; // Import bộ validation
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
    // Validate Giá tiền: Không null, thấp nhất là 0
    @Column(name = "giaTien", precision = 18, scale = 2)
    @NotNull(message = "Giá tiền không được để trống")
    @DecimalMin(value = "0.0", message = "Giá tiền phải lớn hơn hoặc bằng 0")
    private BigDecimal giaTien;

    // Validate Số lượng: Không null, thấp nhất là 0
    @Column(name = "soLuong")
    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải lớn hơn hoặc bằng 0")
    private Integer soLuong;

    // Validate Mô tả: Có thể tùy chọn, nhưng nếu nhập thì không quá dài
    @Column(name = "moTa")
    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    private String moTa;

    // Validate Trạng thái: Không null
    @Column(name = "trangThai")
    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai;

    // =========================================================
    // Validate Khóa ngoại: Bắt buộc phải có đối tượng liên kết
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idKichThuoc")
    @NotNull(message = "Kích thước không được để trống")
    private KichThuoc kichThuoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPhanLoai")
    // @NotNull(message = "Phân loại không được để trống") // Bỏ comment nếu bắt buộc
    private PhanLoai phanLoai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idXuatXu")
    @NotNull(message = "Xuất xứ không được để trống")
    private XuatXu xuatXu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idChatLieu")
    @NotNull(message = "Chất liệu không được để trống")
    private ChatLieu chatLieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idMauSac")
    @NotNull(message = "Màu sắc không được để trống")
    private MauSac mauSac;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTheLoai")
    @NotNull(message = "Thể loại không được để trống")
    private TheLoai theLoai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSanPham")
    @NotNull(message = "Sản phẩm gốc không được để trống")
    private SanPham sanPham;

    // Mối quan hệ List (Không cần validate @NotNull)
    @JsonIgnore
    @OneToMany(mappedBy = "sanPhamChiTiet", fetch = FetchType.LAZY)
    private List<HoaDonChiTiet> hoaDonChiTiets;
    // Trong file SanPhamChiTiet.java
    // Sửa lại đoạn cuối thành thế này:
    public BigDecimal getDonGia() {
        return this.giaTien; // Trả về giá của chính biến thể này (Size/Màu này)
    }
}