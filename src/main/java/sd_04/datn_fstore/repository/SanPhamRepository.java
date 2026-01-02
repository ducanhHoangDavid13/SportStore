package sd_04.datn_fstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.SanPham;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {


    /**
     * Câu lệnh Query 1: Tìm kiếm sản phẩm (theo mã hoặc tên) VÀ lọc theo trạng thái,
     * kết quả trả về có phân trang (Pageable).
     */
    @Query(value = "SELECT sp FROM SanPham sp WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR sp.tenSanPham LIKE %:keyword% OR sp.maSanPham LIKE %:keyword%) AND " +
            "(:trangThai IS NULL OR sp.trangThai = :trangThai)",

            countQuery = "SELECT COUNT(sp) FROM SanPham sp WHERE " +
                    "(:keyword IS NULL OR :keyword = '' OR sp.tenSanPham LIKE %:keyword% OR sp.maSanPham LIKE %:keyword%) AND " +
                    "(:trangThai IS NULL OR sp.trangThai = :trangThai)")
    Page<SanPham> findPaginated(Pageable pageable,
                                @Param("keyword") String keyword,
                                @Param("trangThai") Integer trangThai);

    /**
     * Câu lệnh Query 2: Tìm một sản phẩm chính xác bằng mã sản phẩm (maSanPham).
     */
    Optional<SanPham> findByMaSanPham(String maSanPham);

    /**
     * Câu lệnh Query 3: Tìm kiếm sản phẩm (theo mã hoặc tên).
     */
    boolean existsByMaSanPham(String maSanPham);

    /**
     * Câu lệnh Query 4: Lọc sản phẩm nâng cao (theo nhiều tiêu chí và phân trang).
     * Đã BỔ SUNG tham số 'keyword' để khớp với API Controller.
     */
    @Query(value = """
        SELECT DISTINCT s FROM SanPham s
        LEFT JOIN s.sanPhamChiTiets ct 
        WHERE
            (:xuatXuIds IS NULL OR ct.xuatXu.id IN :xuatXuIds) AND
            (:theLoaiIds IS NULL OR ct.theLoai.id IN :theLoaiIds) AND
            (:phanLoaiIds IS NULL OR ct.phanLoai.id IN :phanLoaiIds) AND
            (:chatLieuIds IS NULL OR ct.chatLieu.id IN :chatLieuIds) AND
            (s.giaTien BETWEEN :minPrice AND :maxPrice) AND
            (:keyword IS NULL OR :keyword = '' OR s.tenSanPham LIKE %:keyword% OR s.maSanPham LIKE %:keyword%)
        """,
            countQuery = """ 
        SELECT COUNT(DISTINCT s.id) FROM SanPham s
        LEFT JOIN s.sanPhamChiTiets ct 
        WHERE
            (:xuatXuIds IS NULL OR ct.xuatXu.id IN :xuatXuIds) AND
            (:theLoaiIds IS NULL OR ct.theLoai.id IN :theLoaiIds) AND
            (:phanLoaiIds IS NULL OR ct.phanLoai.id IN :phanLoaiIds) AND
            (:chatLieuIds IS NULL OR ct.chatLieu.id IN :chatLieuIds) AND
            (s.giaTien BETWEEN :minPrice AND :maxPrice) AND
            (:keyword IS NULL OR :keyword = '' OR s.tenSanPham LIKE %:keyword% OR s.maSanPham LIKE %:keyword%)
        """)
    Page<SanPham> findFilteredProducts(
            @Param("xuatXuIds") List<Integer> xuatXuIds,
            @Param("theLoaiIds") List<Integer> theLoaiIds,
            @Param("phanLoaiIds") List<Integer> phanLoaiIds,
            @Param("chatLieuIds") List<Integer> chatLieuIds,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("keyword") String keyword, // Đã bổ sung tham số keyword
            Pageable pageable);

    // =========================================================
    // 💡 TRUY VẤN MỚI CHO LOGIC SO LUONG
    // =========================================================
    /**
     * Truy vấn tính tổng số lượng tồn kho của tất cả SanPhamChiTiet thuộc một SanPham.
     * Service sẽ gọi hàm này để cập nhật trường 'soLuong' trong SanPham.
     *
     * Giả định: Trường SanPhamChiTiet có tên biến là 'soLuong' và có quan hệ với SanPham.
     */
    @Query(value = "SELECT COALESCE(SUM(spct.soLuong), 0) FROM SanPhamChiTiet spct WHERE spct.sanPham.id = :sanPhamId")
    Integer sumQuantityBySanPhamId(@Param("sanPhamId") Integer sanPhamId);

    // =========================================================
    // ... Các truy vấn khác (Giữ nguyên)
    // =========================================================

    int countBySoLuongLessThan(Integer integer);
}