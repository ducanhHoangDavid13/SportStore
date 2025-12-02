package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.model.HinhAnh;
import sd_04.datn_fstore.model.SanPham; // Cần thiết để tham chiếu đến SanPham
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
    private final HinhAnhService hinhAnhService; // Inject HinhAnhService
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
        // 1. Lưu biến thể trước
        if (sanPhamChiTiet.getId() == null) {
            sanPhamChiTiet.setTrangThai(1);
        }
        SanPhamChiTiet savedSpct = sanPhamChiTietRepository.save(sanPhamChiTiet);

        // 2. CẬP NHẬT LẠI SỐ LƯỢNG SẢN PHẨM CHA
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

            // Cập nhật lại số lượng cha sau khi xóa
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

            // Cập nhật lại số lượng cha (nếu logic của bạn là ngừng bán thì không tính số lượng)
            // Nếu ngừng bán vẫn tính tồn kho thì bỏ dòng dưới đi
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


    private void updateTotalQuantitySanPham(Integer sanPhamId) {
        // Lấy tất cả biến thể của sản phẩm này (chỉ lấy những cái đang hoạt động nếu muốn)
        List<SanPhamChiTiet> listBienThe = sanPhamChiTietRepository.findBySanPhamTenSanPham(
                sanPhamRepository.findById(sanPhamId).get().getTenSanPham()
        );
        // Lưu ý: Cách lấy list trên hơi rủi ro nếu trùng tên.
        // Tốt nhất nên viết thêm hàm findBySanPhamId trong Repo.
        // Ở đây tôi dùng tạm logic stream lọc tay hoặc giả định bạn đã có hàm findBySanPhamId.

        // Cách an toàn nhất không cần sửa Repo:
        SanPham sanPham = sanPhamRepository.findById(sanPhamId).orElse(null);
        if (sanPham != null) {
            // Lấy danh sách biến thể thông qua quan hệ JPA (nếu đã config @OneToMany)
            // Hoặc query thủ công. Giả sử dùng JPA Relation:
            int total = 0;
            if (sanPham.getSanPhamChiTiets() != null) {
                for (SanPhamChiTiet ct : sanPham.getSanPhamChiTiets()) {
                    // Chỉ cộng dồn nếu trạng thái đang hoạt động (Tùy nghiệp vụ của bạn)
                    if (ct.getTrangThai() == 1 && ct.getSoLuong() != null) {
                        total += ct.getSoLuong();
                    }
                }
            }

            // Update và Save cha
            sanPham.setSoLuong(total);
            sanPhamRepository.save(sanPham);
        }
    }

    @Override
    public List<SanPhamChiTiet> getBySanPhamId(Integer id) {
        return List.of();
    }

    /**
     * Phương thức dùng để gán tên hình ảnh chính vào đối tượng SanPham (cha)
     * nằm bên trong SanPhamChiTiet, giúp API trả về có đủ thông tin.
     */
    private void loadTenHinhAnhChinh(SanPhamChiTiet spct) {
        // Kiểm tra mối quan hệ SanPham có tồn tại không
        if (spct.getSanPham() == null) {
            return;
        }

        // Lấy đối tượng SanPham (cha)
        SanPham sanPhamCha = spct.getSanPham();
        Integer sanPhamId = sanPhamCha.getId();

        // 1. Ưu tiên tìm hình ảnh Avatar
        Optional<HinhAnh> avatarOpt = hinhAnhService.getAvatar(sanPhamId);

        if (avatarOpt.isPresent()) {
            // Gán vào đối tượng SanPham (sanPhamCha)
            sanPhamCha.setTenHinhAnhChinh(avatarOpt.get().getTenHinhAnh());
        } else {
            // 2. Nếu không có Avatar, lấy hình ảnh đầu tiên trong danh sách
            List<HinhAnh> allImages = hinhAnhService.getBySanPhamId(sanPhamId);
            if (!allImages.isEmpty()) {
                // Gán vào đối tượng SanPham (sanPhamCha)
                sanPhamCha.setTenHinhAnhChinh(allImages.get(0).getTenHinhAnh());
            }
        }
    }

    @Override
    public SanPhamChiTiet getByIdAndAvailable(Integer id) {
        // Gọi hàm tìm kiếm theo ID và Trạng thái = 1 trong Repo vừa sửa
        Optional<SanPhamChiTiet> optional = sanPhamChiTietRepository.findByIdAndAvailable(id);

        if (optional.isPresent()) {
            SanPhamChiTiet spct = optional.get();
            // QUAN TRỌNG: Load ảnh đại diện để hiển thị trên POS
            loadTenHinhAnhChinh(spct);
            return spct;
        }
        return null; // Hoặc ném ngoại lệ tùy logic của bạn
    }
}