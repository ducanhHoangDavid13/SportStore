package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.TheLoai;
import sd_04.datn_fstore.repository.TheLoaiRepository;
import sd_04.datn_fstore.service.TheLoaiService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TheLoaiServiceImpl implements TheLoaiService {

    private final TheLoaiRepository theLoaiRepository;

    // ... (CRUD cơ bản) ...
    @Override public List<TheLoai> getAll() { return theLoaiRepository.findAll(); }
    @Override public Optional<TheLoai> getById(Integer id) { return theLoaiRepository.findById(id); }
    @Override public TheLoai save(TheLoai theLoai) { return theLoaiRepository.save(theLoai); }
    @Override public void delete(Integer id) { theLoaiRepository.deleteById(id); }

    // Triển khai Phân trang (Sử dụng findPaginated)
    @Override
    public Page<TheLoai> searchAndPaginate(Pageable pageable, String keyword, Integer trangThai) {
        return theLoaiRepository.findPaginated(pageable, keyword, trangThai);
    }

    // Phương thức tìm kiếm/lọc không phân trang
    @Override public List<TheLoai> searchByTen(String ten) {
        return theLoaiRepository.findPaginated(Pageable.unpaged(), ten, null).getContent();
    }
    @Override public List<TheLoai> filterByTrangThai(Integer trangThai) {
        return theLoaiRepository.findPaginated(Pageable.unpaged(), null, trangThai).getContent();
    }
    @Override public List<TheLoai> searchAndFilter(String ten, Integer trangThai) {
        return theLoaiRepository.findPaginated(Pageable.unpaged(), ten, trangThai).getContent();
    }
}