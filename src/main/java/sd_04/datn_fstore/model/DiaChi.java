package sd_04.datn_fstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*; // Import
import lombok.*;

@Data
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "DiaChi")
public class DiaChi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "hoTen")
    @NotBlank(message = "Họ tên người nhận không được để trống")
    private String hoTen;

    @Column(name = "soDienThoai", length = 20)
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại phải gồm 10 số và bắt đầu bằng số 0")
    private String soDienThoai;

    @Column(name = "diaChiCuThe")
    @NotBlank(message = "Địa chỉ cụ thể không được để trống")
    private String diaChiCuThe;

    @Column(name = "xa")
    @NotBlank(message = "Xã/Phường không được để trống")
    private String xa;

    @Column(name = "huyen") // <--- CẦN THÊM THUỘC TÍNH NÀY
    private String huyen;
    @Column(name = "thanhPho")
    @NotBlank(message = "Tỉnh/Thành phố không được để trống") // Hoặc Quận/Huyện tùy logic
    private String thanhPho;

    @Column(name = "loaiDiaChi")
    private String loaiDiaChi;

    @Column(name = "ghiChu")
    private String ghiChu;

    @ManyToOne @JoinColumn(name = "idKhachHang")
    private KhachHang khachhang;

    @Column(name = "trangThai")
    private Integer trangThai;


}