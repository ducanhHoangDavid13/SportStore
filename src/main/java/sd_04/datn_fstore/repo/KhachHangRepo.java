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

    /**
     * Truy vấn tìm kiếm và phân trang cho KhachHang.
     * ĐÃ THÊM countQuery để khắc phục lỗi phân trang (lỗi ':keyword_1').
     */
    @Query(value = "SELECT kh FROM KhachHang kh " +
            "WHERE (:keyword IS NULL OR kh.maKhachHang LIKE %:keyword% OR kh.tenKhachHang LIKE %:keyword%) " +
            "AND (:sdt IS NULL OR kh.soDienThoai LIKE %:sdt%) " +
            "AND (:gioiTinh IS NULL OR kh.gioiTinh = :gioiTinh) " +
            "AND kh.trangThai = 1", // Truy vấn chính

            // >>>>> PHẦN BỔ SUNG ĐỂ KHẮC PHỤC LỖI COUNT QUERY <<<<<
            countQuery = "SELECT COUNT(kh) FROM KhachHang kh " +
                    "WHERE (:keyword IS NULL OR kh.maKhachHang LIKE %:keyword% OR kh.tenKhachHang LIKE %:keyword%) " +
                    "AND (:sdt IS NULL OR kh.soDienThoai LIKE %:sdt%) " +
                    "AND (:gioiTinh IS NULL OR kh.gioiTinh = :gioiTinh) " +
                    "AND kh.trangThai = 1" // Truy vấn đếm
    )
    Page<KhachHang> findFilteredKhachHang(
            @Param("keyword") String keyword,
            @Param("sdt") String sdt,
            @Param("gioiTinh") Boolean gioiTinh,
            Pageable pageable); // Pageable không cần @Param

    Optional<KhachHang> findByEmail(String email);
}