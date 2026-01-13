package sd_04.datn_fstore.dto;

import lombok.Data;
import java.util.List;

@Data
public class CheckoutRequestDTO {
    private Integer addressId;
    private String note;
    private String paymentMethod;
    private String voucherCode;
    private Boolean isBuyNow;
    private List<CartItemOrder> items;

    @Data
    public static class CartItemOrder {
        private Integer cartItemId; // ID của dòng trong bảng GioHang
        private Integer sanPhamChiTietId;
        private Integer soLuong;
    }
}