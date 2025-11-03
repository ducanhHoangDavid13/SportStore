package sd_04.datn_fstore.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PhieuGiamGia")
public class PhieuGiamGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "maPhieuGiamGia", length = 50)
    private String maPhieuGiamGia;

    @Column(name = "tenPhieuGiamGia", length = 500)
    private String tenPhieuGiamGia;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ngayTao")
    private Date ngayTao;

    @Column(name = "trangThai")
    private Integer trangThai;

    @Column(name = "dieuKienGiamGia", precision = 18, scale = 2)
    private BigDecimal dieuKienGiamGia;

    @Column(name = "soTienGiam", precision = 18, scale = 2)
    private BigDecimal soTienGiam;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ngayBatDau")
    private Date ngayBatDau;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ngayKetThuc")
    private Date ngayKetThuc;

    @Column(name = "moTa", length = 255)
    private String moTa;

    @Column(name = "soLuong")
    private Integer soLuong;

    // Một phiếu giảm giá có thể thuộc nhiều hóa đơn
    @OneToMany(mappedBy = "phieuGiamGia", fetch = FetchType.LAZY)
    @JsonBackReference(value = "phieuGiamGia-hoaDons") // Ngăn vòng lặp
    private List<HoaDon> hoaDons;

    // Một phiếu giảm giá có thể thuộc nhiều chi tiết hóa đơn
    @OneToMany(mappedBy = "phieuGiamGia", fetch = FetchType.LAZY)
    @JsonBackReference(value = "phieuGiamGia-hoaDonChiTiets") // Ngăn vòng lặp
    private List<HoaDonChiTiet> hoaDonChiTiets;
}
