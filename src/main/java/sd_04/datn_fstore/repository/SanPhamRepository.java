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
     * ĐÃ THÊM countQuery để khắc phục lỗi Hibernate/JPA khi tự động tạo truy vấn đếm phức tạp.
     */
    @Query(value = "SELECT sp FROM SanPham sp WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR sp.tenSanPham LIKE %:keyword% OR sp.maSanPham LIKE %:keyword%) AND " +
            "(:trangThai IS NULL OR sp.trangThai = :trangThai)",

            // >>>>> PHẦN BỔ SUNG ĐỂ KHẮC PHỤC LỖI :keyword_1 <<<<<
            countQuery = "SELECT COUNT(sp) FROM SanPham sp WHERE " +
                    "(:keyword IS NULL OR :keyword = '' OR sp.tenSanPham LIKE %:keyword% OR sp.maSanPham LIKE %:keyword%) AND " +
                    "(:trangThai IS NULL OR sp.trangThai = :trangThai)")
    Page<SanPham> findPaginated(Pageable pageable,
                                @Param("keyword") String keyword,
                                @Param("trangThai") Integer trangThai);

    /**
     * Câu lệnh Query 2: Tìm một sản phẩm chính xác bằng mã sản phẩm (maSanPham).
     * Không cần sửa, vì đây là truy vấn đơn giản, không cần phân trang.
     */
    Optional<SanPham> findByMaSanPham(String maSanPham);

    @Query(value = """
        SELECT DISTINCT s FROM SanPham s
        LEFT JOIN s.sanPhamChiTiets ct 
        WHERE
            (:xuatXuIds IS NULL OR ct.xuatXu.id IN :xuatXuIds) AND
            (:theLoaiIds IS NULL OR ct.theLoai.id IN :theLoaiIds) AND
            (:phanLoaiIds IS NULL OR ct.phanLoai.id IN :phanLoaiIds) AND
            (:chatLieuIds IS NULL OR ct.chatLieu.id IN :chatLieuIds) AND
            (s.giaTien BETWEEN :minPrice AND :maxPrice)
        """,
            // CUNG CẤP TRUY VẤN COUNT RÕ RÀNG để tránh lỗi ánh xạ tham số
            countQuery = """ 
        SELECT COUNT(DISTINCT s.id) FROM SanPham s
        LEFT JOIN s.sanPhamChiTiets ct 
        WHERE
            (:xuatXuIds IS NULL OR ct.xuatXu.id IN :xuatXuIds) AND
            (:theLoaiIds IS NULL OR ct.theLoai.id IN :theLoaiIds) AND
            (:phanLoaiIds IS NULL OR ct.phanLoai.id IN :phanLoaiIds) AND
            (:chatLieuIds IS NULL OR ct.chatLieu.id IN :chatLieuIds) AND
            (s.giaTien BETWEEN :minPrice AND :maxPrice)
        """)
    Page<SanPham> findFilteredProducts(
            @Param("xuatXuIds") List<Integer> xuatXuIds,
            @Param("theLoaiIds") List<Integer> theLoaiIds,
            @Param("phanLoaiIds") List<Integer> phanLoaiIds,
            @Param("chatLieuIds") List<Integer> chatLieuIds,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);
}