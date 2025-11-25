package sd_04.datn_fstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "XuatXu")
public class XuatXu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

//    @Column(name = "maXuatXu", length = 50, unique = true)
//    @NotBlank(message = "Mã xuất xứ không được để trống")
//    private String maXuatXu;

    @Column(name = "tenXuatXu", length = 500)
    @NotBlank(message = "Tên xuất xứ không được để trống")
    private String tenXuatXu;

    @Column(name = "trangThai")
    private Integer trangThai;

    @Column(name = "ngayTao")
    private LocalDateTime ngayTao;

    @Column(name = "moTa")
    private String moTa;

    // 🔴 SỬA Ở ĐÂY: Trỏ tới SanPhamChiTiet
    @JsonIgnore
    @OneToMany(mappedBy = "xuatXu", fetch = FetchType.LAZY)
    private List<SanPhamChiTiet> sanPhamChiTiets;
}