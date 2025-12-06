package sd_04.datn_fstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sd_04.datn_fstore.model.SanPhamChiTiet;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SanPhamCTService {

    // CRUD CƠ BẢN
    List<SanPhamChiTiet> getAll();
    Page<SanPhamChiTiet> getAll(Pageable pageable);
    Optional<SanPhamChiTiet> getById(Integer id);
    SanPhamChiTiet save(SanPhamChiTiet sanPhamChiTiet);
    void delete(Integer id);

    // SEARCH 10 THAM SỐ (Đã bỏ MinPrice, MaxPrice)
    Page<SanPhamChiTiet> search(
            Pageable pageable,
            Integer idSanPham,
            Integer idKichThuoc,
            Integer idChatLieu,
            Integer idTheLoai,
            Integer idXuatXu,
            Integer idMauSac,
            Integer idPhanLoai,
            Integer trangThai,
            String keyword
    );

    // Lấy biến thể active của 1 sản phẩm (Cho khách hàng chọn size/màu)
    List<SanPhamChiTiet> getAvailableProducts(Integer idSanPham);

    // Tìm nhanh theo tên
    List<SanPhamChiTiet> searchBySanPhamTen(String tenSp);

    // Đổi trạng thái nhanh
    SanPhamChiTiet updateTrangThai(Integer id, Integer newStatus);

    // Lấy tất cả biến thể của 1 sản phẩm (Cho Admin quản lý)
    List<SanPhamChiTiet> getBySanPhamId(Integer idSanPham);
    List<SanPhamChiTiet> getAllActive();
    List<SanPhamChiTiet> timTheoKhoangGia(BigDecimal maxPrice);
}