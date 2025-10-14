package model;


import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "SanPham")
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "maSanPham", columnDefinition = "NVARCHAR(100)")
    private String maSanPham;

    @Column(name = "tenSanPham", columnDefinition = "NVARCHAR(500)")
    private String tenSanPham;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ngayTao")
    private Date ngayTao;

    @Column(name = "trangThai")
    private Integer trangThai;

    @Column(name = "moTa", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @Column(name = "giaTien", precision = 18, scale = 2)
    private BigDecimal giaTien;

    @Column(name = "soLuong")
    private Integer soLuong;

//    @OneToMany(mappedBy = "sanPham")
//    private List<HinhAnh> hinhAnhList;
//
//    @OneToMany(mappedBy = "sanPham")
//    private List<SanPhamChiTiet> sanPhamChiTietList;
}
