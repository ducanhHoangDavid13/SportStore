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
@Table(name = "TheLoai")
public class TheLoai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

//    @Column(name = "maTheLoai", length = 50, unique = true)
//    @NotBlank(message = "Mã thể loại không được để trống")
//    private String maTheLoai;

    @Column(name = "tenTheLoai", length = 500)
    @NotBlank(message = "Tên thể loại không được để trống")
    private String tenTheLoai;

    @Column(name = "trangThai")
    private Integer trangThai;

    @Column(name = "ngayTao")
    private LocalDateTime ngayTao;

    @Column(name = "moTa")
    private String moTa;

    // 🔴 SỬA Ở ĐÂY: Trỏ tới SanPhamChiTiet, không phải SanPham
    @JsonIgnore
    @OneToMany(mappedBy = "theLoai", fetch = FetchType.LAZY)
    private List<SanPhamChiTiet> sanPhamChiTiets;
}