package sd_04.datn_fstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.Year;
import java.util.List;


@Entity
@Getter
@Setter
@Builder
@Table(name = "KhachHang")
@AllArgsConstructor
@NoArgsConstructor
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "maKhachHang", length = 50)
    private String maKhachHang;

    @Column(name = "tenKhachHang")
    private String tenKhachHang;

    @Column(name = "email", length = 500)
    private String email;

    @Column(name = "gioiTinh")
    private Boolean gioiTinh;

    @Column(name = "soDienThoai", length = 20)
    private String soDienThoai;

    @Column(name = "namSinh")
    private Integer namSinh;

    @Column(name = "vaiTro")
    private String vaiTro;

    @Column(name = "trangThai")
    private Integer trangThai; // 1: Hoạt động (hiển thị), 0: Đã xóa (ẩn)

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

