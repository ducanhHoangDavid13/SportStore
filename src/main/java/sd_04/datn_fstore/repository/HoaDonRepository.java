package sd_04.datn_fstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.dto.RecentOrderDTO;
import sd_04.datn_fstore.model.HoaDon;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    // =========================================================================
    // 1. CÁC HÀM SEARCH & FILTER (ADMIN)
    // =========================================================================

    @Query(value = "SELECT hd FROM HoaDon hd WHERE " +
            "(:trangThaiList IS NULL OR hd.trangThai IN :trangThaiList) AND " +
            "(:ngayBatDau IS NULL OR hd.ngayTao >= :ngayBatDau) AND " +
            "(:ngayKetThuc IS NULL OR hd.ngayTao <= :ngayKetThuc) AND " +
            "(:keyword IS NULL OR hd.maHoaDon LIKE %:keyword%) AND " +
            "(:minPrice IS NULL OR hd.tongTien >= :minPrice) AND " +
            "(:maxPrice IS NULL OR hd.tongTien <= :maxPrice)",

            countQuery = "SELECT COUNT(hd) FROM HoaDon hd WHERE " +
                    "(:trangThaiList IS NULL OR hd.trangThai IN :trangThaiList) AND " +
                    "(:ngayBatDau IS NULL OR hd.ngayTao >= :ngayBatDau) AND " +
                    "(:ngayKetThuc IS NULL OR hd.ngayTao <= :ngayKetThuc) AND " +
                    "(:keyword IS NULL OR hd.maHoaDon LIKE %:keyword%) AND " +
                    "(:minPrice IS NULL OR hd.tongTien >= :minPrice) AND " +
                    "(:maxPrice IS NULL OR hd.tongTien <= :maxPrice)")
    Page<HoaDon> searchByTrangThaiAndNgayTao(
            Pageable pageable,
            @Param("trangThaiList") List<Integer> trangThaiList,
            @Param("ngayBatDau") LocalDateTime ngayBatDau,
            @Param("ngayKetThuc") LocalDateTime ngayKetThuc,
            @Param("keyword") String keyword,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );

    // =========================================================================
    // 2. CÁC HÀM CHO API (CLIENT / MOBILE)
    // =========================================================================

    List<HoaDon> findByTrangThaiOrderByNgayTaoDesc(Integer trangThai);

    List<HoaDon> findByNgayTaoBetweenOrderByNgayTaoDesc(LocalDateTime startTime, LocalDateTime endTime);

    Optional<HoaDon> findByMaHoaDon(String maHoaDon);

    // =========================================================================
    // 3. CÁC HÀM CHO BÁN HÀNG TẠI QUẦY (POS)
    // =========================================================================

    /**
     * Tải danh sách HĐ Tạm
     */
    List<HoaDon> findByTrangThaiInOrderByNgayTaoDesc(List<Integer> trangThais);

    /**
     * Lấy HĐ Tạm cho Modal (Đơn giản)
     */
    List<HoaDon> findByTrangThaiIn(List<Integer> trangThaiList);

    /**
     * Tải chi tiết một HĐ (Fetch Join để tránh lỗi Lazy)
     */
    @Query("SELECT hd FROM HoaDon hd " +
            "LEFT JOIN FETCH hd.hoaDonChiTiets " +
            "LEFT JOIN FETCH hd.khachHang " +
            "LEFT JOIN FETCH hd.nhanVien " +
            "LEFT JOIN FETCH hd.phieuGiamGia " +
            "WHERE hd.id = :id")
    Optional<HoaDon> findByIdWithDetails(@Param("id") Integer id);

    // =========================================================================
    // 4. CÁC HÀM THỐNG KÊ DASHBOARD (QUAN TRỌNG)
    // =========================================================================

    /**
     * Đếm số đơn hàng trong khoảng thời gian (Dùng cho Card "Đơn hàng hôm nay")
     */
    Integer countByNgayTaoBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Tính tổng doanh thu (Dùng cho Card "Doanh thu" & Biểu đồ Line)
     * - Chỉ tính đơn đã hoàn thành (trangThai = 1)
     * - Dùng COALESCE để trả về 0 nếu không có đơn nào (tránh lỗi Null)
     */
    @Query("SELECT COALESCE(SUM(h.tongTien), 0) FROM HoaDon h " +
            "WHERE h.ngayTao BETWEEN :start AND :end AND h.trangThai = 1")
    BigDecimal sumTotalAmountByDateRange(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    /**
     * Đếm số lượng đơn theo trạng thái (Dùng cho Biểu đồ Tròn)
     */
    @Query("SELECT COUNT(h) FROM HoaDon h " +
            "WHERE h.trangThai = :status AND h.ngayTao BETWEEN :start AND :end")
    Integer countByStatusAndDateRange(@Param("status") Integer status,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

    /**
     * Lấy danh sách 5 đơn hàng gần đây (Dùng cho Bảng Table)
     * - Map trực tiếp vào DTO
     * - CHECK LẠI: 'h.khachHang.tenKhachHang' hay 'h.khachHang.hoTen' trong code của bạn
     */
    @Query("SELECT new sd_04.datn_fstore.dto.RecentOrderDTO(" +
            "h.maHoaDon, " +
            "h.khachHang.tenKhachHang, " + // <-- Kiểm tra lại trường này trong Entity KhachHang
            "h.ngayTao, " +
            "h.tongTien, " +
            "h.trangThai) " +
            "FROM HoaDon h ORDER BY h.ngayTao DESC")
    List<RecentOrderDTO> findRecentOrders(Pageable pageable);

    // =========================================================================
    // 5. CÁC HÀM TIỆN ÍCH KHÁC (CÓ THỂ GIỮ LẠI NẾU CẦN)
    // =========================================================================

    int countByTrangThai(Integer trangThai);
    // Trong HoaDonRepository.java
    @Query("SELECT " +
            "SUM(CASE WHEN h.trangThai = 0 THEN 1 ELSE 0 END), " + // Chờ xác nhận
            "SUM(CASE WHEN h.trangThai = 1 THEN 1 ELSE 0 END), " + // Đã xác nhận
            "SUM(CASE WHEN h.trangThai = 2 THEN 1 ELSE 0 END), " + // Chuẩn bị
            "SUM(CASE WHEN h.trangThai = 3 THEN 1 ELSE 0 END), " + // Đang giao
            "SUM(CASE WHEN h.trangThai = 4 THEN 1 ELSE 0 END), " + // Hoàn thành
            "SUM(CASE WHEN h.trangThai = 5 THEN 1 ELSE 0 END), " + // Đã hủy
            "SUM(CASE WHEN h.trangThai = 6 THEN 1 ELSE 0 END) " +  // Chờ thanh toán
            "FROM HoaDon h WHERE h.ngayTao BETWEEN :start AND :end")
    List<Object[]> countOrdersByStatusBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    @Query("SELECT COALESCE(SUM(CASE WHEN h.tongTienSauGiam > 0 THEN h.tongTienSauGiam ELSE h.tongTien END), 0) " +
            "FROM HoaDon h " +
            "WHERE h.ngayTao BETWEEN :start AND :end AND h.trangThai = :status")
    BigDecimal sumTotalAmountByDateAndStatus(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end,
                                             @Param("status") Integer status);

    // 2. Lấy 5 hóa đơn mới nhất (Trả về Entity để Service tự Map an toàn)
    List<HoaDon> findTop5ByOrderByNgayTaoDesc();

}