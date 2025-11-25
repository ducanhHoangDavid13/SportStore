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
    @Transactional // Đảm bảo tính toàn vẹn dữ liệu khi thêm/sửa
    public SanPhamChiTiet save(SanPhamChiTiet sanPhamChiTiet) {
        // Ví dụ: Set mặc định trạng thái là 1 (Hoạt động/Đang bán)
        sanPhamChiTiet.setTrangThai(1);

        sanPhamChiTietRepository.save(sanPhamChiTiet);
        return sanPhamChiTietRepository.save(sanPhamChiTiet);
    }

    /**
     * XÓA MỀM (Soft Delete)
     * Chuyển trạng thái về 0 thay vì xóa khỏi DB để giữ lịch sử hóa đơn.
     */
    @Override
    @Transactional // QUAN TRỌNG: Cần có để commit thay đổi xuống DB
    public void delete(Integer id) {
        Optional<SanPhamChiTiet> optional = sanPhamChiTietRepository.findById(id);
        if (optional.isPresent()) {
            SanPhamChiTiet spct = optional.get();
            spct.setTrangThai(0); // 0: Ngừng hoạt động
            sanPhamChiTietRepository.save(spct);
        }
        // Nếu muốn xóa cứng (hard delete) thì dùng:
        // sanPhamChiTietRepository.deleteById(id);
    }

    /**
     * TÌM KIẾM NÂNG CAO (Admin)
     * Khớp với câu Query 12 tham số trong Repository
     */
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

    /**
     * API CHO TRANG BÁN HÀNG (POS)
     * Gọi hàm tối ưu trong Repository để lấy full thông tin (Ảnh, Màu, Size...)
     * Trạng thái = 1 (Đang bán) và Số lượng > 0
     */
    @Override
    public List<SanPhamChiTiet> getAvailableProducts() {
        // Gọi đúng tên hàm mới trong Repository
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
}