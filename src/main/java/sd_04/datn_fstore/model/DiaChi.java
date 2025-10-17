package sd_04.datn_fstore.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "DiaChi")
public class DiaChi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "hoTen")
    private String hoTen;

    @Column(name = "soDienThoai", length = 20)
    private String soDienThoai;

    @Column(name = "diaChiCuThe")
    private String diaChiCuThe;

    @Column(name = "xa")
    private String xa;

    @Column(name = "thanhPho")
    private String thanhPho;

    @Column(name = "loaiDiaChi")
    private String loaiDiaChi;

    @Column(name = "ghiChu")
    private String ghiChu;

    @ManyToOne @JoinColumn(name = "idKhachHang")
    private KhachHang khachhang;

    @Column(name = "trangThai")
    private Integer trangThai;
}
