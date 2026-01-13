package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.model.HinhAnh;
import sd_04.datn_fstore.model.SanPham;
import sd_04.datn_fstore.repository.HinhAnhRepository;
import sd_04.datn_fstore.service.FileStorageService;
import sd_04.datn_fstore.service.HinhAnhService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
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
        // Đã sửa: Tìm kiếm ảnh có trạng thái = 1 (avatar)
        return hinhAnhRepo.findFirstBySanPhamIdAndTrangThai(sanPhamId, 1);
    }

    @Override
    public Optional<HinhAnh> findById(Integer id) {
        return hinhAnhRepo.findById(id);
    }

    @Override
    @Transactional
    public HinhAnh save(HinhAnh hinhAnh) {
        // Sử dụng LocalDateTime cho ngày tạo/sửa
        if (hinhAnh.getId() == null) {
            hinhAnh.setNgayTao(LocalDateTime.now());
        }
        hinhAnh.setNgaySua(LocalDateTime.now());

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
                log.error("Lỗi khi xóa file vật lý: {} | Error: {}", tenHinhAnh, e.getMessage());
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
                log.error("Lỗi xóa file batch: {}", img.getTenHinhAnh());
            }
        }
        hinhAnhRepo.deleteAll(hinhAnhs);
    }

    @Override
    @Transactional
    public void deleteAvatarBySanPhamId(Integer sanPhamId) {
        // SỬA: Dùng findFirstBy... để tìm ảnh có trạng thái = 1 (avatar)
        Optional<HinhAnh> optionalAvatar = hinhAnhRepo.findFirstBySanPhamIdAndTrangThai(sanPhamId, 1);

        if (optionalAvatar.isPresent()) {
            HinhAnh avatarToDelete = optionalAvatar.get();

            // 1. Xóa file vật lý
            try {
                fileStorageService.deleteFile(avatarToDelete.getTenHinhAnh());
            } catch (Exception e) {
                // Dùng log.warn để ghi nhận cảnh báo thay vì System.err.println
                log.warn("Cảnh báo: Không thể xóa file vật lý {} cho Sản phẩm ID {}. Tiếp tục xóa khỏi DB.",
                        avatarToDelete.getTenHinhAnh(), sanPhamId, e);
            }

            // 2. Xóa Entity khỏi DB
            hinhAnhRepo.delete(avatarToDelete);
        }
    }
}