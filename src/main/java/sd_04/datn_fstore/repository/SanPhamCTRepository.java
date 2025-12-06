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
    Page<SanPhamChiTiet> search(
            Pageable pageable,
            @Param("idSanPham") Integer idSanPham,
            @Param("idKichThuoc") Integer idKichThuoc,
            @Param("idChatLieu") Integer idChatLichieu,
            @Param("idTheLoai") Integer idTheLoai,
            @Param("idXuatXu") Integer idXuatXu,
            @Param("idMauSac") Integer idMauSac,
            @Param("idPhanLoai") Integer idPhanLoai,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("trangThai") Integer trangThai,
            @Param("keyword") String keyword
    );

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

}