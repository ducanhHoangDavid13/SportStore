package sd_04.datn_fstore.service;
import sd_04.datn_fstore.model.HinhAnh;
import sd_04.datn_fstore.model.SanPham;
import java.util.List;
import java.util.Optional;

public interface HinhAnhService {
    /**
     * Lấy tất cả hình ảnh thuộc về một sản phẩm (dựa theo ID sản phẩm).
     */
    List<HinhAnh> getBySanPhamId(Integer sanPhamId);

    /**
     * Lấy ảnh đại diện (avatar) của sản phẩm.
     */
    Optional<HinhAnh> getAvatar(Integer sanPhamId);

    /**
     * Lấy 1 ảnh theo ID của chính nó.
     */
    Optional<HinhAnh> findById(Integer id);

    /**
     * Lưu một đối tượng HinhAnh.
     * Logic nghiệp vụ: Cần gán ngày tạo/ngày sửa.
     */
    HinhAnh save(HinhAnh hinhAnh);

    /**
     * Xóa một hình ảnh theo ID của chính nó.
     * Logic nghiệp vụ quan trọng:
     * - Cần xóa file vật lý trên server/cloud.
     * - Xóa record trong CSDL.
     */
    void delete(Integer id);

    /**
     * Xóa TẤT CẢ hình ảnh liên quan đến MỘT sản phẩm.
     * Dùng khi xóa SanPham.
     */
    void deleteBySanPham(SanPham sanPham);
}
