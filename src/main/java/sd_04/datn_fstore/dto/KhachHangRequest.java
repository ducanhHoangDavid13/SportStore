package sd_04.datn_fstore.dto;

import lombok.*;


@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KhachHangRequest {

    private Integer id;
    private String maKhachHang;
    private String tenKhachHang;
    private String email;
    private String password;
    private Boolean gioiTinh;
    private String soDienThoai;
    private Integer namSinh;
    private String vaiTro;
    private Integer trangThai; // 1: Hoạt động (hiển thị), 0: Đã xóa (ẩn)

}

