package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.model.HinhAnh;
import sd_04.datn_fstore.model.SanPham;
import sd_04.datn_fstore.model.SanPhamChiTiet;
import sd_04.datn_fstore.repository.SanPhamCTRepository; // [MỚI]
import sd_04.datn_fstore.repository.SanPhamRepository;
import sd_04.datn_fstore.service.HinhAnhService;
import sd_04.datn_fstore.service.SanPhamService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SanPhamServiceImpl implements SanPhamService {

    private final SanPhamRepository sanPhamRepository;
    private final SanPhamCTRepository sanPhamCTRepository; // [MỚI] Inject thêm cái này
    private final HinhAnhService hinhAnhService;

    @Override
    public List<SanPham> getAll() {
        List<SanPham> sanPhamList = sanPhamRepository.findAll();
        sanPhamList.forEach(this::loadHinhAnhChinh);
        return sanPhamList;
    }

    @Override
    public Optional<SanPham> getById(Integer id) {
        Optional<SanPham> sanPhamOpt = sanPhamRepository.findById(id);
        sanPhamOpt.ifPresent(this::loadHinhAnhChinh);
        return sanPhamOpt;
    }

    @Override
    @Transactional
    public SanPham save(SanPham sanPham) {
        // 1. Lưu sản phẩm cha trước
        SanPham savedSanPham = sanPhamRepository.save(sanPham);

        // 2. [LOGIC MỚI] Đồng bộ giá cho tất cả sản phẩm chi tiết (con)
        if (savedSanPham.getId() != null) {
            List<SanPhamChiTiet> listCon = sanPhamCTRepository.findBySanPhamId(savedSanPham.getId());
            if (!listCon.isEmpty()) {
                for (SanPhamChiTiet chiTiet : listCon) {
                    // Gán giá của con = giá của cha
                    chiTiet.setGiaTien(savedSanPham.getGiaTien());
                }
                // Lưu lại danh sách con đã cập nhật giá
                sanPhamCTRepository.saveAll(listCon);
            }
        }

        return savedSanPham;
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Optional<SanPham> sanPhamOpt = sanPhamRepository.findById(id);
        if (sanPhamOpt.isPresent()) {
            SanPham sanPham = sanPhamOpt.get();
            hinhAnhService.deleteBySanPham(sanPham);
            sanPhamRepository.delete(sanPham);
        }
    }

    @Override
    public Page<SanPham> searchAndPaginate(Pageable pageable, String keyword, Integer trangThai) {
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }
        Page<SanPham> sanPhamPage = sanPhamRepository.findPaginated(pageable, keyword, trangThai);
        sanPhamPage.getContent().forEach(this::loadHinhAnhChinh);
        return sanPhamPage;
    }

    private void loadHinhAnhChinh(SanPham sanPham) {
        Optional<HinhAnh> avatarOpt = hinhAnhService.getAvatar(sanPham.getId());
        if (avatarOpt.isPresent()) {
            sanPham.setTenHinhAnhChinh(avatarOpt.get().getTenHinhAnh());
        } else {
            List<HinhAnh> allImages = hinhAnhService.getBySanPhamId(sanPham.getId());
            if (!allImages.isEmpty()) {
                sanPham.setTenHinhAnhChinh(allImages.get(0).getTenHinhAnh());
            }
        }
    }

    @Override
    public boolean existsByMaSanPham(String maSanPham) {
        return sanPhamRepository.findByMaSanPham(maSanPham).isPresent();
    }

    @Override
    public SanPham updateTrangThai(Integer id, Integer newStatus) {
        Optional<SanPham> optionalSanPham = sanPhamRepository.findById(id);
        if (optionalSanPham.isPresent()) {
            SanPham sanPham = optionalSanPham.get();
            sanPham.setTrangThai(newStatus);
            return sanPhamRepository.save(sanPham);
        } else {
            throw new RuntimeException("Không tìm thấy sản phẩm có ID: " + id);
        }
    }
    // Tìm đến cuối file SanPhamServiceImpl.java và thay thế hàm updateTotalQuantity cũ bằng hàm này:

    @Override // Nhớ đảm bảo Interface SanPhamService có định nghĩa hàm này
    public void updateTotalQuantity(Integer sanPhamId) {
        // 1. Lấy sản phẩm cha
        SanPham sanPham = sanPhamRepository.findById(sanPhamId).orElse(null);
        if (sanPham == null) return;

        // 2. Lấy danh sách biến thể con
        List<SanPhamChiTiet> variants = sanPhamCTRepository.findBySanPhamId(sanPhamId);

        int total = 0;
        // 3. Tính tổng (Chỉ cộng các biến thể ĐANG HOẠT ĐỘNG)
        for (SanPhamChiTiet ct : variants) {
            if (ct.getTrangThai() != null && ct.getTrangThai() == 1 && ct.getSoLuong() != null) {
                total += ct.getSoLuong();
            }
        }

        // 4. Cập nhật số lượng
        sanPham.setSoLuong(total);

        // 5. 🔥 LOGIC TỰ ĐỘNG TẮT/MỞ TRẠNG THÁI 🔥
        if (total <= 0) {
            sanPham.setTrangThai(0); // Hết hàng -> Ngừng bán
        } else {
            // Nếu muốn tự động mở bán lại khi có hàng thì dùng dòng dưới, không thì comment lại
            sanPham.setTrangThai(1);
        }

        // 6. Lưu lại
        sanPhamRepository.save(sanPham);
    }
}