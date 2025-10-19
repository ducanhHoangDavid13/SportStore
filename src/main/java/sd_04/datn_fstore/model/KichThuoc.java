package sd_04.datn_fstore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "KichThuoc")
public class KichThuoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tenKichThuoc", length = 500)
    private String tenKichThuoc;

    @Column(name = "trangThai")
    private Integer trangThai;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ngayTao")
    private Date ngayTao;

    @Column(name = "moTa")
    private String moTa;

    // Mối quan hệ: Một kích thước có trong nhiều sản phẩm chi tiết
    @OneToMany(mappedBy = "kichThuoc", fetch = FetchType.LAZY)
    private List<SanPhamChiTiet> sanPhamChiTiets;
}
