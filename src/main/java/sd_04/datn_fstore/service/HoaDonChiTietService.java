package sd_04.datn_fstore.service;

import sd_04.datn_fstore.model.HoaDonChiTiet;
import java.util.List;
import java.util.Optional;

public interface HoaDonChiTietService {

    List<HoaDonChiTiet> getAll();

    Optional<HoaDonChiTiet> getById(Integer id);

    HoaDonChiTiet save(HoaDonChiTiet hoaDonChiTiet);

    void deleteById(Integer id);

    List<HoaDonChiTiet> findByHoaDonId(Integer hoaDonId);

    long countBySanPhamChiTietId(Integer sanPhamChiTietId);

    // --- BỔ SUNG NẾU CẦN THIẾT ---
    /**
     * Xuất PDF cho Hóa đơn cha, nhưng được gọi thông qua ID chi tiết
     * (Hàm này vẫn phải tìm HoaDon cha để xuất)
     */
    byte[] exportHoaDonByChiTietId(Integer hoaDonChiTietId);
}