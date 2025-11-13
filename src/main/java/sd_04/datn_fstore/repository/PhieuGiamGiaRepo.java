package sd_04.datn_fstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository; // Thêm @Repository cho chắc chắn
import sd_04.datn_fstore.model.PhieuGiamGia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository // Thêm @Repository
public interface PhieuGiamGiaRepo extends JpaRepository<PhieuGiamGia, Integer> {

    /**
     * Hàm tìm kiếm và phân trang chính
     */
    @Query(value = "SELECT p FROM PhieuGiamGia p WHERE " +
            "(:trangThai IS NULL OR p.trangThai = :trangThai) AND " +
            "(:keyword IS NULL OR p.maPhieuGiamGia LIKE %:keyword% OR p.tenPhieuGiamGia LIKE %:keyword%) AND " +
            "(:ngayBatDau IS NULL OR p.ngayBatDau >= :ngayBatDau) AND " +
            "(:ngayKetThuc IS NULL OR p.ngayKetThuc <= :ngayKetThuc)",

            // Thêm countQuery để sửa lỗi phân trang
            countQuery = "SELECT COUNT(p) FROM PhieuGiamGia p WHERE " +
                    "(:trangThai IS NULL OR p.trangThai = :trangThai) AND " +
                    "(:keyword IS NULL OR p.maPhieuGiamGia LIKE %:keyword% OR p.tenPhieuGiamGia LIKE %:keyword%) AND " +
                    "(:ngayBatDau IS NULL OR p.ngayBatDau >= :ngayBatDau) AND " +
                    "(:ngayKetThuc IS NULL OR p.ngayKetThuc <= :ngayKetThuc)")
    Page<PhieuGiamGia> searchAndFilter(
            @Param("trangThai") Integer trangThai,
            @Param("keyword") String keyword,
            @Param("ngayBatDau") LocalDateTime ngayBatDau,
            @Param("ngayKetThuc") LocalDateTime ngayKetThuc,
            Pageable pageable);

    /**
     * Dùng cho hàm getActive() trong Service
     */
    List<PhieuGiamGia> findByTrangThai(Integer trangThai);

    /**
     * Dùng để check trùng mã khi Thêm/Sửa
     */
    Optional<PhieuGiamGia> findByMaPhieuGiamGia(String maPhieuGiamGia);

    /**
     * Dùng cho hàm sync: Lấy PGG Đang hoạt động (0) nhưng đã Hết hạn
     */
    @Query("SELECT p FROM PhieuGiamGia p WHERE p.trangThai = 0 AND p.ngayKetThuc < :now")
    List<PhieuGiamGia> findExpiredActivePromotions(@Param("now") LocalDateTime now);

    /**
     * Dùng cho hàm sync: Lấy PGG Sắp diễn ra (2) nhưng đã Bắt đầu
     */
    @Query("SELECT p FROM PhieuGiamGia p WHERE p.trangThai = 2 AND p.ngayBatDau <= :now")
    List<PhieuGiamGia> findUpcomingPromotionsToActivate(@Param("now") LocalDateTime now);
}