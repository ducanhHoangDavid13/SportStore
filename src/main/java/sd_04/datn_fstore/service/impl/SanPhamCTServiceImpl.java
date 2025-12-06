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
import sd_04.datn_fstore.repository.SanPhamRepository;
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
    private final SanPhamRepository sanPhamRepository;

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
        if (sanPhamChiTiet.getId() == null) {
            sanPhamChiTiet.setTrangThai(1);
        }

        // [LOGIC MỚI] Tự động lấy giá tiền từ Sản Phẩm cha nếu có
        if (sanPhamChiTiet.getSanPham() != null && sanPhamChiTiet.getSanPham().getId() != null) {
            // Tìm sản phẩm cha trong DB để đảm bảo lấy được giá mới nhất
            SanPham spCha = sanPhamRepository.findById(sanPhamChiTiet.getSanPham().getId()).orElse(null);
            if (spCha != null) {
                // Gán giá cha cho con
                sanPhamChiTiet.setGiaTien(spCha.getGiaTien());
                // Gán lại object cha để chắc chắn
                sanPhamChiTiet.setSanPham(spCha);
            }
        }

        SanPhamChiTiet savedSpct = sanPhamChiTietRepository.save(sanPhamChiTiet);

        if (savedSpct.getSanPham() != null) {
            updateTotalQuantitySanPham(savedSpct.getSanPham().getId());
        }
        return savedSpct;
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Optional<SanPhamChiTiet> optional = sanPhamChiTietRepository.findById(id);
        if (optional.isPresent()) {
            SanPhamChiTiet spct = optional.get();
            Integer sanPhamId = spct.getSanPham().getId();

            spct.setTrangThai(0); // Soft delete
            sanPhamChiTietRepository.save(spct);
            updateTotalQuantitySanPham(sanPhamId);
        }
    }

    @Override
    @Transactional
    public SanPhamChiTiet updateTrangThai(Integer id, Integer newStatus) {
        Optional<SanPhamChiTiet> optional = sanPhamChiTietRepository.findById(id);
        if (optional.isPresent()) {
            SanPhamChiTiet spct = optional.get();
            spct.setTrangThai(newStatus);
            SanPhamChiTiet saved = sanPhamChiTietRepository.save(spct);
            updateTotalQuantitySanPham(spct.getSanPham().getId());
            return saved;
        } else {
            throw new RuntimeException("Không tìm thấy biến thể sản phẩm ID: " + id);
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
                trangThai,
                keyword
        );
        page.getContent().forEach(this::loadTenHinhAnhChinh);
        return page;
    }

    @Override
    public List<SanPhamChiTiet> getAvailableProducts(Integer idSanPham) {
        List<SanPhamChiTiet> list = sanPhamChiTietRepository.findAvailableVariants(idSanPham);
        list.forEach(this::loadTenHinhAnhChinh);
        return list;
    }

    @Override
    public List<SanPhamChiTiet> getAllActive() {
        List<SanPhamChiTiet> list = sanPhamChiTietRepository.getAvailableProductsWithDetails(1, 0);
        list.forEach(this::loadTenHinhAnhChinh);
        return list;
    }

    @Override
    public List<SanPhamChiTiet> timTheoKhoangGia(BigDecimal maxPrice) {
        return sanPhamChiTietRepository.findBySanPham_GiaTienLessThanEqual(maxPrice);
    }

    @Override
    public List<SanPhamChiTiet> getBySanPhamId(Integer idSanPham) {
        List<SanPhamChiTiet> list = sanPhamChiTietRepository.findBySanPhamId(idSanPham);
        list.forEach(this::loadTenHinhAnhChinh);
        return list;
    }

    @Override
    public List<SanPhamChiTiet> searchBySanPhamTen(String tenSp) {
        List<SanPhamChiTiet> list = sanPhamChiTietRepository.findBySanPhamTenSanPham(tenSp);
        list.forEach(this::loadTenHinhAnhChinh);
        return list;
    }

    private void updateTotalQuantitySanPham(Integer sanPhamId) {
        SanPham sanPham = sanPhamRepository.findById(sanPhamId).orElse(null);
        if (sanPham != null) {
            int total = 0;
            List<SanPhamChiTiet> variants = sanPhamChiTietRepository.findBySanPhamId(sanPhamId);
            for (SanPhamChiTiet ct : variants) {
                if (ct.getTrangThai() == 1 && ct.getSoLuong() != null) {
                    total += ct.getSoLuong();
                }
            }
            sanPham.setSoLuong(total);
            sanPhamRepository.save(sanPham);
        }
    }

    private void loadTenHinhAnhChinh(SanPhamChiTiet spct) {
        if (spct.getSanPham() == null) return;
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
    // SanPhamCTServiceImpl.java

// ... (các imports và constructor)

    // Phương thức mới:
    @Override
    public void updateBatchTotalQuantity(List<SanPham> sanPhamList) {
        for (SanPham sanPham : sanPhamList) {
            // Ta không cần truy vấn lại sanPhamRepository.findById(sanPham.getId())
            // vì đối tượng sanPham đã có trong List.
            int total = 0;
            // Dùng phương thức findBySanPhamId để lấy tất cả biến thể của sản phẩm này
            List<SanPhamChiTiet> variants = sanPhamChiTietRepository.findBySanPhamId(sanPham.getId());

            for (SanPhamChiTiet ct : variants) {
                if (ct.getTrangThai() == 1 && ct.getSoLuong() != null) {
                    total += ct.getSoLuong();
                }
            }
            // Chỉ cần set SoLuong vào đối tượng trong List, không cần gọi save()
            // vì nó không phải là hàm cập nhật DB.
            sanPham.setSoLuong(total);
        }
    }

// ... (giữ nguyên hàm private void updateTotalQuantitySanPham cũ)
}