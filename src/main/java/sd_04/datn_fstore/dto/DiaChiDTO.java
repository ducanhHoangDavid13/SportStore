package sd_04.datn_fstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DiaChiDTO {

    private Integer id;

    @NotBlank(message = "Họ tên người nhận không được để trống")
    private String hoTen;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại không hợp lệ")
    private String soDienThoai;

    @NotBlank(message = "Địa chỉ cụ thể không được để trống")
    private String diaChiCuThe;

    @NotBlank(message = "Xã/Phường không được để trống")
    private String xa;

    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    private String thanhPho; // Tôi giả định trường này bao gồm cả Quận/Huyện, Tỉnh/Thành

    private String loaiDiaChi; // Ví dụ: "Nhà riêng" hoặc "Văn phòng"

    private String ghiChu;
}