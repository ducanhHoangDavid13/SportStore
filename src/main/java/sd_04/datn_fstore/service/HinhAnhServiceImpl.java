package sd_04.datn_fstore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.model.HinhAnh;
import sd_04.datn_fstore.model.SanPham;
import sd_04.datn_fstore.repository.HinhAnhRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HinhAnhServiceImpl implements HinhAnhService {

    private final HinhAnhRepository hinhAnhRepo;
    private final FileStorageService fileStorageService; // Cần thiết để xóa file vật lý

    @Override
    public List<HinhAnh> getAll() {
        return hinhAnhRepo.findAll();
    }

//    @Override
//    public Optional<HinhAnh> getById(Integer id) { // <-- Đồng bộ với HinhAnhService
//        return hinhAnhRepo.findById(id);
//    }

    @Override
    public List<HinhAnh> getBySanPhamId(Integer sanPhamId) {
        return hinhAnhRepo.findAllBySanPhamId(sanPhamId);
    }

    /**
     * Đã áp dụng FIX LỖI NonUniqueResultException:
     * Dùng findFirstBy... trên Repository để giới hạn kết quả trả về là 1.
     */
    @Override
    public Optional<HinhAnh> getAvatar(Integer sanPhamId) {
        // Gọi phương thức Repository đã được sửa
        // Giả định trangThai = 1 là ảnh đại diện
        return hinhAnhRepo.findFirstBySanPhamIdAndTrangThai(sanPhamId, 1);
    }

    @Override
    public Optional<HinhAnh> findById(Integer id) {
        return Optional.empty();
    }

    @Override
    @Transactional
    public HinhAnh save(HinhAnh hinhAnh) {
        if (hinhAnh.getId() == null) {
            hinhAnh.setNgayTao(new Date());
        }
        hinhAnh.setNgaySua(new Date());
        return hinhAnhRepo.save(hinhAnh);
    }
    /**
     * Xóa 1 ảnh: Xóa file vật lý trên server và record DB
     */
    @Override
    @Transactional
    public void deleteById(Integer id) { // <-- Đồng bộ với HinhAnhService
        Optional<HinhAnh> opt = hinhAnhRepo.findById(id);
        if (opt.isPresent()) {
            HinhAnh hinhAnh = opt.get();
            String tenHinhAnh = hinhAnh.getTenHinhAnh();

            // 1. Xóa file vật lý khỏi server
            try {
                fileStorageService.deleteFile(tenHinhAnh);
            } catch (Exception e) {
                System.err.println("Lỗi khi xóa file vật lý: " + tenHinhAnh + " | Error: " + e.getMessage());
            }

            // 2. Xóa record trong CSDL
            hinhAnhRepo.delete(hinhAnh);
        }
    }

    /**
     * Xóa tất cả ảnh của 1 sản phẩm: Dùng khi xóa SanPham
     */
    @Override
    @Transactional
    public void deleteBySanPham(SanPham sanPham) {
        // 1. Lấy danh sách tất cả ảnh của sản phẩm
        List<HinhAnh> hinhAnhs = hinhAnhRepo.findBySanPham(sanPham);

        // 2. Lặp qua và xóa từng file vật lý
        for (HinhAnh img : hinhAnhs) {
            try {
                fileStorageService.deleteFile(img.getTenHinhAnh());
            } catch (Exception e) {
                System.err.println("Lỗi khi xóa file vật lý (Batch Delete): " + img.getTenHinhAnh() + " | Error: " + e.getMessage());
            }
        }

        // 3. Xóa tất cả record DB liên quan
        hinhAnhRepo.deleteAll(hinhAnhs);
    }
}