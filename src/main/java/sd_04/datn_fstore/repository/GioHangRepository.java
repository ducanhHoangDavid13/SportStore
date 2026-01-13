package sd_04.datn_fstore.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.GioHang;

import java.util.List;
import java.util.Optional;

@Repository
public interface GioHangRepository extends JpaRepository<GioHang, Integer> {

    // 1. Tìm tất cả giỏ hàng theo ID Khách hàng
    // Đảm bảo trạng thái này khớp với trạng thái bạn gán khi thêm mới (trangThai=1)
    @Query("SELECT gh FROM GioHang gh WHERE gh.idKhachHang = :idKhachHang AND gh.trangThai = 1")
    List<GioHang> findByIdKhachHang(@Param("idKhachHang") Integer idKhachHang);


    // 2. Tìm một sản phẩm chi tiết cụ thể trong giỏ hàng của Khách hàng
    @Query("SELECT gh FROM GioHang gh WHERE " +
            "gh.idKhachHang = :idKhachHang AND " +
            "gh.idSanPhamChiTiet = :idSanPhamChiTiet AND gh.trangThai = 1")
    Optional<GioHang> findByIdKhachHangAndIdSanPhamChiTiet(
            @Param("idKhachHang") Integer idKhachHang,
            @Param("idSanPhamChiTiet") Integer idSanPhamChiTiet
    );

    // 3. Tìm tất cả các giỏ hàng liên quan đến MỘT sản phẩm CHI TIẾT
    List<GioHang> findByIdSanPhamChiTiet(Integer idSanPhamChiTiet);

    @Modifying
    @Transactional
    @Query("DELETE FROM GioHang gh WHERE gh.idKhachHang = :idKhachHang AND gh.idSanPhamChiTiet = :idSanPhamChiTiet AND gh.trangThai = 1")
    void deleteFromCart(@Param("idKhachHang") Integer idKhachHang, @Param("idSanPhamChiTiet") Integer idSanPhamChiTiet);
}