package sd_04.datn_fstore.service;
import sd_04.datn_fstore.model.HinhAnh;
import sd_04.datn_fstore.model.SanPham;
import java.util.List;
import java.util.Optional;

public interface HinhAnhService {
    /**
     * Lấy tất cả hình ảnh (dùng cho dropdown Khóa phụ)
     * @return List<HinhAnh>
     */
    List<HinhAnh> getAll();

    /**
     * Lưu một đối tượng Hình ảnh (thường sau khi file đã được upload)
     * @param hinhAnh Đối tượng HinhAnh
     * @return HinhAnh đã lưu
     */
    HinhAnh save(HinhAnh hinhAnh);

    // Có thể thêm các hàm khác như delete, findBySanPhamId, v.v.
}
