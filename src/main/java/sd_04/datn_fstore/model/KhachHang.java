package sd_04.datn_fstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.Year;
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
    private String maKhachHang;

    @Column(name = "tenKhachHang")
    private String tenKhachHang;

    @Column(name = "email", length = 500, unique = true)
    @Email(message = "Email không đúng định dạng")
    private String email;

    @Column(name = "gioiTinh")
    private Boolean gioiTinh;

    @Column(name = "soDienThoai", length = 20, unique = true)
    private String soDienThoai;

    // 🟢 Đã đổi từ namSinh (Integer) sang ngaySinh (LocalDateTime)
    @Column(name = "ngaySinh")
    private LocalDateTime ngaySinh;

    @Column(name = "vaiTro")
    private String vaiTro;

    @Column(name = "trangThai")
    private Integer trangThai;

    private LocalDateTime ngayTao;

    @Column(name = "namSinh")
    private Integer namSinh;
    public KhachHang() {
    }

    @JsonIgnore
    @OneToMany(mappedBy = "khachHang", fetch = FetchType.LAZY)
    private List<HoaDon> hoaDons;

    public Integer getTuoi() {
        if (this.namSinh != null) {
            return Year.now().getValue() - this.namSinh;
        }
        return null;
    }
}