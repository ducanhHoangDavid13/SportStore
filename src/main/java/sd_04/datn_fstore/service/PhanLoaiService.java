package sd_04.datn_fstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sd_04.datn_fstore.model.PhanLoai;
import java.util.List;
import java.util.Optional;

public interface PhanLoaiService {
    List<PhanLoai> getAll();
    Optional<PhanLoai> getById(Integer id);
    PhanLoai save(PhanLoai phanLoai);
    void delete(Integer id);

    // Tìm kiếm, Lọc và Phân trang
    Page<PhanLoai> searchAndPaginate(Pageable pageable, String keyword, Integer trangThai);

    // Phương thức tìm kiếm không phân trang
    List<PhanLoai> searchByTen(String ten);
    List<PhanLoai> filterByTrangThai(Integer trangThai);
    List<PhanLoai> searchAndFilter(String ten, Integer trangThai);
    // ... các hàm cũ
    PhanLoai updateTrangThai(Integer id, Integer newStatus);
    List<PhanLoai> getAllActive();
}