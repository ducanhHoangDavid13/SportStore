package sd_04.datn_fstore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "PhieuGiamGia")
public class PhieuGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    // Ánh xạ cột maPhieuGiamGia
    @Column(name = "maPhieuGiamGia", length = 50)
    private String maPhieuGiamGia;

    // Ánh xạ cột tenPhieuGiamGia
    @Column(name = "tenPhieuGiamGia", length = 500)
    private String tenPhieuGiamGia;

    // Ánh xạ cột ngayTao
    @Column(name = "ngayTao")
    private LocalDateTime ngayTao; // Dùng LocalDateTime thay cho java.util.Date/Timestamp cho các trường DATETIME

    // Ánh xạ cột trangThai
    @Column(name = "trangThai")
    private Integer trangThai; //0 đang hoat động ,1 dừng hoạt động , sắp diễn ra


    // Ánh xạ cột dieuKienGiamGia (DECIMAL(18,2))
    @Column(name = "dieuKienGiamGia", precision = 18, scale = 2)
    private BigDecimal dieuKienGiamGia;

    // Ánh xạ cột soTienGiam (DECIMAL(18,2))
    @Column(name = "soTienGiam", precision = 18, scale = 2)
    private BigDecimal soTienGiam;

    // Ánh xạ cột ngayBatDau
    @Column(name = "ngayBatDau")
    private LocalDateTime ngayBatDau;

    // Ánh xạ cột ngayKetThuc
    @Column(name = "ngayKetThuc")
    private LocalDateTime ngayKetThuc;

    // Ánh xạ cột moTa
    @Column(name = "moTa", length = 255)
    private String moTa;

    // Ánh xạ cột soLuong
    @Column(name = "soLuong")
    private Integer soLuong;
    public PhieuGiamGia() {
    }

    public PhieuGiamGia(Integer id, String maPhieuGiamGia, String tenPhieuGiamGia, LocalDateTime ngayTao, Integer trangThai, BigDecimal dieuKienGiamGia, BigDecimal soTienGiam, LocalDateTime ngayBatDau, LocalDateTime ngayKetThuc, String moTa, Integer soLuong) {
        this.id = id;
        this.maPhieuGiamGia = maPhieuGiamGia;
        this.tenPhieuGiamGia = tenPhieuGiamGia;
        this.ngayTao = ngayTao;
        this.trangThai = trangThai;
        this.dieuKienGiamGia = dieuKienGiamGia;
        this.soTienGiam = soTienGiam;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.moTa = moTa;
        this.soLuong = soLuong;
    }

    // --- Getters và Setters ---

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMaPhieuGiamGia() {
        return maPhieuGiamGia;
    }

    public void setMaPhieuGiamGia(String maPhieuGiamGia) {
        this.maPhieuGiamGia = maPhieuGiamGia;
    }

    public String getTenPhieuGiamGia() {
        return tenPhieuGiamGia;
    }

    public void setTenPhieuGiamGia(String tenPhieuGiamGia) {
        this.tenPhieuGiamGia = tenPhieuGiamGia;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public Integer getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Integer trangThai) {
        this.trangThai = trangThai;
    }

    public BigDecimal getDieuKienGiamGia() {
        return dieuKienGiamGia;
    }

    public void setDieuKienGiamGia(BigDecimal dieuKienGiamGia) {
        this.dieuKienGiamGia = dieuKienGiamGia;
    }

    public BigDecimal getSoTienGiam() {
        return soTienGiam;
    }

    public void setSoTienGiam(BigDecimal soTienGiam) {
        this.soTienGiam = soTienGiam;
    }

    public LocalDateTime getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDateTime ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDateTime getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDateTime ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

}
