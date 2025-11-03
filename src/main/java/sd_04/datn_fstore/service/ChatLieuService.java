package sd_04.datn_fstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sd_04.datn_fstore.model.ChatLieu;
import java.util.List;
import java.util.Optional;

public interface ChatLieuService {
    // CRUD Cơ bản
    List<ChatLieu> getAll();
    Optional<ChatLieu> getById(Integer id);
    ChatLieu save(ChatLieu chatLieu);
    void delete(Integer id);

    // Tìm kiếm, Lọc và Phân trang (Sử dụng phương thức findPaginated của Repo)
    Page<ChatLieu> searchAndPaginate(Pageable pageable, String keyword, Integer trangThai);

    // Phương thức tìm kiếm không phân trang (Giữ lại cho API hoặc các mục đích khác)
    List<ChatLieu> searchByTen(String ten);
    List<ChatLieu> filterByTrangThai(Integer trangThai);
    List<ChatLieu> searchAndFilter(String ten, Integer trangThai);
}