package sd_04.datn_fstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sd_04.datn_fstore.model.XuatXu;
import java.util.List;
import java.util.Optional;

public interface XuatXuService {
    List<XuatXu> getAll();
    Optional<XuatXu> getById(Integer id);
    XuatXu save(XuatXu xuatXu);
    void delete(Integer id);

    // Tìm kiếm, Lọc và Phân trang
    Page<XuatXu> searchAndPaginate(Pageable pageable, String keyword, Integer trangThai);

    // Phương thức tìm kiếm không phân trang
    List<XuatXu> searchByTen(String ten);
    List<XuatXu> filterByTrangThai(Integer trangThai);
    List<XuatXu> searchAndFilter(String ten, Integer trangThai);
    // ... các hàm cũ
    XuatXu updateTrangThai(Integer id, Integer newStatus);
}