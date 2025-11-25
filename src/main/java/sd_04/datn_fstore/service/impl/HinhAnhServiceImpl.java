package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.model.HinhAnh;
import sd_04.datn_fstore.model.SanPham;
import sd_04.datn_fstore.repository.HinhAnhRepository;
import sd_04.datn_fstore.service.FileStorageService;
import sd_04.datn_fstore.service.HinhAnhService;

import java.time.LocalDateTime; // <--- Import mới
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HinhAnhServiceImpl implements HinhAnhService {

    private final HinhAnhRepository hinhAnhRepo;
    private final FileStorageService fileStorageService;

    @Override
    public List<HinhAnh> getAll() {
        return hinhAnhRepo.findAll();
    }

    @Override
    public List<HinhAnh> getBySanPhamId(Integer sanPhamId) {
        return hinhAnhRepo.findAllBySanPhamId(sanPhamId);
    }

    @Override
    public Optional<HinhAnh> getAvatar(Integer sanPhamId) {
        return hinhAnhRepo.findFirstBySanPhamIdAndTrangThai(sanPhamId, 1);
    }

    @Override
    public Optional<HinhAnh> findById(Integer id) {
        // --- ĐÃ SỬA LẠI (Code cũ trả về empty là sai logic) ---
        return hinhAnhRepo.findById(id);
    }

    @Override
    @Transactional
    public HinhAnh save(HinhAnh hinhAnh) {
        // --- SỬA ĐỔI LocalDateTime ---
        if (hinhAnh.getId() == null) {
            hinhAnh.setNgayTao(LocalDateTime.now()); // Thay new Date()
        }
        hinhAnh.setNgaySua(LocalDateTime.now());     // Thay new Date()
        // -----------------------------

        return hinhAnhRepo.save(hinhAnh);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        Optional<HinhAnh> opt = hinhAnhRepo.findById(id);
        if (opt.isPresent()) {
            HinhAnh hinhAnh = opt.get();
            String tenHinhAnh = hinhAnh.getTenHinhAnh();

            // 1. Xóa file vật lý
            try {
                fileStorageService.deleteFile(tenHinhAnh);
            } catch (Exception e) {
                System.err.println("Lỗi khi xóa file vật lý: " + tenHinhAnh + " | Error: " + e.getMessage());
            }

            // 2. Xóa record DB
            hinhAnhRepo.delete(hinhAnh);
        }
    }

    @Override
    @Transactional
    public void deleteBySanPham(SanPham sanPham) {
        List<HinhAnh> hinhAnhs = hinhAnhRepo.findBySanPham(sanPham);

        for (HinhAnh img : hinhAnhs) {
            try {
                fileStorageService.deleteFile(img.getTenHinhAnh());
            } catch (Exception e) {
                System.err.println("Lỗi xóa file batch: " + img.getTenHinhAnh());
            }
        }
        hinhAnhRepo.deleteAll(hinhAnhs);
    }
}