package sd_04.datn_fstore.service;
import sd_04.datn_fstore.model.HinhAnh;
import sd_04.datn_fstore.model.SanPham;
import java.util.List;
import java.util.Optional;

public interface HinhAnhService {

    List<HinhAnh> getAll();

    List<HinhAnh> getBySanPhamId(Integer sanPhamId);

    Optional<HinhAnh> getAvatar(Integer sanPhamId);

    Optional<HinhAnh> findById(Integer id);

    HinhAnh save(HinhAnh hinhAnh);

    void deleteById(Integer id); // <-- SỬA TÊN PHƯƠNG THỨC Ở ĐÂY

    void deleteBySanPham(SanPham sanPham);
}