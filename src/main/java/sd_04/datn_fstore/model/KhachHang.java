package sd_04.datn_fstore.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Year;


@Entity
@Getter
@Setter
@Table(name = "KhachHang")
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "maKhachHang", length = 50)
    private String maKhachHang;

    @Column(name = "tenKhachHang")
    private String tenKhachHang;

    @Column(name = "email", length = 500)
    private String email;

    @Column(name = "gioiTinh")
    private Boolean gioiTinh;

    @Column(name = "soDienThoai", length = 20)
    private String soDienThoai;

    @Column(name = "namSinh")
    private Integer namSinh;

    @Column(name = "vaiTro")
    private String vaiTro;

    @Column(name = "trangThai")
    private Integer trangThai; // 1: Hoạt động (hiển thị), 0: Đã xóa (ẩn)
    public KhachHang() {
    }


    public Integer getTuoi() {
        if (this.namSinh != null) {
            return Year.now().getValue() - this.namSinh;
        }
        return null;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }


    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public Boolean getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(Boolean gioiTinh) {
        this.gioiTinh = gioiTinh;
    }


    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }


    public Integer getNamSinh() {
        return namSinh;
    }

    public void setNamSinh(Integer namSinh) {
        this.namSinh = namSinh;
    }


    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }


    public Integer getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Integer trangThai) {
        this.trangThai = trangThai;
    }

    
}
