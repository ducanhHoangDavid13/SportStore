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

@Data // Lombok: Tự sinh Getter, Setter, toString...
@NoArgsConstructor // Lombok: Constructor không tham số
@AllArgsConstructor // Lombok: Constructor full tham số
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "KichThuoc")
public class KichThuoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tenKichThuoc", length = 500, unique = true)
    @NotBlank(message = "Tên kích thước không được để trống")
    private String tenKichThuoc;

    @Column(name = "trangThai")
    private Integer trangThai;

    @Column(name = "ngayTao")
    private LocalDateTime ngayTao; // Đã đổi sang LocalDateTime

    @Column(name = "moTa")
    private String moTa;

    @JsonIgnore
    @OneToMany(mappedBy = "kichThuoc", fetch = FetchType.LAZY)
    private List<SanPhamChiTiet> sanPhamChiTiets;
}