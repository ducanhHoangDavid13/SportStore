package sd_04.datn_fstore.dto;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class KhachHangRequest {
    private String maKhachHang;
    private String tenKhachHang;
    private String email;
    private String password;

}