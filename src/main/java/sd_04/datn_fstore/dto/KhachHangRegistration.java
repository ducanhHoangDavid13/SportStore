package sd_04.datn_fstore.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import sd_04.datn_fstore.model.HoaDon;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KhachHangRegistration {
    private Integer id;
    private String maKhachHang;
    private String tenKhachHang;
    private String email;
    private String password;
    private Boolean gioiTinh;
    private String soDienThoai;
    private Integer namSinh;
    private String vaiTro;
    private Integer trangThai;
}