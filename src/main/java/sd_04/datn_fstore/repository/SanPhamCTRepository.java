package sd_04.datn_fstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.dto.TopProductDTO;
import sd_04.datn_fstore.model.KichThuoc;
import sd_04.datn_fstore.model.MauSac;
import sd_04.datn_fstore.model.SanPhamChiTiet;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamCTRepository extends JpaRepository<SanPhamChiTiet, Integer> {

<<<<<<< HEAD
    // 1. Tìm kiếm và phân trang phức tạp (Dùng trong search method của Service)
    @Query("SELECT spct FROM SanPhamChiTiet spct " +
            "WHERE (:idSanPham IS NULL OR spct.sanPham.id = :idSanPham) " +
            "AND (:idKichThuoc IS NULL OR spct.kichThuoc.id = :idKichThuoc) " +
            "AND (:idChatLieu IS NULL OR spct.chatLieu.id = :idChatLieu) " +
            "AND (:idTheLoai IS NULL OR spct.theLoai.id = :idTheLoai) " +
            "AND (:idXuatXu IS NULL OR spct.xuatXu.id = :idXuatXu) " +
            "AND (:idMauSac IS NULL OR spct.mauSac.id = :idMauSac) " +
            "AND (:idPhanLoai IS NULL OR spct.phanLoai.id = :idPhanLoai) " +
            "AND (:trangThai IS NULL OR spct.trangThai = :trangThai) " +
            "AND (:minPrice IS NULL OR spct.giaTien >= :minPrice) " +
            "AND (:maxPrice IS NULL OR spct.giaTien <= :maxPrice) " +
            "AND (:keyword IS NULL OR " +
            "spct.sanPham.tenSanPham LIKE %:keyword% OR " +
            "spct.mauSac.tenMauSac LIKE %:keyword% OR " +
            "spct.kichThuoc.tenKichThuoc LIKE %:keyword%)")
=======
    // --- 1. TÌM KIẾM NÂNG CAO (ĐÃ BỎ MIN/MAX PRICE) ---
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
            "(:trangThai IS NULL OR spct.trangThai = :trangThai) AND " +
            "(" +
            ":keyword IS NULL OR :keyword = '' OR " +
            "sp.tenSanPham LIKE CONCAT('%', :keyword, '%') OR " +
            "sp.maSanPham LIKE CONCAT('%', :keyword, '%') OR " +
            "CONCAT(spct.id, '') LIKE CONCAT('%', :keyword, '%')" +
            ")",
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
                    "(:trangThai IS NULL OR spct.trangThai = :trangThai) AND " +
                    "(" +
                    ":keyword IS NULL OR :keyword = '' OR " +
                    "sp.tenSanPham LIKE CONCAT('%', :keyword, '%') OR " +
                    "sp.maSanPham LIKE CONCAT('%', :keyword, '%') OR " +
                    "CONCAT(spct.id, '') LIKE CONCAT('%', :keyword, '%')" +
                    ")")
>>>>>>> 88acbdeaf7b068cb7c67e415a4bf4650adf7384d
    Page<SanPhamChiTiet> search(
            Pageable pageable,
            @Param("idSanPham") Integer idSanPham,
            @Param("idKichThuoc") Integer idKichThuoc,
            @Param("idChatLieu") Integer idChatLichieu,
            @Param("idTheLoai") Integer idTheLoai,
            @Param("idXuatXu") Integer idXuatXu,
            @Param("idMauSac") Integer idMauSac,
            @Param("idPhanLoai") Integer idPhanLoai,
            @Param("trangThai") Integer trangThai,
            @Param("keyword") String keyword
    );

<<<<<<< HEAD
    // 2. Lấy danh sách SPCT theo ID Sản phẩm (Cha)
    List<SanPhamChiTiet> findBySanPhamId(Integer id);

    // 3. Lấy các SPCT đang hoạt động và có tồn kho > 0 (Dùng cho getAvailableProducts)
    @Query("SELECT spct FROM SanPhamChiTiet spct WHERE spct.trangThai = :active AND spct.soLuong > :minStock")
    List<SanPhamChiTiet> getAvailableProductsWithDetails(@Param("active") Integer active, @Param("minStock") Integer minStock);

    // 4. Tìm kiếm theo Tên Sản phẩm (Cha)
    List<SanPhamChiTiet> findBySanPhamTenSanPham(String tenSp);

    // 5. Tìm kiếm theo ID và trạng thái có sẵn (Trạng thái = 1)
    @Query("SELECT spct FROM SanPhamChiTiet spct WHERE spct.id = :id AND spct.trangThai = 1")
    Optional<SanPhamChiTiet> findByIdAndAvailable(@Param("id") Integer id);

    // 6. Đếm SPCT có số lượng <= giá trị cho trước (Dùng trong DashboardServiceImpl)
    long countBySoLuongLessThanEqual(int soLuong);

    // 7. PHƯƠNG THỨC ĐÃ THÊM: Lấy danh sách SPCT có số lượng <= giá trị cho trước.
    // 💡 Giải quyết lỗi 'cannot find symbol: method findBySoLuongLessThanEqual(int)'
    List<SanPhamChiTiet> findBySoLuongLessThanEqual(int soLuong);

    // 8. Lấy Top sản phẩm bán chạy nhất (Dùng trong DashboardServiceImpl)
    @Query("SELECT NEW sd_04.datn_fstore.dto.TopProductDTO(spct.sanPham.tenSanPham, SUM(hdct.soLuong)) " +
            "FROM HoaDonChiTiet hdct JOIN hdct.sanPhamChiTiet spct " +
            "GROUP BY spct.sanPham.tenSanPham " +
            "ORDER BY SUM(hdct.soLuong) DESC")
    List<TopProductDTO> findTopSellingProducts(Pageable pageable);
    // LẤY DANH SÁCH MÀU THEO ID SẢN PHẨM
    @Query("SELECT DISTINCT spct.mauSac FROM SanPhamChiTiet spct " +
            "WHERE spct.sanPham.id = :idSanPham AND spct.soLuong > 0")
    List<MauSac> findMauBySanPham(@Param("idSanPham") Integer idSanPham);


    // LẤY DANH SÁCH SIZE THEO ID SẢN PHẨM
    @Query("SELECT DISTINCT spct.kichThuoc FROM SanPhamChiTiet spct " +
            "WHERE spct.sanPham.id = :idSanPham AND spct.soLuong > 0")
    List<KichThuoc> findSizeBySanPham(@Param("idSanPham") Integer idSanPham);

=======
    // --- 2. CÁC HÀM BỔ SUNG ĐỂ KHỚP SERVICE ---

    // Lấy tất cả biến thể của 1 sản phẩm (Dùng cho Admin xem chi tiết SP)
    List<SanPhamChiTiet> findBySanPhamId(Integer idSanPham);

    // Lấy các biến thể ĐANG BÁN của 1 sản phẩm (Dùng cho Client chọn mua)
    @Query("SELECT spct FROM SanPhamChiTiet spct WHERE spct.sanPham.id = :idSanPham AND spct.trangThai = 1 AND spct.soLuong > 0")
    List<SanPhamChiTiet> findAvailableVariants(@Param("idSanPham") Integer idSanPham);

    // Tìm theo tên sản phẩm
    @Query("SELECT spct FROM SanPhamChiTiet spct JOIN spct.sanPham sp WHERE sp.tenSanPham LIKE CONCAT('%', :tenSp, '%')")
    List<SanPhamChiTiet> findBySanPhamTenSanPham(@Param("tenSp") String tenSp);

    // --- 3. API BÁN HÀNG & KHÁC (GIỮ NGUYÊN) ---
    @Query("SELECT DISTINCT spct FROM SanPhamChiTiet spct " +
            "JOIN FETCH spct.sanPham sp " +
            "LEFT JOIN FETCH sp.hinhAnh " +
            "WHERE spct.trangThai = :trangThai AND spct.soLuong > :soLuong " +
            "ORDER BY sp.ngayTao DESC")
    List<SanPhamChiTiet> getAvailableProductsWithDetails(@Param("trangThai") Integer trangThai, @Param("soLuong") Integer soLuong);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT spct FROM SanPhamChiTiet spct WHERE spct.id = :id")
    Optional<SanPhamChiTiet> findByIdWithLock(@Param("id") Integer id);

    @Query("SELECT spct FROM SanPhamChiTiet spct WHERE spct.id = :id AND spct.trangThai = 1")
    Optional<SanPhamChiTiet> findByIdAndAvailable(@Param("id") Integer id);

    @Query("SELECT new sd_04.datn_fstore.dto.TopProductDTO(" +
            "sp.tenSanPham, " +
            "SUM(hdct.soLuong), " +
            "spct.giaTien, " +
            "MAX(img.tenHinhAnh)) " +
            "FROM HoaDonChiTiet hdct " +
            "JOIN hdct.sanPhamChiTiet spct " +
            "JOIN spct.sanPham sp " +
            "LEFT JOIN sp.hinhAnh img " +
            "GROUP BY sp.id, sp.tenSanPham, spct.giaTien " +
            "ORDER BY SUM(hdct.soLuong) DESC")
    List<TopProductDTO> findTopSellingProducts(Pageable pageable);

    List<SanPhamChiTiet> findBySoLuongLessThanEqual(Integer threshold);

    // 2. Đếm số lượng SPCT sắp hết hàng (Lưu ý: count trả về long)
    int countBySoLuongLessThanEqual(Integer threshold);
    List<SanPhamChiTiet> findBySanPham_GiaTienLessThanEqual(BigDecimal price);
>>>>>>> 88acbdeaf7b068cb7c67e415a4bf4650adf7384d
}