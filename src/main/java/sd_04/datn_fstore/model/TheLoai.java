package sd_04.datn_fstore.model;
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
@Table(name = "TheLoai")
public class TheLoai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tenTheLoai", length = 500)
    private String tenTheLoai;

    @Column(name = "trangThai")
    private Integer trangThai;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ngayTao")
    private Date ngayTao;

    @Column(name = "moTa")
    private String moTa;

    // Mối quan hệ: Một thể loại có trong nhiều sản phẩm chi tiết
    @OneToMany(mappedBy = "theLoai", fetch = FetchType.LAZY)
    private List<SanPhamChiTiet> sanPhamChiTiets;
}
