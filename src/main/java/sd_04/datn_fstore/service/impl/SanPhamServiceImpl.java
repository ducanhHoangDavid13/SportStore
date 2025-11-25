package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.model.HinhAnh;
import sd_04.datn_fstore.model.SanPham;
import sd_04.datn_fstore.repository.SanPhamRepository;
import sd_04.datn_fstore.service.HinhAnhService;
import sd_04.datn_fstore.service.SanPhamService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SanPhamServiceImpl implements SanPhamService {

    private final SanPhamRepository sanPhamRepository;
    private final HinhAnhService hinhAnhService; // Đã tiêm

    @Override
    public List<SanPham> getAll() {
        // Có thể cần bổ sung logic tải ảnh chính tại đây nếu API này được dùng.
        List<SanPham> sanPhamList = sanPhamRepository.findAll();
        sanPhamList.forEach(this::loadHinhAnhChinh);
        return sanPhamList;
    }

    @Override
    public Optional<SanPham> getById(Integer id) {
        Optional<SanPham> sanPhamOpt = sanPhamRepository.findById(id);

        // Tải tên ảnh chính khi lấy chi tiết (cho modal Edit)
        sanPhamOpt.ifPresent(this::loadHinhAnhChinh);

        return sanPhamOpt;
    }

    @Override
    @Transactional
    public SanPham save(SanPham sanPham) {
        return sanPhamRepository.save(sanPham);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Optional<SanPham> sanPhamOpt = sanPhamRepository.findById(id);

        if (sanPhamOpt.isPresent()) {
            SanPham sanPham = sanPhamOpt.get();

            // 1. Xóa TẤT CẢ hình ảnh liên quan (file vật lý và record DB)
            hinhAnhService.deleteBySanPham(sanPham);

            // 2. Xóa sản phẩm chính
            sanPhamRepository.delete(sanPham);
        }
    }

    @Override
    public Page<SanPham> searchAndPaginate(Pageable pageable, String keyword, Integer trangThai) {
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }

        Page<SanPham> sanPhamPage = sanPhamRepository.findPaginated(pageable, keyword, trangThai);

        // Tải tên ảnh chính cho từng sản phẩm trong trang
        sanPhamPage.getContent().forEach(this::loadHinhAnhChinh);

        return sanPhamPage;
    }

    /**
     * Phương thức dùng chung để tải tên ảnh đại diện và gán vào trường transient.
     */
    private void loadHinhAnhChinh(SanPham sanPham) {
        // 1. Ưu tiên lấy ảnh đại diện (giả định: trangThai = 1)
        Optional<HinhAnh> avatarOpt = hinhAnhService.getAvatar(sanPham.getId());

        if (avatarOpt.isPresent()) {
            // Gán tên file vào trường @Transient tenHinhAnhChinh
            sanPham.setTenHinhAnhChinh(avatarOpt.get().getTenHinhAnh());
        } else {
            // 2. Nếu không có, lấy ảnh đầu tiên trong danh sách ảnh chi tiết
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
        // 1. Tìm sản phẩm trong DB
        Optional<SanPham> optionalSanPham = sanPhamRepository.findById(id);

        if (optionalSanPham.isPresent()) {
            SanPham sanPham = optionalSanPham.get();

            // 2. Cập nhật trạng thái mới
            sanPham.setTrangThai(newStatus);

            // 3. Lưu lại vào DB
            return sanPhamRepository.save(sanPham);
        } else {
            // Ném lỗi nếu không tìm thấy ID (Sẽ được Controller bắt hoặc Global Exception Handler)
            throw new RuntimeException("Không tìm thấy sản phẩm có ID: " + id);
        }
    }

//    @Override
//    public List<SanPham> findAllForExport(String keyword, Integer trangThai) {
//        // Phương thức này phụ thuộc vào Repository của bạn
//        // Giả sử Repository có hàm findAllForExport
//        List<SanPham> sanPhamList = sanPhamRepository.findAllForExport(keyword, trangThai);
//
//        // Tải tên ảnh chính cho từng sản phẩm trong danh sách xuất file
//        sanPhamList.forEach(this::loadHinhAnhChinh);
//
//        return sanPhamList;
//    }
}