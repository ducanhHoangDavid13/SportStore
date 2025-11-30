package sd_04.datn_fstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NhanVienRegistration {
    private Integer id;
    private String maNhanVien;
    private String tenNhanVien;
    private Boolean gioiTinh;
    private String hinhAnh;
    private String cccd;
    private String email;
    private String password;
    private String soDienThoai;
    private String diaChi;
    private String vaiTro;
    private Integer trangThai;
}