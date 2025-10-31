package sd_04.datn_fstore.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.KhachHang;

import java.util.Optional;

@Repository
public interface KhachHangRepo extends JpaRepository<KhachHang, Integer> {
    @Query("SELECT kh FROM KhachHang kh " +
            "WHERE (:keyword IS NULL OR kh.maKhachHang LIKE %:keyword% OR kh.tenKhachHang LIKE %:keyword%) " +
            "AND (:sdt IS NULL OR kh.soDienThoai LIKE %:sdt%) " +
            "AND (:gioiTinh IS NULL OR kh.gioiTinh = :gioiTinh) " +
            "AND kh.trangThai = 1")
        // CHỈ LẤY CÁC KHÁCH HÀNG CÓ TRẠNG THÁI = 1
    Page<KhachHang> findFilteredKhachHang(
            @Param("keyword") String keyword,
            @Param("sdt") String sdt,
            @Param("gioiTinh") Boolean gioiTinh,
            Pageable pageable);

    Optional<KhachHang> findByEmail(String email);
}
