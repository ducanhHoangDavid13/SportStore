package sd_04.datn_fstore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.SanPham;
import sd_04.datn_fstore.repository.SanPhamRepository;
import sd_04.datn_fstore.service.SanPhamService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SanPhamServiceImpl implements SanPhamService {

    private final SanPhamRepository sanPhamRepository;

    @Override
    public List<SanPham> getAll() {
        return sanPhamRepository.findAll();
    }

    @Override
    public Optional<SanPham> getById(Integer id) {
        return sanPhamRepository.findById(id);
    }

    @Override
    public SanPham save(SanPham sanPham) {
        return sanPhamRepository.save(sanPham);
    }

    @Override
    public void delete(Integer id) {
        if (sanPhamRepository.existsById(id)) {
            sanPhamRepository.deleteById(id);
        }
    }

    @Override
    public Page<SanPham> searchAndPaginate(Pageable pageable, String keyword, Integer trangThai) {
        // Xử lý keyword nếu nó là rỗng
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }
        return sanPhamRepository.findPaginated(pageable, keyword, trangThai);
    }

    @Override
    public boolean existsByMaSanPham(String maSanPham) {
        return sanPhamRepository.findByMaSanPham(maSanPham).isPresent();
    }
}