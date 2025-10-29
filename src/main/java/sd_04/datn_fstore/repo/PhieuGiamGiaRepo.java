package sd_04.datn_fstore.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;
import sd_04.datn_fstore.model.PhieuGiamGia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PhieuGiamGiaRepo extends JpaRepository<PhieuGiamGia, Integer> {

    @Query("SELECT p FROM PhieuGiamGia p WHERE " +
            // Lọc bắt buộc theo trangThai, nếu trangThai = null thì bỏ qua điều kiện này
            "(:trangThai IS NULL OR p.trangThai = :trangThai) AND " +
            // Tìm kiếm theo keyword (Mã hoặc Tên)
            "(:keyword IS NULL OR p.maPhieuGiamGia LIKE %:keyword% OR p.tenPhieuGiamGia LIKE %:keyword%) AND " +
            // Lọc theo ngày bắt đầu
            "(:ngayBatDau IS NULL OR p.ngayBatDau >= :ngayBatDau) AND " +
            // Lọc theo ngày kết thúc
            "(:ngayKetThuc IS NULL OR p.ngayKetThuc <= :ngayKetThuc)")
    Page<PhieuGiamGia> searchAndFilter(
            @Param("trangThai") Integer trangThai,
            @Param("keyword") String keyword,
            @Param("ngayBatDau") LocalDateTime ngayBatDau,
            @Param("ngayKetThuc") LocalDateTime ngayKetThuc,
            Pageable pageable);

    // THÊM: Phương thức tìm kiếm theo mã để kiểm tra trùng lặp
    Optional<PhieuGiamGia> findByMaPhieuGiamGia(String maPhieuGiamGia);

    /**
     * Lấy danh sách các PGG Đang hoạt động (0) nhưng đã Hết hạn (ngayKetThuc < now)
     */
    @Query("SELECT p FROM PhieuGiamGia p WHERE p.trangThai = 0 AND p.ngayKetThuc < :now")
    List<PhieuGiamGia> findExpiredActivePromotions(@Param("now") LocalDateTime now);

    /**
     * Lấy danh sách các PGG Sắp diễn ra (2) nhưng đã Bắt đầu (ngayBatDau <= now)
     */
    @Query("SELECT p FROM PhieuGiamGia p WHERE p.trangThai = 2 AND p.ngayBatDau <= :now")
    List<PhieuGiamGia> findUpcomingPromotionsToActivate(@Param("now") LocalDateTime now);


}