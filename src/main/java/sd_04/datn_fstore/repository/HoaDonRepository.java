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
    // 1. HÀM TÌM KIẾM (SEARCH) - ĐÃ CẬP NHẬT QUERY
    // =========================================================================
    // Giữ nguyên tên hàm: searchByTrangThaiAndNgayTao
    // Thay đổi: Thêm LEFT JOIN FETCH để lấy Khách hàng, Voucher, Địa chỉ
    @Query(value = "SELECT hd FROM HoaDon hd " +
            "LEFT JOIN FETCH hd.khachHang " +        // [MỚI] Lấy thông tin khách
            "LEFT JOIN FETCH hd.phieuGiamGia " +     // [MỚI] Lấy thông tin voucher
            "LEFT JOIN FETCH hd.diaChiGiaoHang " +   // [MỚI] Lấy địa chỉ giao hàng
            "WHERE " +
            "(:trangThaiList IS NULL OR hd.trangThai IN :trangThaiList) AND " +
            "(:ngayBatDau IS NULL OR hd.ngayTao >= :ngayBatDau) AND " +
            "(:ngayKetThuc IS NULL OR hd.ngayTao <= :ngayKetThuc) AND " +
            "(:keyword IS NULL OR hd.maHoaDon LIKE %:keyword% OR hd.khachHang.tenKhachHang LIKE %:keyword%) AND " +
            "(:minPrice IS NULL OR hd.tongTien >= :minPrice) AND " +
            "(:maxPrice IS NULL OR hd.tongTien <= :maxPrice)",

            countQuery = "SELECT COUNT(hd) FROM HoaDon hd WHERE " +
                    "(:trangThaiList IS NULL OR hd.trangThai IN :trangThaiList) AND " +
                    "(:ngayBatDau IS NULL OR hd.ngayTao >= :ngayBatDau) AND " +
                    "(:ngayKetThuc IS NULL OR hd.ngayTao <= :ngayKetThuc) AND " +
                    "(:keyword IS NULL OR hd.maHoaDon LIKE %:keyword% OR hd.khachHang.tenKhachHang LIKE %:keyword%) AND " +
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
    // 2. HÀM CHI TIẾT (DETAIL) - ĐÃ CẬP NHẬT QUERY
    // =========================================================================
    // Giữ nguyên tên hàm: findByIdWithDetails
    // Thay đổi: Thêm LEFT JOIN FETCH diaChiGiaoHang
    @Query("SELECT hd FROM HoaDon hd " +
            "LEFT JOIN FETCH hd.hoaDonChiTiets " +   // Lấy list sản phẩm con
            "LEFT JOIN FETCH hd.khachHang " +        // Lấy khách hàng
            "LEFT JOIN FETCH hd.nhanVien " +         // Lấy nhân viên
            "LEFT JOIN FETCH hd.phieuGiamGia " +     // Lấy voucher
            "LEFT JOIN FETCH hd.diaChiGiaoHang " +   // [MỚI] Lấy địa chỉ giao hàng
            "WHERE hd.id = :id")
    Optional<HoaDon> findByIdWithDetails(@Param("id") Integer id);

    // =========================================================================
    // 3. CÁC HÀM KHÁC (GIỮ NGUYÊN NHƯ CŨ)
    // =========================================================================

    Page<HoaDon> findByKhachHangId(Integer khachHangId, Pageable pageable);

    List<HoaDon> findByTrangThaiOrderByNgayTaoDesc(Integer trangThai);

    List<HoaDon> findByNgayTaoBetweenOrderByNgayTaoDesc(LocalDateTime startTime, LocalDateTime endTime);

    Optional<HoaDon> findByMaHoaDon(String maHoaDon);

    List<HoaDon> findByTrangThaiInOrderByNgayTaoDesc(List<Integer> trangThais);

    void deleteByMaHoaDon(String maHoaDon);

    List<HoaDon> findByNhanVienIdAndTrangThaiInOrderByNgayTaoDesc(Integer nhanVienId, List<Integer> trangThais);

    Integer countByNgayTaoBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(h.tongTien), 0) FROM HoaDon h " +
            "WHERE h.ngayTao BETWEEN :start AND :end AND h.trangThai = 4")
    BigDecimal sumTotalAmountByDateRange(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(h) FROM HoaDon h " +
            "WHERE h.trangThai = :status AND h.ngayTao BETWEEN :start AND :end")
    Integer countByStatusAndDateRange(@Param("status") Integer status,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

    @Query("SELECT new sd_04.datn_fstore.dto.RecentOrderDTO(" +
            "h.maHoaDon, " +
            "h.khachHang.tenKhachHang, " +
            "h.ngayTao, " +
            "h.tongTien, " +
            "h.trangThai) " +
            "FROM HoaDon h ORDER BY h.ngayTao DESC")
    List<RecentOrderDTO> findRecentOrders(Pageable pageable);

    @Query("SELECT " +
            "SUM(CASE WHEN h.trangThai = 0 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN h.trangThai = 1 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN h.trangThai = 2 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN h.trangThai = 3 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN h.trangThai = 4 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN h.trangThai = 5 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN h.trangThai = 6 THEN 1 ELSE 0 END) " +
            "FROM HoaDon h WHERE h.ngayTao BETWEEN :start AND :end")
    List<Object[]> countOrdersByStatusBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(CASE WHEN h.tongTienSauGiam > 0 THEN h.tongTienSauGiam ELSE h.tongTien END), 0) " +
            "FROM HoaDon h " +
            "WHERE h.ngayTao BETWEEN :start AND :end AND h.trangThai = :status")
    BigDecimal sumTotalAmountByDateAndStatus(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end,
                                             @Param("status") Integer status);

    int countByTrangThai(Integer trangThai);
    List<HoaDon> findTop5ByOrderByNgayTaoDesc();
    List<HoaDon> findAllByMaHoaDon(String maHoaDon);
    Optional<HoaDon> findTopByMaHoaDonOrderByNgayTaoDesc(String maHoaDon);
}