package sd_04.datn_fstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.HinhAnh;
import sd_04.datn_fstore.model.SanPham;

import java.util.List;
import java.util.Optional;

@Repository
public interface HinhAnhRepository extends JpaRepository<HinhAnh, Integer>, JpaSpecificationExecutor<HinhAnh> {

    /**
     * 1. Lấy TẤT CẢ hình ảnh của MỘT sản phẩm dựa trên ID.
     */
    List<HinhAnh> findAllBySanPhamId(Integer sanPhamId);

    /**
     * 2. TÌM ẢNH ĐẠI DIỆN (AVATAR) của sản phẩm dựa trên thuộc tính 'trangThai'.
     *
     * Giả định: trangThai = 1 là Ảnh đại diện (Avatar/Ảnh chính).
     * Phương thức này thay thế cho `findBySanPhamIdAndAvatarTrue` đã bị lỗi.
     * Cần đảm bảo chỉ có 1 ảnh có trangThai = 1 cho mỗi sản phẩm.
     */
    Optional<HinhAnh> findBySanPhamIdAndTrangThai(Integer sanPhamId, Integer trangThai);

    /**
     * 3. Lấy hình ảnh đầu tiên có trangThai cụ thể (thường dùng để tìm Avatar).
     * (Giữ lại nếu bạn muốn đảm bảo chỉ lấy một, nhưng phương thức trên là đủ)
     */
    Optional<HinhAnh> findFirstBySanPhamIdAndTrangThai(Integer sanPhamId, Integer trangThai);

    /**
     * 4. Lấy tất cả hình ảnh liên quan đến một đối tượng SanPham (dùng cho logic xóa cascading).
     */
    List<HinhAnh> findBySanPham(SanPham sanPham);

    /**
     * 5. Xóa tất cả hình ảnh liên quan đến một sản phẩm.
     * Cần được đánh dấu @Transactional trong Service khi gọi phương thức này.
     */
    void deleteAllBySanPhamId(Integer sanPhamId);
}