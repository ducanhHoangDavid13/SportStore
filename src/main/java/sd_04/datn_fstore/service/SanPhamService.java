package sd_04.datn_fstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sd_04.datn_fstore.model.SanPham;

import java.util.List;
import java.util.Optional;

public interface SanPhamService {

    // Lấy tất cả (không phân trang)
    List<SanPham> getAll();

    // Tìm theo ID
    Optional<SanPham> getById(Integer id);

    // Thêm mới hoặc cập nhật
    SanPham save(SanPham sanPham);

    // Xóa theo ID
    void delete(Integer id);

    /**
     * Tìm kiếm và phân trang theo keyword (mã hoặc tên) và trạng thái.
     * @param pageable Phân trang
     * @param keyword Từ khóa tìm kiếm (mã hoặc tên)
     * @param trangThai Trạng thái (null nếu không lọc)
     * @return Trang kết quả
     */
    Page<SanPham> searchAndPaginate(Pageable pageable, String keyword, Integer trangThai);

    /**
     * Kiểm tra xem mã sản phẩm đã tồn tại hay chưa.
     * @param maSanPham Mã sản phẩm cần kiểm tra
     * @return true nếu tồn tại, false nếu không
     */
    boolean existsByMaSanPham(String maSanPham);
    SanPham updateTrangThai(Integer id, Integer newStatus);

}