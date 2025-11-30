package sd_04.datn_fstore.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.dto.TopProductDTO;
import sd_04.datn_fstore.model.SanPhamChiTiet;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamCTRepository extends JpaRepository<SanPhamChiTiet, Integer> {

    /**
     * 1. TÌM KIẾM NÂNG CAO
     * - Đã sửa: LIKE %:keyword% -> LIKE CONCAT('%', :keyword, '%')
     */
    @Query(value = "SELECT spct FROM SanPhamChiTiet spct " +
            "LEFT JOIN spct.sanPham sp " +
            "LEFT JOIN spct.mauSac ms " +
            "LEFT JOIN spct.kichThuoc kt " +
            "LEFT JOIN spct.chatLieu cl " +
            "LEFT JOIN spct.theLoai tl " +
            "LEFT JOIN spct.xuatXu xx " +
            "LEFT JOIN spct.phanLoai pl " +
            "WHERE " +
            "(:idSanPham IS NULL OR sp.id = :idSanPham) AND " +
            "(:idMauSac IS NULL OR ms.id = :idMauSac) AND " +
            "(:idKichThuoc IS NULL OR kt.id = :idKichThuoc) AND " +
            "(:idChatLieu IS NULL OR cl.id = :idChatLieu) AND " +
            "(:idTheLoai IS NULL OR tl.id = :idTheLoai) AND " +
            "(:idXuatXu IS NULL OR xx.id = :idXuatXu) AND " +
            "(:idPhanLoai IS NULL OR pl.id = :idPhanLoai) AND " +
            "(:minPrice IS NULL OR spct.giaTien >= :minPrice) AND " +
            "(:maxPrice IS NULL OR spct.giaTien <= :maxPrice) AND " +
            "(:trangThai IS NULL OR spct.trangThai = :trangThai) AND " +
            "(:keyword IS NULL OR :keyword = '' OR sp.tenSanPham LIKE CONCAT('%', :keyword, '%'))",

            countQuery = "SELECT COUNT(spct) FROM SanPhamChiTiet spct " +
                    "LEFT JOIN spct.sanPham sp " +
                    "LEFT JOIN spct.mauSac ms " +
                    "LEFT JOIN spct.kichThuoc kt " +
                    "LEFT JOIN spct.chatLieu cl " +
                    "LEFT JOIN spct.theLoai tl " +
                    "LEFT JOIN spct.xuatXu xx " +
                    "LEFT JOIN spct.phanLoai pl " +
                    "WHERE " +
                    "(:idSanPham IS NULL OR sp.id = :idSanPham) AND " +
                    "(:idMauSac IS NULL OR ms.id = :idMauSac) AND " +
                    "(:idKichThuoc IS NULL OR kt.id = :idKichThuoc) AND " +
                    "(:idChatLieu IS NULL OR cl.id = :idChatLieu) AND " +
                    "(:idTheLoai IS NULL OR tl.id = :idTheLoai) AND " +
                    "(:idXuatXu IS NULL OR xx.id = :idXuatXu) AND " +
                    "(:idPhanLoai IS NULL OR pl.id = :idPhanLoai) AND " +
                    "(:minPrice IS NULL OR spct.giaTien >= :minPrice) AND " +
                    "(:maxPrice IS NULL OR spct.giaTien <= :maxPrice) AND " +
                    "(:trangThai IS NULL OR spct.trangThai = :trangThai) AND " +
                    "(:keyword IS NULL OR :keyword = '' OR sp.tenSanPham LIKE CONCAT('%', :keyword, '%'))")
    Page<SanPhamChiTiet> search(
            Pageable pageable,
            @Param("idSanPham") Integer idSanPham,
            @Param("idKichThuoc") Integer idKichThuoc,
            @Param("idChatLieu") Integer idChatLieu,
            @Param("idTheLoai") Integer idTheLoai,
            @Param("idXuatXu") Integer idXuatXu,
            @Param("idMauSac") Integer idMauSac,
            @Param("idPhanLoai") Integer idPhanLoai,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("trangThai") Integer trangThai,
            @Param("keyword") String keyword
    );

    /**
     * 2. API BÁN HÀNG (POS)
     */
    @Query("SELECT DISTINCT spct FROM SanPhamChiTiet spct " +
            "JOIN FETCH spct.sanPham sp " +
            "LEFT JOIN FETCH sp.hinhAnh " +
            "LEFT JOIN FETCH spct.kichThuoc " +
            "LEFT JOIN FETCH spct.phanLoai " +
            "LEFT JOIN FETCH spct.xuatXu " +
            "LEFT JOIN FETCH spct.chatLieu " +
            "LEFT JOIN FETCH spct.mauSac " +
            "LEFT JOIN FETCH spct.theLoai " +
            "WHERE spct.trangThai = :trangThai AND spct.soLuong > :soLuong " +
            "ORDER BY sp.ngayTao DESC")
    List<SanPhamChiTiet> getAvailableProductsWithDetails(@Param("trangThai") Integer trangThai, @Param("soLuong") Integer soLuong);

    /**
     * 3. TRỪ KHO AN TOÀN (Locking)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT spct FROM SanPhamChiTiet spct WHERE spct.id = :id")
    Optional<SanPhamChiTiet> findByIdWithLock(@Param("id") Integer id);

    /**
     * 4. TOP SẢN PHẨM BÁN CHẠY
     * - Đã sửa: 'images' -> 'hinhAnh' (để khớp với hàm POS)
     * - Đã sửa: 'donGia' -> 'giaTien' (để khớp với hàm Search)
     * - Đã sửa: Logic lấy ảnh đại diện (dùng MAX để tránh lỗi Group By)
     */
    @Query("SELECT new sd_04.datn_fstore.dto.TopProductDTO(" +
            "sp.tenSanPham, " +
            "SUM(hdct.soLuong), " +
            "spct.giaTien, " +          // Sửa: donGia -> giaTien
            "MAX(img.tenHinhAnh)) " +     // Sửa: Lấy 1 ảnh bất kỳ để không vỡ Group By
            "FROM HoaDonChiTiet hdct " +
            "JOIN hdct.sanPhamChiTiet spct " +
            "JOIN spct.sanPham sp " +
            "LEFT JOIN sp.hinhAnh img " + // Sửa: images -> hinhAnh
            "GROUP BY sp.id, sp.tenSanPham, spct.giaTien " +
            "ORDER BY SUM(hdct.soLuong) DESC")
    List<TopProductDTO> findTopSellingProducts(Pageable pageable);

    /**
     * 5. CÁC HÀM TIỆN ÍCH KHÁC
     */
    List<SanPhamChiTiet> findByTrangThai(Integer trangThai);

    @Query("SELECT spct FROM SanPhamChiTiet spct JOIN spct.sanPham sp WHERE sp.tenSanPham LIKE CONCAT('%', :tenSp, '%')")
    List<SanPhamChiTiet> findBySanPhamTenSanPham(@Param("tenSp") String tenSp);

    // JPA Query Method
    List<SanPhamChiTiet> findByTrangThaiAndSoLuongGreaterThan(Integer trangThai, Integer minSoLuong);

    // Đếm sản phẩm sắp hết hàng
    Integer countBySoLuongLessThanEqual(Integer threshold);
}