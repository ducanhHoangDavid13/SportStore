package sd_04.datn_fstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "KhachHang")
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "maKhachHang", length = 50, unique = true)
    @NotBlank(message = "Mã khách hàng không được để trống")
    private String maKhachHang;

    @Column(name = "tenKhachHang")
    @NotBlank(message = "Tên khách hàng không được để trống")
    @Size(max = 100, message = "Tên khách hàng không quá 100 ký tự")
    private String tenKhachHang;

    @Column(name = "email", length = 500, unique = true)
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @Column(name = "gioiTinh")
    private Boolean gioiTinh;

    @Column(name = "soDienThoai", length = 20, unique = true)
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại phải gồm 10 số và bắt đầu bằng số 0")
    private String soDienThoai;

    // 🟢 Đã đổi từ namSinh (Integer) sang ngaySinh (LocalDateTime)
    @Column(name = "ngaySinh")
    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDateTime ngaySinh;

    @Column(name = "vaiTro")
    private String vaiTro;

    @Column(name = "trangThai")
    private Integer trangThai;

    public KhachHang() {
    }

    @JsonIgnore
    @OneToMany(mappedBy = "khachHang", fetch = FetchType.LAZY)
    private List<HoaDon> hoaDons;

    // 🟢 Cập nhật logic tính tuổi theo LocalDateTime
    public Integer getTuoi() {
        if (this.ngaySinh != null) {
            return Period.between(this.ngaySinh.toLocalDate(), LocalDate.now()).getYears();
        }
        return null;
    }
}