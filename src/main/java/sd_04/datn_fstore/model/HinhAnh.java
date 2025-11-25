package sd_04.datn_fstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "HinhAnh")
public class HinhAnh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tenHinhAnh")
    private String tenHinhAnh;

    @Column(name = "moTa")
    private String moTa;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ngayTao")
    private Date ngayTao;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ngaySua")
    private Date ngaySua;

    @Column(name = "trangThai")
    private Integer trangThai;

    // Quan hệ ManyToOne với SanPham
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSanPham")
    private SanPham sanPham;

    // Custom toString() an toàn, chỉ hiển thị thông tin quan trọng của SanPham
    @Override
    public String toString() {
        return "HinhAnh{" +
                "id=" + id +
                ", tenHinhAnh='" + tenHinhAnh + '\'' +
                ", moTa='" + moTa + '\'' +
                ", ngayTao=" + ngayTao +
                ", ngaySua=" + ngaySua +
                ", trangThai=" + trangThai +
                ", sanPhamId=" + (sanPham != null ? sanPham.getId() : null) +
                ", sanPhamTen=" + (sanPham != null ? sanPham.getTenSanPham() : null) +
                '}';
    }
}
