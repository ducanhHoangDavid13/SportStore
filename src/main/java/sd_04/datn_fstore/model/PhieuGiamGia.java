package sd_04.datn_fstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
@Table(name = "PhieuGiamGia")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PhieuGiamGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "maPhieuGiamGia", length = 50, unique = true)
    @NotBlank(message = "Mã phiếu giảm giá không được để trống")
    private String maPhieuGiamGia;

    @Column(name = "tenPhieuGiamGia", length = 500)
    @NotBlank(message = "Tên phiếu giảm giá không được để trống")
    private String tenPhieuGiamGia;

    @Column(name = "ngayTao")
    private LocalDateTime ngayTao;

    @Column(name = "trangThai")
    private Integer trangThai; // 0: Đang hoạt động, 1: Dừng, 2: Sắp diễn ra

    // --- CÁC TRƯỜNG LIÊN QUAN ĐẾN LOẠI GIẢM GIÁ ---

    /**
     * 1: Giảm tiền mặt (VND) - Ví dụ giảm thẳng 50k
     * 2: Giảm phần trăm (%) - Ví dụ giảm 10%
     */
    @Column(name = "hinhThucGiam")
    @NotNull(message = "Hình thức giảm không được để trống")
    private Integer hinhThucGiam;

    /**
     * Giá trị giảm thực tế:
     * - Nếu hinhThuc = 1: Lưu số tiền (VD: 50000)
     * - Nếu hinhThuc = 2: Lưu số phần trăm (VD: 10, 20, 50)
     */
    @Column(name = "giaTriGiam", precision = 18, scale = 2)
    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal giaTriGiam;

    /**
     * Số tiền giảm TỐI ĐA (Max Discount):
     * - Nếu là %: Ví dụ giảm 10% tối đa 50k -> Trường này lưu 50000.
     * - Nếu là VND: Trường này thường bằng chính giaTriGiam.
     */
    @Column(name = "soTienGiam", precision = 18, scale = 2)
    // Có thể null nếu không giới hạn, nhưng tốt nhất nên để giá trị mặc định lớn nếu null
    private BigDecimal soTienGiam;

    // ------------------------------------------------

    @Column(name = "dieuKienGiamGia", precision = 18, scale = 2)
    @NotNull(message = "Điều kiện giảm giá (đơn tối thiểu) không được để trống")
    @DecimalMin(value = "0.0", message = "Điều kiện giảm giá phải lớn hơn hoặc bằng 0")
    private BigDecimal dieuKienGiamGia;

    @Column(name = "ngayBatDau")
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime ngayBatDau;

    @Column(name = "ngayKetThuc")
    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime ngayKetThuc;

    @Column(name = "moTa", length = 255)
    private String moTa;

    @Column(name = "soLuong")
    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng không được âm")
    private Integer soLuong;

    @JsonIgnore
    @OneToMany(mappedBy = "phieuGiamGia", fetch = FetchType.LAZY)
    private List<HoaDon> hoaDons;
}