package sd_04.datn_fstore.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; // 🟢 Import mới
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "MauSac")
public class MauSac {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "maMau", length = 100, unique = true)
    @NotBlank(message = "Mã màu không được để trống")
    private String maMau;

    @Column(name = "tenMauSac", length = 255)
    @NotBlank(message = "Tên màu sắc không được để trống")
    private String tenMauSac;

    // 🟢 Dùng LocalDateTime
    @Column(name = "ngayTao")
    private LocalDateTime ngayTao;

    @Column(name = "trangThai")
    private Integer trangThai;

    @Column(name = "moTa")
    private String moTa;

    @JsonIgnore
    @OneToMany(mappedBy = "mauSac", fetch = FetchType.LAZY)
    private List<SanPhamChiTiet> sanPhamChiTiets;
}