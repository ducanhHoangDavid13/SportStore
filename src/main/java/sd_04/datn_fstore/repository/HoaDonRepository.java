package sd_04.datn_fstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.HoaDon;

import java.math.BigDecimal; // <-- THÊM IMPORT NÀY
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    /**
     * HÀM 1: Search đầy đủ (cho API /api/admin/hoadon/search)
     * (Đã thêm 2 dòng lọc giá minPrice và maxPrice)
     */
    @Query(value = "SELECT hd FROM HoaDon hd WHERE " +
            "(:trangThaiList IS NULL OR hd.trangThai IN :trangThaiList) AND " +
            "(:ngayBatDau IS NULL OR hd.ngayTao >= :ngayBatDau) AND " +
            "(:ngayKetThuc IS NULL OR hd.ngayTao <= :ngayKetThuc) AND " +
            "(:keyword IS NULL OR hd.maHoaDon LIKE %:keyword%) AND " +
            // --- THÊM 2 DÒNG LỌC GIÁ ---
            "(:minPrice IS NULL OR hd.tongTien >= :minPrice) AND " +
            "(:maxPrice IS NULL OR hd.tongTien <= :maxPrice)",

            countQuery = "SELECT COUNT(hd) FROM HoaDon hd WHERE " +
                    "(:trangThaiList IS NULL OR hd.trangThai IN :trangThaiList) AND " +
                    "(:ngayBatDau IS NULL OR hd.ngayTao >= :ngayBatDau) AND " +
                    "(:ngayKetThuc IS NULL OR hd.ngayTao <= :ngayKetThuc) AND " +
                    "(:keyword IS NULL OR hd.maHoaDon LIKE %:keyword%) AND " +
                    // --- THÊM 2 DÒNG LỌC GIÁ ---
                    "(:minPrice IS NULL OR hd.tongTien >= :minPrice) AND " +
                    "(:maxPrice IS NULL OR hd.tongTien <= :maxPrice)")
    Page<HoaDon> searchByTrangThaiAndNgayTao(
            Pageable pageable,
            @Param("trangThaiList") List<Integer> trangThaiList,
            @Param("ngayBatDau") LocalDateTime ngayBatDau,
            @Param("ngayKetThuc") LocalDateTime ngayKetThuc,
            @Param("keyword") String keyword,
            // --- THÊM 2 BIẾN GIÁ ---
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );

    // --- CÁC HÀM CHO API (KHỚP VỚI JS) ---

    /**
     * HÀM 2: Dùng cho API /api/hoadon/trangthai
     */
    List<HoaDon> findByTrangThaiOrderByNgayTaoDesc(Integer trangThai);

    /**
     * HÀM 3: Dùng cho API /api/hoadon/date
     */
    List<HoaDon> findByNgayTaoBetweenOrderByNgayTaoDesc(LocalDateTime startTime, LocalDateTime endTime);

    // --- CÁC HÀM CHO TRANG BÁN HÀNG (POS) ---

    /**
     * HÀM 4: Dùng cho API /api/ban-hang/hoa-don-tam (Tải HĐ Tạm)
     */
    List<HoaDon> findByTrangThaiInOrderByNgayTaoDesc(List<Integer> trangThais);

    /**
     * HÀM 5: Dùng cho API /api/ban-hang/hoa-don-tam/{id} (Tải chi tiết HĐ Tạm)
     */
    @Query("SELECT h FROM HoaDon h " +
            "LEFT JOIN FETCH h.khachHang " +
            "LEFT JOIN FETCH h.hoaDonChiTiets hdct " +
            "LEFT JOIN FETCH hdct.sanPhamChiTiet spct " +
            "LEFT JOIN FETCH spct.sanPham sp " +
            "LEFT JOIN FETCH sp.hinhAnh " + // Lấy cả hình ảnh
            "WHERE h.id = :id")
    Optional<HoaDon> findByIdWithDetails(@Param("id") Integer id);

    // --- HÀM TIỆN ÍCH KHÁC ---

    /**
     * HÀM 6: Dùng để kiểm tra trùng mã HĐ
     */
    Optional<HoaDon> findByMaHoaDon(String maHoaDon);
}