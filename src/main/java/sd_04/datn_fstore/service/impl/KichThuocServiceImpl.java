package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.KichThuoc;
import sd_04.datn_fstore.repository.KichThuocRepository;
import sd_04.datn_fstore.service.KichThuocService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KichThuocServiceImpl implements KichThuocService {

    private final KichThuocRepository kichThuocRepository;

    // ... (CRUD cơ bản) ...
    @Override public List<KichThuoc> getAll() { return kichThuocRepository.findAll(); }
    @Override public Optional<KichThuoc> getById(Integer id) { return kichThuocRepository.findById(id); }
    @Override public KichThuoc save(KichThuoc kichThuoc) { return kichThuocRepository.save(kichThuoc); }
    @Override public void delete(Integer id) { kichThuocRepository.deleteById(id); }

    // Triển khai Phân trang (Sử dụng findPaginated)
    @Override
    public Page<KichThuoc> searchAndPaginate(Pageable pageable, String keyword, Integer trangThai) {
        return kichThuocRepository.findPaginated(pageable, keyword, trangThai);
    }

    // Phương thức tìm kiếm/lọc không phân trang
    @Override public List<KichThuoc> searchByTen(String ten) {
        return kichThuocRepository.findPaginated(Pageable.unpaged(), ten, null).getContent();
    }
    @Override public List<KichThuoc> filterByTrangThai(Integer trangThai) {
        return kichThuocRepository.findPaginated(Pageable.unpaged(), null, trangThai).getContent();
    }
    @Override public List<KichThuoc> searchAndFilter(String ten, Integer trangThai) {
        return kichThuocRepository.findPaginated(Pageable.unpaged(), ten, trangThai).getContent();
    }
    @Override
    public KichThuoc updateTrangThai(Integer id, Integer newStatus) {
        Optional<KichThuoc> optional = kichThuocRepository.findById(id);
        if (optional.isPresent()) {
            KichThuoc entity = optional.get();
            entity.setTrangThai(newStatus);
            return kichThuocRepository.save(entity);
        } else {
            throw new RuntimeException("Không tìm thấy kích thước ID: " + id);
        }
    }

    @Override
    public List<KichThuoc> getAllActive() {
        return kichThuocRepository.findByTrangThai(1);
    }
}