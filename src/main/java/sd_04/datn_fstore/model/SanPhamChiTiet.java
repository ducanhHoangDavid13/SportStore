package sd_04.datn_fstore.model;


import jakarta.persistence.*;
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
public class SanPhamChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "giaTien", precision = 18, scale = 2)
    private BigDecimal giaTien;

    @Column(name = "soLuong")
    private Integer soLuong;

    @Column(name = "moTa")
    private String moTa;

    @Column(name = "trangThai")
    private Integer trangThai;

    // Foreign Keys
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idKichThuoc")
    private KichThuoc kichThuoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPhanLoai")
    private PhanLoai phanLoai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idXuatXu")
    private XuatXu xuatXu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idChatLieu")
    private ChatLieu chatLieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idMauSac")
    private MauSac mauSac;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTheLoai")
    private TheLoai theLoai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSanPham")
    private SanPham sanPham;

//    // Mối quan hệ: Một chi tiết sản phẩm có trong nhiều hóa đơn chi tiết
    @OneToMany(mappedBy = "sanPhamChiTiet", fetch = FetchType.LAZY)
    private List<HoaDonChiTiet> hoaDonChiTiets;
}
