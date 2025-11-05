package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.ChatLieu;
import sd_04.datn_fstore.repository.ChatLieuRepository;
import sd_04.datn_fstore.service.ChatLieuService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatLieuServiceImpl implements ChatLieuService {

    private final ChatLieuRepository chatLieuRepository;

    // ... (CRUD cơ bản) ...
    @Override public List<ChatLieu> getAll() { return chatLieuRepository.findAll(); }
    @Override public Optional<ChatLieu> getById(Integer id) { return chatLieuRepository.findById(id); }
    @Override public ChatLieu save(ChatLieu chatLieu) { return chatLieuRepository.save(chatLieu); }
    @Override public void delete(Integer id) { chatLieuRepository.deleteById(id); }

    // Triển khai Phân trang (Sử dụng findPaginated)
    @Override
    public Page<ChatLieu> searchAndPaginate(Pageable pageable, String keyword, Integer trangThai) {
        return chatLieuRepository.findPaginated(pageable, keyword, trangThai);
    }

    // Phương thức tìm kiếm/lọc không phân trang (Giả định chúng gọi các phương thức tương ứng nếu cần)
    @Override public List<ChatLieu> searchByTen(String ten) {
        return chatLieuRepository.findPaginated(Pageable.unpaged(), ten, null).getContent();
    }
    @Override public List<ChatLieu> filterByTrangThai(Integer trangThai) {
        return chatLieuRepository.findPaginated(Pageable.unpaged(), null, trangThai).getContent();
    }
    @Override public List<ChatLieu> searchAndFilter(String ten, Integer trangThai) {
        return chatLieuRepository.findPaginated(Pageable.unpaged(), ten, trangThai).getContent();
    }
}