package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.dto.*;
import sd_04.datn_fstore.model.*;
import sd_04.datn_fstore.repository.*;
import sd_04.datn_fstore.service.BanHangService;
import sd_04.datn_fstore.service.CheckoutService;
import sd_04.datn_fstore.service.VnPayService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final BanHangService banHangService;
    private final VnPayService vnPayService;
    private final PhieuGiamGiaRepo phieuGiamGiaRepository;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final SanPhamCTRepository sanPhamCTRepository;
    private final DiaChiRepo diaChiRepo;
    private final NhanVienRepository nhanVienRepository;
    private final KhachHangRepo khachHangRepository;

    // =========================================================================
    // 1. TÍNH TOÁN TỔNG TIỀN
    // =========================================================================
    @Override
    public CalculateTotalResponse calculateOrderTotal(CalculateTotalRequest request) {
        BigDecimal subTotal = BigDecimal.ZERO;
        if (request.getItems() != null) {
            for (CalculateTotalRequest.CartItem item : request.getItems()) {
                BigDecimal itemTotal = item.getDonGia().multiply(BigDecimal.valueOf(item.getSoLuong()));
                subTotal = subTotal.add(itemTotal);
            }
        }

        BigDecimal shippingFee = request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        boolean voucherValid = false;
        String voucherMessage = "";

        if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
            Optional<PhieuGiamGia> pggOpt = phieuGiamGiaRepository.findByMaPhieuGiamGia(request.getVoucherCode());
            if (pggOpt.isPresent()) {
                PhieuGiamGia pgg = pggOpt.get();
                if (pgg.getTrangThai() == 0 && (pgg.getSoLuong() == null || pgg.getSoLuong() > 0)) {
                    if (pgg.getHinhThucGiam() == 2) { // %
                        discountAmount = subTotal.multiply(pgg.getGiaTriGiam()).divide(new BigDecimal(100), 0, RoundingMode.HALF_UP);
                        if (pgg.getSoTienGiam() != null && discountAmount.compareTo(pgg.getSoTienGiam()) > 0) {
                            discountAmount = pgg.getSoTienGiam();
                        }
                    } else { // Tiền
                        discountAmount = pgg.getGiaTriGiam();
                    }
                    if (discountAmount.compareTo(subTotal) > 0) discountAmount = subTotal;
                    voucherValid = true;
                    voucherMessage = "Áp dụng mã thành công!";
                } else {
                    voucherMessage = "Mã không hợp lệ.";
                }
            } else {
                voucherMessage = "Không tìm thấy mã.";
            }
        }

        BigDecimal finalTotal = subTotal.add(shippingFee).subtract(discountAmount);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) finalTotal = BigDecimal.ZERO;

        return new CalculateTotalResponse(subTotal, shippingFee, discountAmount, finalTotal, voucherMessage, voucherValid);
    }

    // =========================================================================
    // 2. XỬ LÝ ĐẶT HÀNG (CÓ ĐỊA CHỈ)
    // =========================================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckoutResponse placeOrder(CheckoutRequest req, String clientIp) {
        // --- A. TẠO HÓA ĐƠN ---
        HoaDon hoaDon = new HoaDon();
        String maHoaDon = "HD" + System.currentTimeMillis();
        hoaDon.setMaHoaDon(maHoaDon);
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setHinhThucBanHang(0); // Online
        hoaDon.setMoTa(req.getNote());

        // --- B. LƯU ĐỊA CHỈ ---
        DiaChi shippingInfo = new DiaChi();
        shippingInfo.setHoTen(req.getFullName());
        shippingInfo.setSoDienThoai(req.getPhone());
        shippingInfo.setDiaChiCuThe(req.getAddressDetail());
        shippingInfo.setXa(req.getWard());
        shippingInfo.setThanhPho(req.getDistrict() + " - " + req.getCity());
        shippingInfo.setGhiChu("Email: " + req.getEmail());
        shippingInfo.setLoaiDiaChi("Giao hàng");
        shippingInfo.setTrangThai(1);

        DiaChi savedDiaChi = diaChiRepo.save(shippingInfo);
        hoaDon.setDiaChiGiaoHang(savedDiaChi);

        // --- C. XỬ LÝ SẢN PHẨM ---
        if (req.getItems() == null || req.getItems().isEmpty()) throw new RuntimeException("Giỏ hàng trống!");

        BigDecimal subTotal = BigDecimal.ZERO;
        List<HoaDonChiTiet> chiTietList = new ArrayList<>();

        for (CheckoutRequest.CartItem itemDTO : req.getItems()) {
            SanPhamChiTiet spct = sanPhamCTRepository.findById(itemDTO.getSanPhamChiTietId())
                    .orElseThrow(() -> new RuntimeException("SP không tồn tại"));

            if (spct.getSoLuong() < itemDTO.getSoLuong())
                throw new RuntimeException("Hết hàng: " + spct.getSanPham().getTenSanPham());

            HoaDonChiTiet cthd = new HoaDonChiTiet();
            cthd.setHoaDon(hoaDon);
            cthd.setSanPhamChiTiet(spct);
            cthd.setSoLuong(itemDTO.getSoLuong());
            cthd.setDonGia(itemDTO.getDonGia());
            cthd.setThanhTien(cthd.getDonGia().multiply(BigDecimal.valueOf(cthd.getSoLuong())));
            chiTietList.add(cthd);
            subTotal = subTotal.add(cthd.getThanhTien());
        }

        // --- D. XỬ LÝ VOUCHER ---
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (req.getVoucherCode() != null && !req.getVoucherCode().trim().isEmpty()) {
            Optional<PhieuGiamGia> pggOpt = phieuGiamGiaRepository.findByMaPhieuGiamGia(req.getVoucherCode());
            if (pggOpt.isPresent()) {
                PhieuGiamGia pgg = pggOpt.get();
                if (pgg.getHinhThucGiam() == 2) {
                    discountAmount = subTotal.multiply(pgg.getGiaTriGiam()).divide(new BigDecimal(100), 0, RoundingMode.HALF_UP);
                    if (pgg.getSoTienGiam() != null && discountAmount.compareTo(pgg.getSoTienGiam()) > 0)
                        discountAmount = pgg.getSoTienGiam();
                } else {
                    discountAmount = pgg.getGiaTriGiam();
                }
                if (discountAmount.compareTo(subTotal) > 0) discountAmount = subTotal;
                hoaDon.setPhieuGiamGia(pgg);
            }
        }

        // --- E. CHỐT TIỀN ---
        BigDecimal shippingFee = req.getShippingFee() != null ? req.getShippingFee() : BigDecimal.ZERO;
        BigDecimal finalTotal = subTotal.subtract(discountAmount).add(shippingFee);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) finalTotal = BigDecimal.ZERO;

        hoaDon.setTongTien(subTotal);
        hoaDon.setTienGiamGia(discountAmount);
        hoaDon.setPhiVanChuyen(shippingFee);
        hoaDon.setTongTienSauGiam(finalTotal);

        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
        for (HoaDonChiTiet ct : chiTietList) {
            ct.setHoaDon(savedHoaDon);
            hoaDonChiTietRepository.save(ct);
        }

        // --- F. THANH TOÁN ---
        String redirectUrl = "";
        if ("VNPAY".equals(req.getPaymentMethod())) {
            savedHoaDon.setTrangThai(5); // Chờ thanh toán
            savedHoaDon.setHinhThucThanhToan(2);
            hoaDonRepository.save(savedHoaDon);

            try {
                long amountInCents = finalTotal.multiply(new BigDecimal(100)).longValue();
                redirectUrl = vnPayService.createOrder(amountInCents, "Thanh toan " + maHoaDon, maHoaDon, clientIp);
            } catch (Exception e) {
                throw new RuntimeException("Lỗi tạo link VNPay");
            }

        } else { // COD
            savedHoaDon.setTrangThai(1); // Chờ xác nhận
            savedHoaDon.setHinhThucThanhToan(0);
            hoaDonRepository.save(savedHoaDon);

            // Trừ kho ngay
            for (HoaDonChiTiet ct : chiTietList) {
                SanPhamChiTiet spct = ct.getSanPhamChiTiet();
                spct.setSoLuong(spct.getSoLuong() - ct.getSoLuong());
                sanPhamCTRepository.save(spct);
            }
            // Trừ voucher
            if (savedHoaDon.getPhieuGiamGia() != null) {
                PhieuGiamGia pgg = savedHoaDon.getPhieuGiamGia();
                if (pgg.getSoLuong() > 0) {
                    pgg.setSoLuong(pgg.getSoLuong() - 1);
                    phieuGiamGiaRepository.save(pgg);
                }
            }
            redirectUrl = "/checkout/success?id=" + savedHoaDon.getId();
        }

        return new CheckoutResponse(true, "Thành công", redirectUrl);
    }

    // =========================================================================
    // 3. TẠO THANH TOÁN VNPAY (ĐÃ IMPLEMENT ĐẦY ĐỦ)
    // =========================================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public VnPayResponseDTO taoThanhToanVnPay(CreateOrderRequest request, String ipAddress) {
        // 1. Tạo hóa đơn tạm từ CreateOrderRequest (Logic đơn giản hơn placeOrder vì thiếu địa chỉ)
        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon(request.getMaHoaDon());
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setTongTien(request.getTongTien()); // Đã tính toán
        hoaDon.setTongTienSauGiam(request.getTongTien());
        hoaDon.setTrangThai(5); // Chờ thanh toán
        hoaDon.setHinhThucBanHang(0);

        // Gán User/NV nếu có
        if (request.getNhanVienId() != null)
            hoaDon.setNhanVien(nhanVienRepository.findById(request.getNhanVienId()).orElse(null));
        if (request.getKhachHangId() != null)
            hoaDon.setKhachHang(khachHangRepository.findById(request.getKhachHangId()).orElse(null));

        HoaDon savedHd = hoaDonRepository.save(hoaDon);

        // Lưu sản phẩm (nếu có)
        if (request.getDanhSachSanPham() != null) {
            for (CreateOrderRequest.SanPhamItem item : request.getDanhSachSanPham()) {
                SanPhamChiTiet spct = sanPhamCTRepository.findById(item.getSanPhamChiTietId()).orElseThrow();
                HoaDonChiTiet ct = new HoaDonChiTiet();
                ct.setHoaDon(savedHd);
                ct.setSanPhamChiTiet(spct);
                ct.setSoLuong(item.getSoLuong());
                ct.setDonGia(item.getDonGia());
                ct.setThanhTien(item.getDonGia().multiply(BigDecimal.valueOf(item.getSoLuong())));
                hoaDonChiTietRepository.save(ct);
            }
        }

        // 2. Tạo Link VNPay
        try {
            long amount = request.getTongTien().multiply(new BigDecimal(100)).longValue();
            String url = vnPayService.createOrder(amount, "Thanh toan " + request.getMaHoaDon(), request.getMaHoaDon(), ipAddress);
            return new VnPayResponseDTO(true, "Tạo link thành công", url);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo link: " + e.getMessage());
        }
    }



@Override
@Transactional(rollbackFor = Exception.class)
public void decrementInventory(List<CreateOrderRequest.SanPhamItem> items) {
    for (CreateOrderRequest.SanPhamItem item : items) {
        SanPhamChiTiet spct = sanPhamCTRepository.findById(item.getSanPhamChiTietId())
                .orElseThrow(() -> new RuntimeException("SP không tồn tại"));

        int newStock = spct.getSoLuong() - item.getSoLuong();
        if (newStock < 0) throw new RuntimeException("Sản phẩm hết hàng!");

        spct.setSoLuong(newStock);
        sanPhamCTRepository.save(spct);
    }
}

@Override
@Transactional(rollbackFor = Exception.class)
public void decrementVoucher(PhieuGiamGia pgg) {
    if (pgg == null) return;
    PhieuGiamGia voucherDb = phieuGiamGiaRepository.findById(pgg.getId())
            .orElseThrow(() -> new RuntimeException("Voucher lỗi"));

    if (voucherDb.getSoLuong() != null && voucherDb.getSoLuong() > 0) {
        voucherDb.setSoLuong(voucherDb.getSoLuong() - 1);
        phieuGiamGiaRepository.save(voucherDb);
    }
}

// --- HÀM HELPER ---

// Chuyển đổi DTO CartItem -> SanPhamItem để dùng chung hàm trừ kho
private List<CreateOrderRequest.SanPhamItem> mapToSanPhamItems(List<CheckoutRequest.CartItem> cartItems) {
    List<CreateOrderRequest.SanPhamItem> list = new ArrayList<>();
    for (CheckoutRequest.CartItem c : cartItems) {
        list.add(new CreateOrderRequest.SanPhamItem(c.getSanPhamChiTietId(), c.getSoLuong(), c.getDonGia()));
    }
    return list;
}

}
