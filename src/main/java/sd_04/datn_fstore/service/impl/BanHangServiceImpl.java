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

        PhieuGiamGia pgg = getPhieuGiamGiaFromRequest(request);

        // 2. Tạo hóa đơn (Lưu vào DB)
        HoaDon hoaDon = createHoaDonFromPayload(request, pgg);
        hoaDon.setTrangThai(4); // 4: Hoàn thành
        hoaDon.setHinhThucBanHang(1); // 1: Tại quầy
        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

        // 3. Trừ kho & Trừ Voucher (nếu có)
        decrementInventory(itemsList);
        if (pgg != null) {
            decrementVoucher(pgg);
        }

        // 4. [REAL-TIME] TÍNH TỔNG TIỀN TỪ DATABASE (Tránh hack giá từ FE)
        BigDecimal totalReal = BigDecimal.ZERO;
        for (CreateOrderRequest.SanPhamItem item : itemsList) {
            BigDecimal itemTotal = saveHoaDonChiTiet(savedHoaDon, item);
            totalReal = totalReal.add(itemTotal);
        }

        // 5. TÍNH TOÁN TIỀN GIẢM GIÁ
        BigDecimal tienGiam = calculateDiscount(totalReal, pgg);

        // 6. Cập nhật số liệu cuối cùng
        savedHoaDon.setTongTien(totalReal);
        savedHoaDon.setTienGiamGia(tienGiam);
        savedHoaDon.setTongTienSauGiam(totalReal.subtract(tienGiam));

        // Lưu lần cuối
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

        // TÍNH TOÁN TIỀN GIẢM GIÁ
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

    /**
     * ✅ ĐÃ THÊM MỚI: Tìm hóa đơn theo Mã Hóa Đơn (String)
     */
    @Override
    @Transactional(readOnly = true)
    public HoaDon getDraftOrderByCode(String maHoaDon) {
        return hoaDonRepository.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn có mã: " + maHoaDon));
    }

    // Lưu ý: Hàm getDraftOrderDetail(Integer id) có thể xóa nếu không dùng nữa,
    // hoặc giữ lại để tương thích ngược. Ở đây tôi giữ lại.
//    @Override
//    @Transactional(readOnly = true)
//    public HoaDon getDraftOrderDetail(Integer id) {
//        return hoaDonRepository.findByIdWithDetails(id)
//                .orElseThrow(() -> new RuntimeException("Không tìm thấy Hóa Đơn ID: " + id));
//    }

    // --- LOGIC TÍNH TIỀN VOUCHER (% VÀ TIỀN MẶT) ---
// File: sd_04.datn_fstore.service.impl.BanHangServiceImpl.java

    // --- LOGIC TÍNH TIỀN VOUCHER (% VÀ TIỀN MẶT) ---
    private BigDecimal calculateDiscount(BigDecimal tongTienHang, PhieuGiamGia pgg) {
        if (pgg == null) return BigDecimal.ZERO;

        // Kiểm tra đơn tối thiểu
        if (pgg.getDieuKienGiamGia() != null && tongTienHang.compareTo(pgg.getDieuKienGiamGia()) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discountAmount;
        Integer loaiGiam = pgg.getHinhThucGiam(); // Quy ước: 1 = Tiền mặt, 2 = Phần trăm

        // ✅ SỬA LOGIC: Kiểm tra nếu là GIẢM PHẦN TRĂM (loaiGiam == 2)
        if (loaiGiam != null && loaiGiam == 2) {
            // --- GIẢM PHẦN TRĂM ---
            BigDecimal phanTram = pgg.getGiaTriGiam().divide(new BigDecimal(100));
            discountAmount = tongTienHang.multiply(phanTram);

            // Kiểm tra Giảm Tối Đa
            if (pgg.getSoTienGiam() != null && discountAmount.compareTo(pgg.getSoTienGiam()) > 0) {
                discountAmount = pgg.getSoTienGiam();
            }
        } else if (loaiGiam != null && loaiGiam == 1) { // ✅ THÊM: Nếu là GIẢM TIỀN MẶT (loaiGiam == 1)
            // --- GIẢM TIỀN MẶT ---
            discountAmount = pgg.getGiaTriGiam();
        } else {
            // Trường hợp không xác định (Mặc định không giảm)
            discountAmount = BigDecimal.ZERO;
        }

        // Không được giảm quá tổng tiền hàng
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
                    .orElseThrow(() -> new RuntimeException("SPCT không tồn tại"));
            if (spct.getSoLuong() < item.getSoLuong())
                throw new RuntimeException("Sản phẩm " + spct.getId() + " không đủ hàng.");
            spct.setSoLuong(spct.getSoLuong() - item.getSoLuong());
            sanPhamCTRepository.save(spct);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementVoucher(PhieuGiamGia pgg) {
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
            String url = "/admin/hoa-don/detail/" + hd.getId();
            thongBaoService.createNotification("Đơn hàng mới", "Thanh toán thành công " + hd.getMaHoaDon(), "ORDER", url);
        } catch (Exception e) {
            System.err.println("Lỗi gửi thông báo: " + e.getMessage());
        }
    }
}