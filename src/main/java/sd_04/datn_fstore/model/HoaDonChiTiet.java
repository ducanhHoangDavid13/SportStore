package sd_04.datn_fstore.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "HoaDonChiTiet")
public class HoaDonChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "maHoaDonChiTiet", length = 50)
    private String maHoaDonChiTiet;

    @Column(name = "soLuong")
    private Integer soLuong;

    @Column(name = "moTa")
    private String moTa;

    @Column(name = "trangThai")
    private Integer trangThai;

    // Foreign Keys
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSanPhamChiTiet")
    private SanPhamChiTiet sanPhamChiTiet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPhieuGiamGia")
    private PhieuGiamGia phieuGiamGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idHoaDon")
    private HoaDon hoaDon;
}
