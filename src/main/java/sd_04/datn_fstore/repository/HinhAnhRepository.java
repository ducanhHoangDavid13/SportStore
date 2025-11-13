package sd_04.datn_fstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.HinhAnh;
import sd_04.datn_fstore.model.SanPham;

import java.util.List;
import java.util.Optional;

@Repository
public interface HinhAnhRepository extends JpaRepository<HinhAnh, Integer>, JpaSpecificationExecutor<HinhAnh> {

    // Lấy tất cả hình ảnh của MỘT sản phẩm
    @Query("SELECT ha FROM HinhAnh ha WHERE ha.sanPham.id = :idSanPham")
    List<HinhAnh> findAllBySanPhamId(@Param("idSanPham") Integer idSanPham);

    // THAY THẾ: Dùng phương thức đặt tên để chỉ lấy KẾT QUẢ ĐẦU TIÊN (LIMIT 1)
    // Giải quyết lỗi NonUniqueResultException. TrangThai=1 được giả định là ảnh đại diện.
    Optional<HinhAnh> findFirstBySanPhamIdAndTrangThai(Integer sanPhamId, Integer trangThai);

    // Lấy tất cả hình ảnh theo đối tượng SanPham (dùng cho logic xóa cascading)
    List<HinhAnh> findBySanPham(SanPham sanPham);
}