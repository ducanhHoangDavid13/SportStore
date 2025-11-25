package sd_04.datn_fstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sd_04.datn_fstore.model.MauSac;
import java.util.List;
import java.util.Optional;

public interface MauSacService {
    // CRUD Cơ bản
    List<MauSac> getAll();
    Optional<MauSac> getById(Integer id);
    MauSac save(MauSac mauSac);
    void delete(Integer id);

    // Tìm kiếm, Lọc và Phân trang (Sử dụng phương thức findPaginated của Repo)
    Page<MauSac> searchAndPaginate(Pageable pageable, String keyword, Integer trangThai);

    // Phương thức tìm kiếm không phân trang (Được giữ lại cho tính nhất quán API/Logic)
    // Sẽ gọi findPaginated bên trong để đảm bảo logic tìm kiếm đồng nhất.
    List<MauSac> searchByTen(String ten);
    List<MauSac> filterByTrangThai(Integer trangThai);
    List<MauSac> searchAndFilter(String ten, Integer trangThai);

    // Phương thức tìm kiếm chính xác (Nếu còn tồn tại trong Repo, hoặc được triển khai qua findPaginated)
    Optional<MauSac> findByMaMau(String maMau);
    MauSac updateTrangThai(Integer id, Integer newStatus);
}