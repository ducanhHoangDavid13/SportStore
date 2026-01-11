package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.PhanLoai;
import sd_04.datn_fstore.repository.PhanLoaiRepository;
import sd_04.datn_fstore.service.PhanLoaiService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PhanLoaiServiceImpl implements PhanLoaiService {

    private final PhanLoaiRepository phanLoaiRepository;

    // ... (CRUD cơ bản) ...
    @Override
    public List<PhanLoai> getAll() {
        return phanLoaiRepository.findAll();
    }

    @Override
    public Optional<PhanLoai> getById(Integer id) {
        return phanLoaiRepository.findById(id);
    }

    @Override
    public PhanLoai save(PhanLoai phanLoai) {
        return phanLoaiRepository.save(phanLoai);
    }

    @Override
    public void delete(Integer id) {
        phanLoaiRepository.deleteById(id);
    }

    // Triển khai Phân trang (Sử dụng findPaginated)
    @Override
    public Page<PhanLoai> searchAndPaginate(Pageable pageable, String keyword, Integer trangThai) {
        return phanLoaiRepository.findPaginated(pageable, keyword, trangThai);
    }

    // Phương thức tìm kiếm/lọc không phân trang
    @Override
    public List<PhanLoai> searchByTen(String ten) {
        return phanLoaiRepository.findPaginated(Pageable.unpaged(), ten, null).getContent();
    }

    @Override
    public List<PhanLoai> filterByTrangThai(Integer trangThai) {
        return phanLoaiRepository.findPaginated(Pageable.unpaged(), null, trangThai).getContent();
    }

    @Override
    public List<PhanLoai> searchAndFilter(String ten, Integer trangThai) {
        return phanLoaiRepository.findPaginated(Pageable.unpaged(), ten, trangThai).getContent();
    }

    @Override
    public PhanLoai updateTrangThai(Integer id, Integer newStatus) {
        Optional<PhanLoai> optional = phanLoaiRepository.findById(id);
        if (optional.isPresent()) {
            PhanLoai entity = optional.get();
            entity.setTrangThai(newStatus);
            return phanLoaiRepository.save(entity);
        } else {
            throw new RuntimeException("Không tìm thấy phân loại ID: " + id);
        }
    }

    @Override
    public List<PhanLoai> getAllActive() {
        // Giả định trạng thái hoạt động được lưu là true hoặc số 1
        // Nếu Model có trường 'trangThai' kiểu boolean (true = active)
        return phanLoaiRepository.findByTrangThai(1);
    }
}