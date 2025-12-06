package sd_04.datn_fstore.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.GioHang;
import sd_04.datn_fstore.model.SanPham;

import java.util.List;
import java.util.Optional;

@Repository
public interface GioHangRepository extends JpaRepository<GioHang, Integer> {

    @Query("SELECT gh FROM GioHang gh WHERE gh.khachHang.id = :idKhachHang")
    List<GioHang> findByKhachHangId(@Param("idKhachHang") Integer idKhachHang);


    @Query("SELECT gh FROM GioHang gh WHERE " +
            "gh.khachHang.id = :idKhachHang AND " +
            "gh.sanPham.id = :idSanPham")
    Optional<GioHang> findByKhachHangIdAndSanPhamId(
            @Param("idKhachHang") Integer idKhachHang,
            @Param("idSanPham") Integer idSanPham
    );

    /**
     * Câu lệnh Query 3: Tìm tất cả các giỏ hàng liên quan đến MỘT sản phẩm.
     * Dùng cho logic xóa sản phẩm (phải xóa các giỏ hàng liên quan trước).
     */
    List<GioHang> findBySanPham(SanPham sanPham);
}
