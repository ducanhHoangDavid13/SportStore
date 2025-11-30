package sd_04.datn_fstore.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalculateTotalResponse {
    private BigDecimal subTotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal finalTotal;
    private String voucherMessage;
    private boolean voucherValid;
}