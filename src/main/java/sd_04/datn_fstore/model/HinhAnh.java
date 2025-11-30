package sd_04.datn_fstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString; // ⬅️ Thêm import Lombok ToString

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "HinhAnh")
public class HinhAnh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tenHinhAnh")
    private String tenHinhAnh;

    @Column(name = "moTa")
    private String moTa;

    @Column(name = "ngayTao")
    private LocalDateTime ngayTao;

    @Column(name = "ngaySua")
    private LocalDateTime ngaySua;

    @Column(name = "trangThai")
    private Integer trangThai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSanPham")
    // NGẮT VÒNG LẶP: Khi in HinhAnh, không in SanPham để tránh đệ quy
    @ToString.Exclude
    private SanPham sanPham;
}