package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.model.SanPhamChiTiet;
import sd_04.datn_fstore.repository.SanPhamCTRepository;
import sd_04.datn_fstore.service.SanPhamCTService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SanPhamCTServiceImpl implements SanPhamCTService {

    private final SanPhamCTRepository sanPhamChiTietRepository;

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
    @Transactional
    public SanPhamChiTiet save(SanPhamChiTiet sanPhamChiTiet) {
        sanPhamChiTiet.setTrangThai(1);

        sanPhamChiTietRepository.save(sanPhamChiTiet);
        return sanPhamChiTietRepository.save(sanPhamChiTiet);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Optional<SanPhamChiTiet> optional = sanPhamChiTietRepository.findById(id);
        if (optional.isPresent()) {
            SanPhamChiTiet spct = optional.get();
            spct.setTrangThai(0);
            sanPhamChiTietRepository.save(spct);
        }
    }

    @Override
    public Page<SanPhamChiTiet> search(
            Pageable pageable,
            Integer idSanPham,
            Integer idKichThuoc,
            Integer idChatLieu,
            Integer idTheLoai,
            Integer idXuatXu,
            Integer idMauSac,
            Integer idPhanLoai,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer trangThai,
            String keyword
    ) {
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
        return sanPhamChiTietRepository.getAvailableProductsWithDetails(1, 0);
    }

    @Override
    public List<SanPhamChiTiet> searchBySanPhamTen(String tenSp) {
        return sanPhamChiTietRepository.findBySanPhamTenSanPham(tenSp);
    }
    @Override
    public SanPhamChiTiet updateTrangThai(Integer id, Integer newStatus) {
        Optional<SanPhamChiTiet> optional = sanPhamChiTietRepository.findById(id);
        if (optional.isPresent()) {
            SanPhamChiTiet spct = optional.get();
            spct.setTrangThai(newStatus);
            return sanPhamChiTietRepository.save(spct);
        } else {
            throw new RuntimeException("Không tìm thấy biến thể sản phẩm ID: " + id);
        }
    }

    @Override
    public List<SanPhamChiTiet> getBySanPhamId(Integer id) {
        return List.of();
    }
}