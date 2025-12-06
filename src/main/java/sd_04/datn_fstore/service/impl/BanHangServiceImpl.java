package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.dto.CreateOrderRequest;
import sd_04.datn_fstore.model.*;
import sd_04.datn_fstore.repository.*;
import sd_04.datn_fstore.service.BanHangService;
import sd_04.datn_fstore.service.PhieuGiamgiaService;
import sd_04.datn_fstore.service.ThongBaoService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BanHangServiceImpl implements BanHangService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final SanPhamCTRepository sanPhamCTRepository;
    private final NhanVienRepository nhanVienRepository;
    private final KhachHangRepo khachHangRepository;
    private final PhieuGiamGiaRepo phieuGiamGiaRepository;

    private final PhieuGiamgiaService phieuGiamgiaService;
    private final ThongBaoService thongBaoService;
    private final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HoaDon thanhToanTienMat(CreateOrderRequest request) {
        // 1. Validation cơ bản
        List<CreateOrderRequest.SanPhamItem> itemsList = request.getDanhSachSanPham();
        if (itemsList == null || itemsList.isEmpty()) {
            throw new IllegalArgumentException("Đơn hàng phải có ít nhất 1 sản phẩm.");
        }

        // Lấy thông tin phiếu giảm giá mới nhất từ DB
        PhieuGiamGia pgg = getPhieuGiamGiaFromRequest(request);

        // 2. Tạo hóa đơn (Lưu vào DB với trạng thái Hoàn thành)
        HoaDon hoaDon = createHoaDonFromPayload(request, pgg);
        hoaDon.setTrangThai(4); // 4: Hoàn thành
        hoaDon.setHinhThucBanHang(1); // 1: Tại quầy
        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

        // 3. Trừ kho & Trừ Voucher (nếu có)
        decrementInventory(itemsList);
        if (pgg != null) {
            decrementVoucher(pgg);
        }

        // 4. [REAL-TIME] TÍNH TỔNG TIỀN TỪ DATABASE (Tránh hack giá từ Frontend)
        BigDecimal totalReal = BigDecimal.ZERO;
        for (CreateOrderRequest.SanPhamItem item : itemsList) {
            BigDecimal itemTotal = saveHoaDonChiTiet(savedHoaDon, item);
            totalReal = totalReal.add(itemTotal);
        }

        // 5. TÍNH TOÁN TIỀN GIẢM GIÁ (Logic đã sửa)
        BigDecimal tienGiam = calculateDiscount(totalReal, pgg);

        // 6. Cập nhật số liệu cuối cùng
        savedHoaDon.setTongTien(totalReal);
        savedHoaDon.setTienGiamGia(tienGiam);
        savedHoaDon.setTongTienSauGiam(totalReal.subtract(tienGiam));

        // Lưu lần cuối để cập nhật tiền
        HoaDon finalOrder = hoaDonRepository.save(savedHoaDon);

        // 7. Gửi thông báo
        sendNotification(finalOrder);

        return finalOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HoaDon luuHoaDonTam(CreateOrderRequest request) {
        PhieuGiamGia pgg = getPhieuGiamGiaFromRequest(request);
        HoaDon hoaDon = createHoaDonFromPayload(request, pgg);

        // Logic trạng thái: Nếu là chuyển khoản thường (TRANSFER) thì chờ xác nhận (5), còn lại là treo (0)
        String pttt = request.getPhuongThucThanhToan();
        hoaDon.setTrangThai("TRANSFER".equals(pttt) ? 5 : 0);

        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

        // Tính toán lại tiền
        BigDecimal totalReal = BigDecimal.ZERO;
        List<CreateOrderRequest.SanPhamItem> itemsList = request.getDanhSachSanPham();

        if (itemsList != null) {
            for (CreateOrderRequest.SanPhamItem item : itemsList) {
                BigDecimal itemTotal = saveHoaDonChiTiet(savedHoaDon, item);
                totalReal = totalReal.add(itemTotal);
            }
        }

        // Tính toán tiền giảm giá
        BigDecimal tienGiam = calculateDiscount(totalReal, pgg);

        savedHoaDon.setTongTien(totalReal);
        savedHoaDon.setTienGiamGia(tienGiam);
        savedHoaDon.setTongTienSauGiam(totalReal.subtract(tienGiam));

        return hoaDonRepository.save(savedHoaDon);
    }

    // --- CÁC HÀM GET DỮ LIỆU ---

    @Override
    @Transactional(readOnly = true)
    public List<HoaDon> getDraftOrders() {
        return hoaDonRepository.findByTrangThaiInOrderByNgayTaoDesc(List.of(0, 5));
    }

    @Override
    @Transactional(readOnly = true)
    public HoaDon getDraftOrderByCode(String maHoaDon) {
        return hoaDonRepository.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn có mã: " + maHoaDon));
    }

    // --- LOGIC TÍNH TIỀN VOUCHER (% VÀ TIỀN MẶT) ---
    // ✅ ĐÃ SỬA: Dùng RoundingMode để tránh lỗi chia số lẻ và làm tròn tiền Việt
    private BigDecimal calculateDiscount(BigDecimal tongTienHang, PhieuGiamGia pgg) {
        // 1. Check null & Giá trị cơ bản
        if (pgg == null || tongTienHang == null || tongTienHang.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 2. Check Điều kiện đơn hàng tối thiểu
        BigDecimal dieuKien = pgg.getDieuKienGiamGia() != null ? pgg.getDieuKienGiamGia() : BigDecimal.ZERO;
        if (tongTienHang.compareTo(dieuKien) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        Integer loaiGiam = pgg.getHinhThucGiam(); // 1: Tiền mặt, 2: %
        BigDecimal giaTriGiam = pgg.getGiaTriGiam() != null ? pgg.getGiaTriGiam() : BigDecimal.ZERO;

        if (loaiGiam != null && loaiGiam == 2) {
            // ================= GIẢM THEO PHẦN TRĂM (%) =================
            // Logic: (Tổng tiền * % Giảm) / 100
            // Sử dụng RoundingMode.HALF_UP để làm tròn thành tiền nguyên (VND)
            discountAmount = tongTienHang.multiply(giaTriGiam)
                    .divide(new BigDecimal(100), 0, RoundingMode.HALF_UP);

            // Check Giảm Tối Đa (Max Discount Amount)
            BigDecimal maxGiam = pgg.getSoTienGiam();
            if (maxGiam != null && maxGiam.compareTo(BigDecimal.ZERO) > 0) {
                if (discountAmount.compareTo(maxGiam) > 0) {
                    discountAmount = maxGiam;
                }
            }

        } else if (loaiGiam != null && loaiGiam == 1) {
            // ================= GIẢM TIỀN MẶT TRỰC TIẾP =================
            discountAmount = giaTriGiam;
        }

        // 3. Chốt chặn cuối cùng: Không bao giờ giảm quá tổng tiền đơn hàng
        if (discountAmount.compareTo(tongTienHang) > 0) {
            discountAmount = tongTienHang;
        }

        return discountAmount;
    }

    // --- CÁC HÀM HỖ TRỢ KHÁC ---

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementInventory(List<CreateOrderRequest.SanPhamItem> items) {
        for (CreateOrderRequest.SanPhamItem item : items) {
            SanPhamChiTiet spct = sanPhamCTRepository.findById(item.getSanPhamChiTietId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm ID " + item.getSanPhamChiTietId() + " không tồn tại"));

            if (spct.getSoLuong() < item.getSoLuong()) {
                throw new RuntimeException("Sản phẩm " + spct.getSanPham().getTenSanPham() + " không đủ hàng (Còn: " + spct.getSoLuong() + ")");
            }

            spct.setSoLuong(spct.getSoLuong() - item.getSoLuong());
            sanPhamCTRepository.save(spct);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementVoucher(PhieuGiamGia pgg) {
        // Gọi service voucher để xử lý logic trừ số lượng và cập nhật trạng thái
        if (pgg != null) phieuGiamgiaService.decrementVoucher(pgg);
    }

    private PhieuGiamGia getPhieuGiamGiaFromRequest(CreateOrderRequest request) {
        if (request.getPhieuGiamGiaId() != null) {
            return phieuGiamGiaRepository.findById(request.getPhieuGiamGiaId()).orElse(null);
        }
        return null;
    }

    private BigDecimal saveHoaDonChiTiet(HoaDon savedHoaDon, CreateOrderRequest.SanPhamItem item) {
        SanPhamChiTiet spct = sanPhamCTRepository.findById(item.getSanPhamChiTietId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy SPCT ID: " + item.getSanPhamChiTietId()));

        HoaDonChiTiet hdct = new HoaDonChiTiet();
        hdct.setHoaDon(savedHoaDon);
        hdct.setSanPhamChiTiet(spct);
        hdct.setSoLuong(item.getSoLuong());
        hdct.setDonGia(spct.getGiaTien());

        BigDecimal thanhTien = spct.getGiaTien().multiply(new BigDecimal(item.getSoLuong()));
        hdct.setThanhTien(thanhTien);

        hoaDonChiTietRepository.save(hdct);
        return thanhTien;
    }

    private HoaDon createHoaDonFromPayload(CreateOrderRequest request, PhieuGiamGia pgg) {
        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon(request.getMaHoaDon());
        hoaDon.setNgayTao(LocalDateTime.now(VN_ZONE));
        hoaDon.setPhieuGiamGia(pgg);

        if (request.getNhanVienId() != null)
            nhanVienRepository.findById(request.getNhanVienId()).ifPresent(hoaDon::setNhanVien);

        if (request.getKhachHangId() != null)
            khachHangRepository.findById(request.getKhachHangId()).ifPresent(hoaDon::setKhachHang);

        hoaDon.setHinhThucBanHang(1); // Tại quầy
        return hoaDon;
    }

    private void sendNotification(HoaDon hd) {
        try {
            // URL trỏ về trang chi tiết hóa đơn admin
            String url = "/admin/hoa-don/detail/" + hd.getId();
            thongBaoService.createNotification("Đơn hàng tại quầy", "Thanh toán thành công " + hd.getMaHoaDon(), "ORDER", url);
        } catch (Exception e) {
            System.err.println("Lỗi gửi thông báo (Không ảnh hưởng luồng chính): " + e.getMessage());
        }
    }
}