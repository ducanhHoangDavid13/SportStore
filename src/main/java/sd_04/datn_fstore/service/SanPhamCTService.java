package sd_04.datn_fstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sd_04.datn_fstore.model.SanPhamChiTiet;
import java.math.BigDecimal;
import java.util.List; // Phải thêm
import java.util.Optional; // Phải thêm

public interface SanPhamCTService {

    // THÊM LẠI CÁC PHƯƠNG THỨC CRUD CƠ BẢN
    List<SanPhamChiTiet> getAll();
    Page<SanPhamChiTiet> getAll(Pageable pageable);
    Optional<SanPhamChiTiet> getById(Integer id);
    SanPhamChiTiet save(SanPhamChiTiet sanPhamChiTiet);
    void delete(Integer id);

    // PHƯƠNG THỨC SEARCH 12 THAM SỐ (Đã cập nhật)
    Page<SanPhamChiTiet> search(
            Pageable pageable,
            Integer idSanPham,
            Integer idKichThuoc,
            Integer idChatLieu,
            Integer idTheLoai,
            Integer idXuatXu,
            Integer idMauSac,
            Integer idPhanLoai,
            BigDecimal minPrice, // Đã thêm
            BigDecimal maxPrice, // Đã thêm
            Integer trangThai,
            String keyword      // Đã thêm
    );
}