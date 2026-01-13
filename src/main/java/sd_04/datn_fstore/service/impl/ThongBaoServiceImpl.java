package sd_04.datn_fstore.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Thêm log để dễ debug
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
@Slf4j // Lombok log
public class ThongBaoServiceImpl implements ThongBaoService {

    private final ThongBaoRepository thongBaoRepository;
    private final SanPhamCTRepository sanPhamCTRepository;

    // Định nghĩa hằng số
    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final String NOTI_TYPE_STOCK = "STOCK";

    @Override
    public void createNotification(String tieuDe, String noiDung, String loai, String url) {
        ThongBao tb = ThongBao.builder()
                .tieuDe(tieuDe)
                .noiDung(noiDung)
                .loaiThongBao(loai)
                .urlLienKet(url)
                .trangThai(0)
                .ngayTao(LocalDateTime.now())
                .build();
        thongBaoRepository.save(tb);
    }

    @Override
    public List<ThongBao> getUnreadNotifications() {
        return thongBaoRepository.findByTrangThaiOrderByNgayTaoDesc(0);
    }

    @Override
    public void markAsRead(Integer id) {
        thongBaoRepository.findById(id).ifPresent(tb -> {
            tb.setTrangThai(1);
            thongBaoRepository.save(tb);
        });
    }

    // Chạy mỗi 30 phút (1.800.000 ms)
    @Scheduled(fixedRate = 1800000)
    @Transactional
    public void autoScanLowStock() {
        log.info("Bắt đầu quét sản phẩm sắp hết hàng...");

        // Cần đảm bảo hàm này trong Repo dùng JOIN FETCH để tránh lỗi N+1
        List<SanPhamChiTiet> lowStockItems = sanPhamCTRepository.findBySoLuongLessThanEqual(LOW_STOCK_THRESHOLD);

        if (lowStockItems.isEmpty()) {
            log.info("Không có sản phẩm nào sắp hết hàng.");
            return;
        }

        int countNewNoti = 0;
        for (SanPhamChiTiet sp : lowStockItems) {
            // Lấy thông tin an toàn (tránh null pointer nếu dữ liệu bẩn)
            String tenSP = sp.getSanPham() != null ? sp.getSanPham().getTenSanPham() : "Unknown";
            String mauSac = sp.getMauSac() != null ? sp.getMauSac().getTenMauSac() : "-";
            String size = sp.getKichThuoc() != null ? sp.getKichThuoc().getTenKichThuoc() : "-";

            String spName = String.format("%s - %s [%s]", tenSP, mauSac, size);
            String noiDung = String.format("Sản phẩm %s sắp hết (Còn: %d)", spName, sp.getSoLuong());

            // Check trùng lặp: Nếu đã có thông báo STOCK với nội dung y hệt mà chưa đọc -> Bỏ qua
            boolean exists = thongBaoRepository.existsByLoaiThongBaoAndNoiDungAndTrangThai(NOTI_TYPE_STOCK, noiDung, 0);

            if (!exists) {
                createNotification(
                        "Cảnh báo sắp hết hàng",
                        noiDung,
                        NOTI_TYPE_STOCK,
                        "/admin/san-pham-chi-tiet/detail/" + sp.getId() // Link trỏ thẳng vào chi tiết sản phẩm thì tốt hơn
                );
                countNewNoti++;
            }
        }
        log.info("Hoàn tất quét kho. Đã tạo {} thông báo mới.", countNewNoti);
    }
    @Override
    @Transactional // Quan trọng: Để đảm bảo update DB thành công
    public void markAllAsRead() {
        // 1. Lấy tất cả thông báo đang có trạng thái chưa đọc (0)
        List<ThongBao> unreadList = thongBaoRepository.findByTrangThaiOrderByNgayTaoDesc(0);

        // 2. Duyệt qua và set lại thành 1
        for (ThongBao tb : unreadList) {
            tb.setTrangThai(1);
        }

        // 3. Lưu lại tất cả
        thongBaoRepository.saveAll(unreadList);
    }
}