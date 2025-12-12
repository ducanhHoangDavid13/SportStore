package sd_04.datn_fstore.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecentOrderDTO {
    private String maHoaDon;
    private String customerName;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private Integer status;
}