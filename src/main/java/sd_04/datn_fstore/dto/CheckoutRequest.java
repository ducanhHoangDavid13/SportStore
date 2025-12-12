package sd_04.datn_fstore.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CheckoutRequest {
    // Thông tin người nhận
    private String fullName;
    private String phone;
    private String email;
    private Integer khachHangId;
    // Địa chỉ
    private String city;
    private String district;
    private String ward;
    private String addressDetail;

    private String note;
    private String paymentMethod; // COD, VNPAY, TRANSFER
    private String voucherCode;

    private BigDecimal shippingFee;

    private List<CartItem> items;

    @Data
    public static class CartItem {
        private Integer sanPhamChiTietId;
        private Integer soLuong;
        private BigDecimal donGia;
    }

    public String getFullAddress() {
        return addressDetail + ", " + ward + ", " + district + ", " + city;
    }
}