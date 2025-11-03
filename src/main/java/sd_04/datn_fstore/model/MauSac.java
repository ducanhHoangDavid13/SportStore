package sd_04.datn_fstore.model;



import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "MauSac")
public class MauSac {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "maMau", length = 100)
    private String maMau;

    @Column(name = "tenMauSac", length = 255)
    private String tenMauSac;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ngayTao")
    private Date ngayTao;

    @Column(name = "trangThai")
    private Integer trangThai;

    @Column(name = "moTa")
    private String moTa;
// Đây là code từ file MauSac.java của bạn:

    // Mối quan hệ: Một màu sắc có trong nhiều sản phẩm chi tiết
    @JsonIgnore
    @OneToMany(mappedBy = "mauSac", fetch = FetchType.LAZY)
    private List<SanPhamChiTiet> sanPhamChiTiets;

}
