package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.HoaDon; // Import model HoaDon
import sd_04.datn_fstore.model.HoaDonChiTiet;
import sd_04.datn_fstore.repository.HoaDonChiTietRepository;
import sd_04.datn_fstore.service.HoaDonChiTietService;
import sd_04.datn_fstore.service.HoaDonExportService;
import sd_04.datn_fstore.service.HoaDonService;      // TIÊM VÀO ĐÂY

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HoaDonChiTietServiceImpl implements HoaDonChiTietService {

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired
    private HoaDonService hoaDonService; // Để tìm Hóa đơn cha

    @Autowired
    private HoaDonExportService hoaDonExportService; // Dịch vụ xuất PDF đã tạo

    // ... (Các phương thức getAll, getById, save, deleteById, findByHoaDonId, countBySanPhamChiTietId giữ nguyên) ...

    @Override
    public List<HoaDonChiTiet> getAll() {
        return hoaDonChiTietRepository.findAll();
    }

    @Override
    public Optional<HoaDonChiTiet> getById(Integer id) {
        return hoaDonChiTietRepository.findById(id);
    }

    @Override
    public HoaDonChiTiet save(HoaDonChiTiet hoaDonChiTiet) {
        return hoaDonChiTietRepository.save(hoaDonChiTiet);
    }

    @Override
    public void deleteById(Integer id) {
        hoaDonChiTietRepository.deleteById(id);
    }

    @Override
    public List<HoaDonChiTiet> findByHoaDonId(Integer hoaDonId) {
        return hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
    }

    @Override
    public long countBySanPhamChiTietId(Integer sanPhamChiTietId) {
        return hoaDonChiTietRepository.countBySanPhamChiTietId(sanPhamChiTietId);
    }

    // --- BỔ SUNG PHƯƠNG THỨC XUẤT PDF ---
    /**
     * Xuất PDF bằng cách sử dụng ID chi tiết để tìm ID hóa đơn cha.
     */
    public byte[] exportHoaDonByChiTietId(Integer hoaDonChiTietId) {
        // 1. Tìm HDCT để lấy Hóa đơn cha
        HoaDonChiTiet hdct = hoaDonChiTietRepository.findById(hoaDonChiTietId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Chi tiết HĐ ID: " + hoaDonChiTietId));

        HoaDon hoaDon = hdct.getHoaDon();
        if (hoaDon == null) {
            throw new RuntimeException("Chi tiết HĐ ID " + hoaDonChiTietId + " không liên kết với Hóa đơn nào.");
        }

        // 2. Gọi dịch vụ xuất PDF chính (sử dụng ID của Hóa đơn cha)
        return hoaDonExportService.exportHoaDon(hoaDon.getId());
    }
}