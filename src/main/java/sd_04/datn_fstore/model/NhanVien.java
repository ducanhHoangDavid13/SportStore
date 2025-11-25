package sd_04.datn_fstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "NhanVien")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "hoaDons"})
public class NhanVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "maNhanVien", length = 50, unique = true)
    @NotBlank(message = "Mã nhân viên không được để trống")
    private String maNhanVien;

    @Column(name = "tenNhanVien", length = 500)
    @NotBlank(message = "Tên nhân viên không được để trống")
    private String tenNhanVien;

    @Column(name = "gioiTinh")
    private Boolean gioiTinh;

    @Column(name = "hinhAnh")
    private String hinhAnh;

    @Column(name = "cccd", length = 50, unique = true)
    @NotBlank(message = "CCCD không được để trống")
    @Pattern(regexp = "\\d{12}", message = "CCCD phải bao gồm đúng 12 chữ số")
    private String cccd;

    @Column(name = "email", length = 500, unique = true)
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @Column(name = "soDienThoai", length = 20, unique = true)
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại phải gồm 10 số và bắt đầu bằng số 0")
    private String soDienThoai;

    @Column(name = "diaChi")
    @NotBlank(message = "Địa chỉ không được để trống")
    private String diaChi;

    @Column(name = "vaiTro", length = 500)
    @NotBlank(message = "Vai trò không được để trống")
    private String vaiTro;

    @Column(name = "trangThai")
    private Integer trangThai;

    @OneToMany(mappedBy = "nhanVien", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"nhanVien"})
    private List<HoaDon> hoaDons;
}