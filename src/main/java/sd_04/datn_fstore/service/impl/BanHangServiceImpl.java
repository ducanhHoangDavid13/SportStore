package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.dto.CreateOrderRequest;
import sd_04.datn_fstore.dto.VnPayResponseDTO;
import sd_04.datn_fstore.model.*;
import sd_04.datn_fstore.repository.*;
import sd_04.datn_fstore.service.BanHangService;
import sd_04.datn_fstore.service.PhieuGiamgiaService;
import sd_04.datn_fstore.service.ThongBaoService;
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
    private final PhieuGiamGiaRepo phieuGiamGiaRepository;

    @Lazy
    private final VnPayService vnPayService;
    private final PhieuGiamgiaService phieuGiamgiaService;
    private final ThongBaoService thongBaoService;
    private final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HoaDon thanhToanTienMat(CreateOrderRequest request) {
        // 1. Validation
        List<CreateOrderRequest.SanPhamItem> itemsList = request.getDanhSachSanPham();
        if (itemsList == null || itemsList.isEmpty()) {
            throw new IllegalArgumentException("Không thể thanh toán đơn hàng không có sản phẩm.");
        }

        PhieuGiamGia pgg = getPhieuGiamGiaFromRequest(request);

        // 2. Tạo hóa đơn tạm (Giá trị tiền lúc này chưa quan trọng, sẽ tính lại ở bước 4)
        HoaDon hoaDon = createHoaDonFromPayload(request, pgg);
        hoaDon.setTrangThai(4); // Hoàn thành
        hoaDon.setHinhThucBanHang(1); // Tại quầy

        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

        // 3. Trừ kho
        decrementInventory(itemsList);
        if (pgg != null) {
            decrementVoucher(pgg);
        }

        // 4. [REAL-TIME] TÍNH LẠI TỔNG TIỀN TỪ DATABASE
        BigDecimal totalReal = BigDecimal.ZERO;

        for (CreateOrderRequest.SanPhamItem item : itemsList) {
            // Hàm này lấy giá trực tiếp từ DB để lưu, bỏ qua giá từ Frontend
            BigDecimal itemTotal = saveHoaDonChiTiet(savedHoaDon, item);
            totalReal = totalReal.add(itemTotal);
        }

        // 5. Cập nhật lại Tổng tiền chuẩn vào Hóa Đơn
        savedHoaDon.setTongTien(totalReal);

        // Tính lại tiền giảm giá (Đảm bảo không âm tiền)
        BigDecimal tienGiam = request.getTienGiamGia() != null ? request.getTienGiamGia() : BigDecimal.ZERO;
        if (tienGiam.compareTo(totalReal) > 0) {
            tienGiam = totalReal;
        }

        savedHoaDon.setTienGiamGia(tienGiam);
        savedHoaDon.setTongTienSauGiam(totalReal.subtract(tienGiam));

        // Lưu lần cuối để chốt giá đúng
        HoaDon finalOrder = hoaDonRepository.save(savedHoaDon);

        // 6. Thông báo
        sendNotification(finalOrder);

        return finalOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HoaDon luuHoaDonTam(CreateOrderRequest request) {
        PhieuGiamGia pgg = getPhieuGiamGiaFromRequest(request);
        HoaDon hoaDon = createHoaDonFromPayload(request, pgg);

        String pttt = request.getPhuongThucThanhToan();
        if ("VNPAY".equals(pttt) || "TRANSFER".equals(pttt) || "QR".equals(pttt)) {
            hoaDon.setTrangThai(5);
        } else {
            hoaDon.setTrangThai(0); // Treo đơn
        }

        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

        // [REAL-TIME] Tính lại tiền khi lưu tạm
        BigDecimal totalReal = BigDecimal.ZERO;
        List<CreateOrderRequest.SanPhamItem> itemsList = request.getDanhSachSanPham();

        if (itemsList != null) {
            for (CreateOrderRequest.SanPhamItem item : itemsList) {
                BigDecimal itemTotal = saveHoaDonChiTiet(savedHoaDon, item);
                totalReal = totalReal.add(itemTotal);
            }
        }

        // Cập nhật lại giá đúng
        savedHoaDon.setTongTien(totalReal);
        BigDecimal tienGiam = request.getTienGiamGia() != null ? request.getTienGiamGia() : BigDecimal.ZERO;
        if (tienGiam.compareTo(totalReal) > 0) tienGiam = totalReal;

        savedHoaDon.setTienGiamGia(tienGiam);
        savedHoaDon.setTongTienSauGiam(totalReal.subtract(tienGiam));

        return hoaDonRepository.save(savedHoaDon);
    }

    // --- CÁC HÀM KHÁC GIỮ NGUYÊN LOGIC ---

    @Override
    @Transactional(readOnly = true)
    public List<HoaDon> getDraftOrders() {
        return hoaDonRepository.findByTrangThaiInOrderByNgayTaoDesc(List.of(0, 5));
    }

    @Override
    @Transactional(readOnly = true)
    public HoaDon getDraftOrderDetail(Integer id) {
        HoaDon hoaDon = hoaDonRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Hóa Đơn ID: " + id));
        hoaDon.getHoaDonChiTiets().size();
        return hoaDon;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VnPayResponseDTO taoThanhToanVnPay(CreateOrderRequest request, String ipAddress) {
        request.setPhuongThucThanhToan("VNPAY");
        HoaDon savedHoaDon = this.luuHoaDonTam(request);

        long amountInVNDcents = savedHoaDon.getTongTienSauGiam().multiply(new BigDecimal("100")).longValue();
        try {
            String paymentUrl = vnPayService.createOrder(amountInVNDcents, "Thanh toan " + savedHoaDon.getMaHoaDon(), savedHoaDon.getMaHoaDon(), ipAddress);
            return new VnPayResponseDTO(true, "Success", paymentUrl);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Lỗi VNPAY: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementInventory(List<CreateOrderRequest.SanPhamItem> items) {
        for (CreateOrderRequest.SanPhamItem item : items) {
            SanPhamChiTiet spct = sanPhamCTRepository.findById(item.getSanPhamChiTietId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
            int soLuongMua = item.getSoLuong();
            int soLuongTon = spct.getSoLuong();
            if (soLuongTon < soLuongMua)
                throw new RuntimeException("Sản phẩm " + spct.getMaSanPhamChiTiet() + " không đủ hàng.");

            spct.setSoLuong(soLuongTon - soLuongMua);
            sanPhamCTRepository.save(spct);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementVoucher(PhieuGiamGia pgg) {
        if (pgg != null) phieuGiamgiaService.decrementVoucher(pgg);
    }

    // --- HELPER METHODS ---

    private PhieuGiamGia getPhieuGiamGiaFromRequest(CreateOrderRequest request) {
        if (request.getPhieuGiamGiaId() != null) {
            return phieuGiamGiaRepository.findById(request.getPhieuGiamGiaId()).orElse(null);
        }
        return null;
    }

    // [QUAN TRỌNG] HÀM NÀY ĐẢM BẢO LẤY GIÁ TỪ DB
    private BigDecimal saveHoaDonChiTiet(HoaDon savedHoaDon, CreateOrderRequest.SanPhamItem item) {
        SanPhamChiTiet spct = sanPhamCTRepository.findById(item.getSanPhamChiTietId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy SPCT ID: " + item.getSanPhamChiTietId()));

        HoaDonChiTiet hdct = new HoaDonChiTiet();
        hdct.setHoaDon(savedHoaDon);
        hdct.setSanPhamChiTiet(spct);
        hdct.setSoLuong(item.getSoLuong());

        // CHỐT: Lấy giá từ DB, bỏ qua giá từ Request
        BigDecimal priceDB = spct.getGiaTien();
        hdct.setDonGia(priceDB);

        BigDecimal thanhTien = priceDB.multiply(new BigDecimal(item.getSoLuong()));
        hdct.setThanhTien(thanhTien);

        hoaDonChiTietRepository.save(hdct);

        return thanhTien; // Trả về để cộng tổng
    }

    private HoaDon createHoaDonFromPayload(CreateOrderRequest request, PhieuGiamGia pgg) {
        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon(request.getMaHoaDon());
        hoaDon.setNgayTao(LocalDateTime.now(VN_ZONE));

        // Giá trị tạm thời (sẽ bị ghi đè bởi tính toán thực tế)
        hoaDon.setTongTien(BigDecimal.ZERO);
        hoaDon.setTienGiamGia(BigDecimal.ZERO);
        hoaDon.setTongTienSauGiam(BigDecimal.ZERO);

        hoaDon.setPhieuGiamGia(pgg);

        if (request.getNhanVienId() != null) {
            nhanVienRepository.findById(request.getNhanVienId()).ifPresent(hoaDon::setNhanVien);
        }
        if (request.getKhachHangId() != null) {
            khachHangRepository.findById(request.getKhachHangId()).ifPresent(hoaDon::setKhachHang);
        }
        hoaDon.setHinhThucBanHang(1);
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