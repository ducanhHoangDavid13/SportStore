package sd_04.datn_fstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.SanPham;

import java.util.Optional;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {

    /**
     * Câu lệnh Query 1: Tìm kiếm sản phẩm (theo mã hoặc tên) VÀ lọc theo trạng thái,
     * kết quả trả về có phân trang (Pageable).
     * Đây là câu lệnh cốt lõi cho trang danh sách sản phẩm.
     */
    @Query("SELECT sp FROM SanPham sp WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR sp.tenSanPham LIKE %:keyword% OR sp.maSanPham LIKE %:keyword%) AND " +
            "(:trangThai IS NULL OR sp.trangThai = :trangThai)")
    Page<SanPham> findPaginated(Pageable pageable,
                                @Param("keyword") String keyword,
                                @Param("trangThai") Integer trangThai);

    /**
     * Câu lệnh Query 2: Tìm một sản phẩm chính xác bằng mã sản phẩm (maSanPham).
     * Dùng để kiểm tra xem mã SP đã tồn tại hay chưa khi thêm mới.
     */
    Optional<SanPham> findByMaSanPham(String maSanPham);
} 