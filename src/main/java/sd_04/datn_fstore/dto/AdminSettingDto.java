package sd_04.datn_fstore.dto; // Nhớ sửa lại package cho đúng

import lombok.Data; // Nếu dùng Lombok, không thì tự generate Getter/Setter

@Data
public class AdminSettingDto {
    // --- Tab Chung ---
    private String siteName;
    private String hotline;
    private String contactEmail;
    private String address;

    // --- Tab Bảo mật ---
    private boolean maintenanceMode; // Dùng boolean để hứng checkbox
    private int sessionTimeout;

    // --- Tab Email ---
    private String mailHost;
    private int mailPort;
    private String mailUsername;
    private String mailPassword;

    // --- Tab Thanh toán ---
    private String vnp_TmnCode;
    private String vnp_HashSecret;
    private String vnp_Url;
}