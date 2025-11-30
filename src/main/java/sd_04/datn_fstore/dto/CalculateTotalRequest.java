package sd_04.datn_fstore.dto;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CalculateTotalRequest {
    private String voucherCode;
    private BigDecimal shippingFee;
    private List<CartItem> items;

    @Data
    public static class CartItem {
        private Integer sanPhamChiTietId;
        private Integer soLuong;
        private BigDecimal donGia;
    }
}