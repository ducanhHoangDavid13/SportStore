package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.XuatXu;
import sd_04.datn_fstore.repository.XuatXuRepository;
import sd_04.datn_fstore.service.XuatXuService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class XuatXuServiceImpl implements XuatXuService {

    private final XuatXuRepository xuatXuRepository;

    // ... (CRUD cơ bản) ...
    @Override public List<XuatXu> getAll() { return xuatXuRepository.findAll(); }
    @Override public Optional<XuatXu> getById(Integer id) { return xuatXuRepository.findById(id); }
    @Override public XuatXu save(XuatXu xuatXu) { return xuatXuRepository.save(xuatXu); }
    @Override public void delete(Integer id) { xuatXuRepository.deleteById(id); }

    // Triển khai Phân trang (Sử dụng findPaginated)
    @Override
    public Page<XuatXu> searchAndPaginate(Pageable pageable, String keyword, Integer trangThai) {
        return xuatXuRepository.findPaginated(pageable, keyword, trangThai);
    }

    // Phương thức tìm kiếm/lọc không phân trang
    @Override public List<XuatXu> searchByTen(String ten) {
        return xuatXuRepository.findPaginated(Pageable.unpaged(), ten, null).getContent();
    }
    @Override public List<XuatXu> filterByTrangThai(Integer trangThai) {
        return xuatXuRepository.findPaginated(Pageable.unpaged(), null, trangThai).getContent();
    }
    @Override public List<XuatXu> searchAndFilter(String ten, Integer trangThai) {
        return xuatXuRepository.findPaginated(Pageable.unpaged(), ten, trangThai).getContent();
    }
}