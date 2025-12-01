package sd_04.datn_fstore.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "thong_bao")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor @Builder
public class ThongBao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String tieuDe;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String noiDung;

    // 0: Chưa đọc, 1: Đã đọc
    private Integer trangThai;

    private LocalDateTime ngayTao;

    // Loại: "ORDER" (Đơn hàng), "STOCK" (Kho), "SYSTEM" (Hệ thống)
    private String loaiThongBao;

    // URL để khi bấm vào thông báo sẽ chuyển trang (Ví dụ: /admin/hoa-don/detail/1)
    private String urlLienKet;
}