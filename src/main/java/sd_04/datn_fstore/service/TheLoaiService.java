package sd_04.datn_fstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sd_04.datn_fstore.model.TheLoai;
import java.util.List;
import java.util.Optional;

public interface TheLoaiService {
    List<TheLoai> getAll();
    Optional<TheLoai> getById(Integer id);
    TheLoai save(TheLoai theLoai);
    void delete(Integer id);

    // Tìm kiếm, Lọc và Phân trang
    Page<TheLoai> searchAndPaginate(Pageable pageable, String keyword, Integer trangThai);

    // Phương thức tìm kiếm không phân trang
    List<TheLoai> searchByTen(String ten);
    List<TheLoai> filterByTrangThai(Integer trangThai);
    List<TheLoai> searchAndFilter(String ten, Integer trangThai);
    // ... các hàm cũ
    TheLoai updateTrangThai(Integer id, Integer newStatus);

    List<TheLoai> getAllActive();
}