package sd_04.datn_fstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.SanPhamChiTiet;

import java.math.BigDecimal;

@Repository
public interface SanPhamCTRepository extends JpaRepository<SanPhamChiTiet, Integer> {

    /**
     * Câu lệnh Query: Tìm kiếm và lọc SanPhamChiTiet theo tất cả các thuộc tính
     * liên quan và các trường dữ liệu khác, trả về kết quả phân trang.
     *
     * JPQL cho phép chúng ta truy cập ID của entity liên kết trực tiếp
     * (ví dụ: spct.sanPham.id) mà không cần JOIN tường minh.
     */
    @Query("SELECT spct FROM SanPhamChiTiet spct WHERE " +
            "(:idSanPham IS NULL OR spct.sanPham.id = :idSanPham) AND " +
            "(:idKichThuoc IS NULL OR spct.kichThuoc.id = :idKichThuoc) AND " +
            "(:idPhanLoai IS NULL OR spct.phanLoai.id = :idPhanLoai) AND " +
            "(:idXuatXu IS NULL OR spct.xuatXu.id = :idXuatXu) AND " +
            "(:idChatLieu IS NULL OR spct.chatLieu.id = :idChatLieu) AND " +
            "(:idMauSac IS NULL OR spct.mauSac.id = :idMauSac) AND " +
            "(:idTheLoai IS NULL OR spct.theLoai.id = :idTheLoai) AND " +
            "(:giaMin IS NULL OR spct.giaTien >= :giaMin) AND " +
            "(:giaMax IS NULL OR spct.giaTien <= :giaMax) AND " +
            "(:trangThai IS NULL OR spct.trangThai = :trangThai)")
    Page<SanPhamChiTiet> search(Pageable pageable,
                                @Param("idSanPham") Integer idSanPham,
                                @Param("idKichThuoc") Integer idKichThuoc,
                                @Param("idPhanLoai") Integer idPhanLoai,
                                @Param("idXuatXu") Integer idXuatXu,
                                @Param("idChatLieu") Integer idChatLieu,
                                @Param("idMauSac") Integer idMauSac,
                                @Param("idTheLoai") Integer idTheLoai,
                                @Param("giaMin") BigDecimal giaMin,
                                @Param("giaMax") BigDecimal giaMax,
                                @Param("trangThai") Integer trangThai);
}