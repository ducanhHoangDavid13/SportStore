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


    public DiaChi() {
    }

    public DiaChi(Integer id, String hoTen, String soDienThoai, String diaChiCuThe, String xa, String thanhPho, String loaiDiaChi, String ghiChu, KhachHang khachhang, Integer trangThai) {
        this.id = id;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.diaChiCuThe = diaChiCuThe;
        this.xa = xa;
        this.thanhPho = thanhPho;
        this.loaiDiaChi = loaiDiaChi;
        this.ghiChu = ghiChu;
        this.khachhang = khachhang;
        this.trangThai = trangThai;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }


    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getDiaChiCuThe() {
        return diaChiCuThe;
    }

    public void setDiaChiCuThe(String diaChiCuThe) {
        this.diaChiCuThe = diaChiCuThe;
    }


    public String getXa() {
        return xa;
    }

    public void setXa(String xa) {
        this.xa = xa;
    }


    public String getThanhPho() {
        return thanhPho;
    }

    public void setThanhPho(String thanhPho) {
        this.thanhPho = thanhPho;
    }


    public String getLoaiDiaChi() {
        return loaiDiaChi;
    }

    public void setLoaiDiaChi(String loaiDiaChi) {
        this.loaiDiaChi = loaiDiaChi;
    }


    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }


    public KhachHang getKhachhang() {
        return khachhang;
    }

    public void setKhachhang(KhachHang khachhang) {
        this.khachhang = khachhang;
    }

    
    public Integer getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Integer trangThai) {
        this.trangThai = trangThai;
    }
}
