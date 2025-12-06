package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.model.HinhAnh;
import sd_04.datn_fstore.model.SanPham;
import sd_04.datn_fstore.model.SanPhamChiTiet;
import sd_04.datn_fstore.repository.SanPhamCTRepository;
import sd_04.datn_fstore.service.HinhAnhService;
import sd_04.datn_fstore.service.SanPhamCTService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SanPhamCTServiceImpl implements SanPhamCTService {

    private final SanPhamCTRepository sanPhamChiTietRepository;
    private final HinhAnhService hinhAnhService;

    @Override
    public List<SanPhamChiTiet> getAll() {
        List<SanPhamChiTiet> list = sanPhamChiTietRepository.findAll();
        list.forEach(this::loadTenHinhAnhChinh);
        return list;
    }

    @Override
    public Page<SanPhamChiTiet> getAll(Pageable pageable) {
        Page<SanPhamChiTiet> page = sanPhamChiTietRepository.findAll(pageable);
        page.getContent().forEach(this::loadTenHinhAnhChinh);
        return page;
    }

    @Override
    public Optional<SanPhamChiTiet> getById(Integer id) {
        Optional<SanPhamChiTiet> optSpct = sanPhamChiTietRepository.findById(id);
        optSpct.ifPresent(this::loadTenHinhAnhChinh);
        return optSpct;
    }

    @Override
    @Transactional
    public SanPhamChiTiet save(SanPhamChiTiet sanPhamChiTiet) {
        sanPhamChiTiet.setTrangThai(1);
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
        Page<SanPhamChiTiet> page = sanPhamChiTietRepository.search(
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
        page.getContent().forEach(this::loadTenHinhAnhChinh);
        return page;
    }

    @Override
    public List<SanPhamChiTiet> getAvailableProducts() {
        List<SanPhamChiTiet> list = sanPhamChiTietRepository.getAvailableProductsWithDetails(1, 0);
        list.forEach(this::loadTenHinhAnhChinh);
        return list;
    }

    @Override
    public List<SanPhamChiTiet> searchBySanPhamTen(String tenSp) {
        List<SanPhamChiTiet> list = sanPhamChiTietRepository.findBySanPhamTenSanPham(tenSp);
        list.forEach(this::loadTenHinhAnhChinh);
        return list;
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

    // 💡 PHƯƠNG THỨC ĐÃ SỬA: Lấy danh sách SPCT theo ID Sản phẩm (cha)
    @Override
    public List<SanPhamChiTiet> getBySanPhamId(Integer id) {
        // GIẢ ĐỊNH: findBySanPhamId đã được định nghĩa trong SanPhamCTRepository
        List<SanPhamChiTiet> list = sanPhamChiTietRepository.findBySanPhamId(id);
        list.forEach(this::loadTenHinhAnhChinh);
        return list;
    }

    private void loadTenHinhAnhChinh(SanPhamChiTiet spct) {
        if (spct.getSanPham() == null) {
            return;
        }

        SanPham sanPhamCha = spct.getSanPham();
        Integer sanPhamId = sanPhamCha.getId();

        Optional<HinhAnh> avatarOpt = hinhAnhService.getAvatar(sanPhamId);

        if (avatarOpt.isPresent()) {
            sanPhamCha.setTenHinhAnhChinh(avatarOpt.get().getTenHinhAnh());
        } else {
            List<HinhAnh> allImages = hinhAnhService.getBySanPhamId(sanPhamId);
            if (!allImages.isEmpty()) {
                sanPhamCha.setTenHinhAnhChinh(allImages.get(0).getTenHinhAnh());
            }
        }
    }

    @Override
    public SanPhamChiTiet getByIdAndAvailable(Integer id) {
        // Giả định: findByIdAndAvailable đã được định nghĩa trong SanPhamCTRepository
        Optional<SanPhamChiTiet> optional = sanPhamChiTietRepository.findByIdAndAvailable(id);

        if (optional.isPresent()) {
            SanPhamChiTiet spct = optional.get();
            loadTenHinhAnhChinh(spct);
            return spct;
        }
        return null;
    }
}