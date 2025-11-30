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
@Table(name = "PhanLoai")
public class PhanLoai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "phanLoai", length = 500, unique = true)
    @NotBlank(message = "Tên phân loại không được để trống")
    private String phanLoai;

    @Column(name = "trangThai")
    private Integer trangThai;

    @Column(name = "ngayTao")
    private LocalDateTime ngayTao; // Đã đổi sang LocalDateTime

    @Column(name = "moTa")
    private String moTa;

    @JsonIgnore
    @OneToMany(mappedBy = "phanLoai", fetch = FetchType.LAZY)
    private List<SanPhamChiTiet> sanPhamChiTiets;
}