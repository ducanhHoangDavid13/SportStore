package sd_04.datn_fstore.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class CheckoutItemDTO {
    private Long id; // ID của CartDetail
    private Long idSanPhamChiTiet;
    private String tenSanPham;
    private String tenHinhAnh;
    private Double donGia;
    private Integer soLuong;
    private String tenKichCo;
    private String tenMau;
}
