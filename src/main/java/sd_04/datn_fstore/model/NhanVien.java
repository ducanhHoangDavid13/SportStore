package sd_04.datn_fstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "NhanVien")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "hoaDons"})
public class NhanVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "maNhanVien", length = 50, unique = true)
    private String maNhanVien;

    @Column(name = "tenNhanVien", length = 500)
    private String tenNhanVien;

    @Column(name = "gioiTinh")
    private Boolean gioiTinh;

    @Column(name = "hinhAnh")
    private String hinhAnh;

    @Column(name = "cccd", length = 50, unique = true)
    private String cccd;

    @Column(name = "email", length = 500, unique = true)
    private String email;

    @Column(name = "soDienThoai", length = 20, unique = true)
    private String soDienThoai;

    @Column(name = "diaChi")
    @NotBlank(message = "Địa chỉ không được để trống")
    private String diaChi;

    @Column(name = "vaiTro", length = 500)
    private String vaiTro;

    @Column(name = "trangThai")
    private Integer trangThai;

    @OneToMany(mappedBy = "nhanVien", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"nhanVien"})
    private List<HoaDon> hoaDons;
}