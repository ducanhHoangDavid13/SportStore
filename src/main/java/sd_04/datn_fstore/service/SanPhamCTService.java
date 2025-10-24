package sd_04.datn_fstore.service;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sd_04.datn_fstore.model.SanPhamChiTiet;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
@Service
public interface SanPhamCTService {
    // Lấy tất cả (không phân trang)
    List<SanPhamChiTiet> getAll();

    // Lấy tất cả (có phân trang)
    Page<SanPhamChiTiet> getAll(Pageable pageable);

    // Tìm theo ID
    Optional<SanPhamChiTiet> getById(Integer id);

    // Thêm mới hoặc cập nhật
    SanPhamChiTiet save(SanPhamChiTiet sanPhamChiTiet);

    // Xóa theo ID
    void delete(Integer id);

    /**
     * Hàm tìm kiếm và lọc động với đầy đủ các thuộc tính
     */
    Page<SanPhamChiTiet> search(Pageable pageable,
                                Integer idSanPham,
                                Integer idKichThuoc,
                                Integer idPhanLoai,
                                Integer idXuatXu,
                                Integer idChatLieu,
                                Integer idMauSac,
                                Integer idTheLoai,
                                BigDecimal giaMin,
                                BigDecimal giaMax,
                                Integer trangThai);
}
