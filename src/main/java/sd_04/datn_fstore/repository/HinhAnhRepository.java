package sd_04.datn_fstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.HinhAnh;
import sd_04.datn_fstore.model.SanPham;

import java.util.List;
import java.util.Optional;

@Repository
public interface HinhAnhRepository extends JpaRepository<HinhAnh, Integer> {
    /**
     * Câu lệnh Query 1: Lấy tất cả hình ảnh của MỘT sản phẩm.
     * (Cách viết ngắn gọn dùng Spring Data JPA: List<HinhAnh> findBySanPham(SanPham sanPham);)
     */
    @Query("SELECT ha FROM HinhAnh ha WHERE ha.sanPham.id = :idSanPham")
    List<HinhAnh> findAllBySanPhamId(@Param("idSanPham") Integer idSanPham);

    /**
     * Câu lệnh Query 2: Lấy ảnh đại diện (avatar) của sản phẩm.
     * (Giả định rằng ảnh đại diện có trangThai = 1).
     */
    @Query("SELECT ha FROM HinhAnh ha WHERE ha.sanPham.id = :idSanPham AND ha.trangThai = 1")
    Optional<HinhAnh> findAvatarBySanPhamId(@Param("idSanPham") Integer idSanPham);

    /**
     * Câu lệnh Query 3: Lấy tất cả hình ảnh theo đối tượng SanPham.
     * Dùng cho logic xóa sản phẩm (phải xóa các ảnh liên quan trước).
     */
    List<HinhAnh> findBySanPham(SanPham sanPham);
}
