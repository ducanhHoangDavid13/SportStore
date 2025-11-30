package sd_04.datn_fstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sd_04.datn_fstore.model.KichThuoc;
import java.util.List;
import java.util.Optional;

public interface KichThuocService {
    List<KichThuoc> getAll();
    Optional<KichThuoc> getById(Integer id);
    KichThuoc save(KichThuoc kichThuoc);
    void delete(Integer id);

    // Tìm kiếm, Lọc và Phân trang
    Page<KichThuoc> searchAndPaginate(Pageable pageable, String keyword, Integer trangThai);

    // Phương thức tìm kiếm không phân trang
    List<KichThuoc> searchByTen(String ten);
    List<KichThuoc> filterByTrangThai(Integer trangThai);
    List<KichThuoc> searchAndFilter(String ten, Integer trangThai);
    // ... các hàm cũ
    KichThuoc updateTrangThai(Integer id, Integer newStatus);
}