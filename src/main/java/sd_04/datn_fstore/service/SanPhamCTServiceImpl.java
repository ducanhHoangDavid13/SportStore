package sd_04.datn_fstore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.SanPhamChiTiet;
import sd_04.datn_fstore.repository.SanPhamCTRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SanPhamCTServiceImpl implements SanPhamCTService {

    // Đổi tên repo cho khớp với file service impl cũ của bạn
    private final SanPhamCTRepository sanPhamChiTietRepository;

    // --- CÁC PHƯƠNG THỨC CŨ (Giữ nguyên) ---
    @Override
    public List<SanPhamChiTiet> getAll() {
        return sanPhamChiTietRepository.findAll();
    }

    @Override
    public Page<SanPhamChiTiet> getAll(Pageable pageable) {
        return sanPhamChiTietRepository.findAll(pageable);
    }

    @Override
    public Optional<SanPhamChiTiet> getById(Integer id) {
        return sanPhamChiTietRepository.findById(id);
    }

    @Override
    public SanPhamChiTiet save(SanPhamChiTiet sanPhamChiTiet) {
        return sanPhamChiTietRepository.save(sanPhamChiTiet);
    }

    @Override
    public void delete(Integer id) {
        if (sanPhamChiTietRepository.existsById(id)) {
            sanPhamChiTietRepository.deleteById(id);
        }
    }

    // --- PHƯƠNG THỨC SEARCH ĐÃ CẬP NHẬT (12 THAM SỐ) ---
    @Override
    public Page<SanPhamChiTiet> search(
            Pageable pageable,
            Integer idSanPham,
            Integer idKichThuoc,
            Integer idChatLieu,  // <-- Đã khớp thứ tự với Interface
            Integer idTheLoai,   // <-- Đã khớp thứ tự với Interface
            Integer idXuatXu,
            Integer idMauSac,
            Integer idPhanLoai,  // <-- Đã khớp thứ tự với Interface
            BigDecimal minPrice, // <-- Đã khớp tên với Interface
            BigDecimal maxPrice, // <-- Đã khớp tên với Interface
            Integer trangThai,
            String keyword
    ) {

        // Bây giờ chúng ta gọi Repository với ĐÚNG 12 tham số
        return sanPhamChiTietRepository.search(
                pageable,
                idSanPham,
                idKichThuoc,
                idChatLieu,
                idTheLoai,
                idXuatXu,
                idMauSac,
                idPhanLoai,
                minPrice,
                maxPrice,
                trangThai,
                keyword
        );
    }
    @Override
    public List<SanPhamChiTiet> getAvailableProducts() {
        // Gọi Repository, đây là nơi logic thuộc về
        return sanPhamChiTietRepository.findAllByTrangThaiAndSoLuongGreaterThan(1, 0);
    }
}