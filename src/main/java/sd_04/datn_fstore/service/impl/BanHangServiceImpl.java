package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy; // <-- 1. THÊM IMPORT
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.dto.CreateOrderRequest;
import sd_04.datn_fstore.dto.VnPayResponseDTO;
import sd_04.datn_fstore.model.*;
import sd_04.datn_fstore.repository.*;
import sd_04.datn_fstore.service.BanHangService;
import sd_04.datn_fstore.service.KhoService;
import sd_04.datn_fstore.service.PhieuGiamgiaService;
import sd_04.datn_fstore.service.VnPayService;

import java.io.UnsupportedEncodingException;
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

    // --- 2. TIÊM CÁC SERVICE LIÊN QUAN ---

    @Lazy // <-- THÊM @Lazy ĐỂ PHÁ VỠ VÒNG LẶP
    private final VnPayService vnPayService;

    private final KhoService khoService;
    private final PhieuGiamgiaService phieuGiamgiaService;
    private final PhieuGiamGiaRepo phieuGiamGiaRepository; // Để lấy PGG

    private final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HoaDon thanhToanTienMat(CreateOrderRequest request) {
        PhieuGiamGia pgg = getPhieuGiamGiaFromRequest(request);

        HoaDon hoaDon = createHoaDonFromPayload(request, pgg);
        hoaDon.setTrangThai(1); // 1 = Đã hoàn thành

        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
        List<CreateOrderRequest.Item> itemsList = request.getItemsList();

        // 3. SỬA LOGIC: Gọi service trừ kho
        decrementInventory(itemsList);

        for (CreateOrderRequest.Item item : itemsList) {
            saveHoaDonChiTiet(savedHoaDon, item); // Lưu chi tiết
        }

        // 4. SỬA LOGIC: Gọi service trừ voucher
        decrementVoucher(pgg);

        return savedHoaDon;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HoaDon luuHoaDonTam(CreateOrderRequest request) {
        PhieuGiamGia pgg = getPhieuGiamGiaFromRequest(request);
        HoaDon hoaDon = createHoaDonFromPayload(request, pgg);

        String pttt = request.getPaymentMethod();
        if ("VNPAY".equals(pttt)) {
            hoaDon.setTrangThai(5); // 5 = Chờ VNPAY
        } else if ("TRANSFER".equals(pttt) || "QR".equals(pttt)) {
            hoaDon.setTrangThai(5); // 5 = Chờ Chuyển Khoản
        } else {
            hoaDon.setTrangThai(0); // 0 = Hóa đơn tạm
        }

        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
        List<CreateOrderRequest.Item> itemsList = request.getItemsList();

        for (CreateOrderRequest.Item item : itemsList) {
            saveHoaDonChiTiet(savedHoaDon, item);
        }

        // Không trừ kho, không trừ voucher khi lưu tạm

        return savedHoaDon;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HoaDon> getDraftOrders() {
        List<Integer> trangThais = List.of(0, 5);
        return hoaDonRepository.findByTrangThaiInOrderByNgayTaoDesc(trangThais);
    }

    @Override
    @Transactional(readOnly = true) // <-- Thêm dòng này
    public HoaDon getDraftOrderDetail(Integer id) {
        HoaDon hoaDon = hoaDonRepository.findByIdWithDetails(id) // Gọi hàm đã JOIN FETCH
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Hóa Đơn Tạm ID: " + id));

        // Dòng này rất quan trọng để "kích hoạt" Lazy loading
        // nếu JOIN FETCH thất bại
        hoaDon.getHoaDonChiTiets().size();

        return hoaDon;
    }

    // --- 5. TRIỂN KHAI CÁC HÀM BỊ RỖNG ---

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VnPayResponseDTO taoThanhToanVnPay(CreateOrderRequest request, String ipAddress) {

        request.setPaymentMethod("VNPAY"); // Đảm bảo PTTT là VNPAY
        HoaDon savedHoaDon = this.luuHoaDonTam(request);

        long amountInVNDcents = savedHoaDon.getTongTienSauGiam().longValue() * 100;
        String orderCode = savedHoaDon.getMaHoaDon();
        String orderInfo = "Thanh toan don hang " + orderCode;

        try {
            // Gọi VnPayService (đã tiêm @Lazy)
            String paymentUrl = vnPayService.createOrder(amountInVNDcents, orderInfo, orderCode, ipAddress);
            return new VnPayResponseDTO(true, "Tạo link VNPAY thành công", paymentUrl);

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Lỗi khi tạo link VNPAY: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementInventory(List<CreateOrderRequest.Item> items) {
        // (Giả sử KhoService đã được tiêm và có hàm truTonKho)
        for (CreateOrderRequest.Item item : items) {
            khoService.truTonKho(item.getSanPhamChiTietId(), item.getSoLuong());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementVoucher(PhieuGiamGia pgg) {
        // (Giả sử PhieuGiamgiaService đã được tiêm và có hàm decrementVoucher)
        phieuGiamgiaService.decrementVoucher(pgg);
    }

    // --- CÁC HÀM TIỆN ÍCH (HELPER METHODS) ---

    private PhieuGiamGia getPhieuGiamGiaFromRequest(CreateOrderRequest request) {
        if (request.getPhieuGiamGiaId() != null) {
            return phieuGiamGiaRepository.findById(request.getPhieuGiamGiaId())
                    .orElse(null);
        }
        return null;
    }

    private void saveHoaDonChiTiet(HoaDon savedHoaDon, CreateOrderRequest.Item item) {
        SanPhamChiTiet spct = sanPhamCTRepository.findById(item.getSanPhamChiTietId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy SPCT ID: " + item.getSanPhamChiTietId()));

        HoaDonChiTiet hdct = new HoaDonChiTiet();
        hdct.setHoaDon(savedHoaDon);
        hdct.setSanPhamChiTiet(spct);
        hdct.setSoLuong(item.getSoLuong());
        hdct.setDonGia(item.getDonGia());
        hdct.setThanhTien(item.getDonGia().multiply(new BigDecimal(item.getSoLuong())));
        hoaDonChiTietRepository.save(hdct);
    }

    // 6. SỬA LỖI: Tính toán Subtotal
    private HoaDon createHoaDonFromPayload(CreateOrderRequest request, PhieuGiamGia pgg) {
        HoaDon hoaDon = new HoaDon();

        // Lấy giá trị từ DTO
        BigDecimal totalAmount = request.getTotalAmount();
        BigDecimal discountAmount = request.getDiscountAmount();

        // Tự tính Subtotal (Tổng tiền gốc)
        BigDecimal subtotalAmount = totalAmount.add(discountAmount);

        hoaDon.setMaHoaDon(request.getOrderCode());
        hoaDon.setNgayTao(LocalDateTime.now(VN_ZONE));
        hoaDon.setTongTien(subtotalAmount); // Tiền gốc
        hoaDon.setTienGiamGia(discountAmount);
        hoaDon.setTongTienSauGiam(totalAmount);

        hoaDon.setPhieuGiamGia(pgg);

        int nhanVienId = request.getNhanVienId();
        NhanVien nv = nhanVienRepository.findById(nhanVienId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Nhân Viên ID: " + nhanVienId));
        hoaDon.setNhanVien(nv);

        Integer khachHangId = request.getKhachHangId();
        if (khachHangId != null) {
            KhachHang kh = khachHangRepository.findById(khachHangId).orElse(null);
            hoaDon.setKhachHang(kh);
        }

        hoaDon.setHinhThucBanHang(1); // 1 = Tại quầy
        return hoaDon;
    }
}