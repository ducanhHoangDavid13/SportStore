package sd_04.datn_fstore.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.SanPhamChiTiet;
import sd_04.datn_fstore.model.ThongBao;
import sd_04.datn_fstore.repository.SanPhamCTRepository;
import sd_04.datn_fstore.repository.ThongBaoRepository;
import sd_04.datn_fstore.service.ThongBaoService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ThongBaoServiceImpl implements ThongBaoService {

    private final ThongBaoRepository thongBaoRepository;
    private final SanPhamCTRepository sanPhamCTRepository;

    @Override
    public void createNotification(String tieuDe, String noiDung, String loai, String url) {
        ThongBao tb = ThongBao.builder()
                .tieuDe(tieuDe)
                .noiDung(noiDung)
                .loaiThongBao(loai)
                .urlLienKet(url)
                .trangThai(0) // 0 = Chưa đọc
                .ngayTao(LocalDateTime.now())
                .build();
        thongBaoRepository.save(tb);
    }

    @Override
    public List<ThongBao> getUnreadNotifications() {
        // Gọi Repository lấy danh sách chưa đọc (Status = 0), mới nhất lên đầu
        return thongBaoRepository.findByTrangThaiOrderByNgayTaoDesc(0);
    }

    @Override
    public void markAsRead(Integer id) {
        thongBaoRepository.findById(id).ifPresent(tb -> {
            tb.setTrangThai(1); // 1 = Đã đọc
            thongBaoRepository.save(tb);
        });
    }


    @Scheduled(fixedRate = 1800000)
    @Transactional
    public void autoScanLowStock() {
        // 1. Lấy tất cả sản phẩm có số lượng <= 10
        List<SanPhamChiTiet> lowStockItems = sanPhamCTRepository.findBySoLuongLessThanEqual(10); // Đảm bảo Repo có hàm này (đã làm ở bước trước)

        for (SanPhamChiTiet sp : lowStockItems) {
            String spName = sp.getSanPham().getTenSanPham() + " - " + sp.getMauSac().getTenMauSac() + " [" + sp.getKichThuoc().getTenKichThuoc() + "]";
            String noiDung = "Sản phẩm " + spName + " sắp hết (Còn: " + sp.getSoLuong() + ")";

            // 2. Kiểm tra xem đã có thông báo chưa đọc về cái này chưa? (Tránh Spam)
            boolean exists = thongBaoRepository.existsByLoaiThongBaoAndNoiDungAndTrangThai("STOCK", noiDung, 0);

            if (!exists) {
                // Nếu chưa báo thì tạo mới
                createNotification(
                        "Cảnh báo tự động",
                        noiDung,
                        "STOCK",
                        "/admin/san-pham-chi-tiet" // Link đến trang quản lý
                );
                System.out.println("Đã tạo thông báo tự động cho: " + spName);
            }
        }
    }
}