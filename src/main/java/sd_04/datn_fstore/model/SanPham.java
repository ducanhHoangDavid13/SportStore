package sd_04.datn_fstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "SanPham")
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "maSanPham", length = 100)
    private String maSanPham;

    @Column(name = "tenSanPham", length = 500)
    private String tenSanPham;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ngayTao")
    private Date ngayTao;

    @Column(name = "trangThai")
    private Integer trangThai;

    @Column(name = "moTa")
    private String moTa;

    @Column(name = "giaTien", precision = 18, scale = 2)
    private BigDecimal giaTien;

    @Column(name = "soLuong")
    private Integer soLuong;

    // Mối quan hệ: Một sản phẩm có nhiều hình ảnh
    @OneToMany(mappedBy = "sanPham", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<HinhAnh> hinhAnhs;

    // Mối quan hệ: Một sản phẩm có trong nhiều giỏ hàng
    @OneToMany(mappedBy = "sanPham", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<GioHang> gioHangs;
}
