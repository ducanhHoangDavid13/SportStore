package sd_04.datn_fstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "NhanVien")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "hoaDons"}) // 🟢 tránh vòng lặp
public class NhanVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "maNhanVien", length = 50)
    private String maNhanVien;

    @Column(name = "tenNhanVien", length = 500)
    private String tenNhanVien;

    @Column(name = "gioiTinh")
    private Boolean gioiTinh;

    @Column(name = "hinhAnh")
    private String hinhAnh;

    @Column(name = "cccd", length = 50)
    private String cccd;

    @Column(name = "email", length = 500)
    private String email;

    @Column(name = "soDienThoai", length = 20)
    private String soDienThoai;

    @Column(name = "diaChi")
    private String diaChi;

    @Column(name = "vaiTro", length = 500)
    private String vaiTro;

    @Column(name = "trangThai")
    private Integer trangThai;

    // Mối quan hệ: Một nhân viên có nhiều hóa đơn
    @OneToMany(mappedBy = "nhanVien", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"nhanVien"}) // 🟢 bỏ thông tin ngược lại để tránh vòng lặp
    private List<HoaDon> hoaDons;
}
