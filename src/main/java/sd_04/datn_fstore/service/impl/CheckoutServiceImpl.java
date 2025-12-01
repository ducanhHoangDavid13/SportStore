package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.dto.*;
import sd_04.datn_fstore.model.*;
import sd_04.datn_fstore.repository.*;
import sd_04.datn_fstore.service.BanHangService;
import sd_04.datn_fstore.service.CheckoutService;
import sd_04.datn_fstore.service.PhieuGiamgiaService;
import sd_04.datn_fstore.service.ThongBaoService;
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

    private final VnPayService vnPayService;
    private final PhieuGiamGiaRepo phieuGiamGiaRepository;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final SanPhamCTRepository sanPhamCTRepository;
    private final DiaChiRepo diaChiRepo;
    private final ThongBaoService thongBaoService;
    private final PhieuGiamgiaService phieuGiamgiaService;

    // =========================================================================
    // 1. TÍNH TOÁN TỔNG TIỀN (SỬA THEO ENTITY MỚI)
    @Override
    public CalculateTotalResponse calculateOrderTotal(CalculateTotalRequest request) {
        // 1. Tính tổng tiền hàng (SubTotal)
        BigDecimal subTotal = BigDecimal.ZERO;
        if (request.getItems() != null) {
            for (CalculateTotalRequest.CartItem item : request.getItems()) {
                BigDecimal price = item.getDonGia();
                if (price == null) {
                    // Fallback: Nếu frontend không gửi giá, lấy từ DB để an toàn
                    SanPhamChiTiet spct = sanPhamCTRepository.findById(item.getSanPhamChiTietId()).orElse(null);
                    price = (spct != null) ? spct.getGiaTien() : BigDecimal.ZERO;
                }
                subTotal = subTotal.add(price.multiply(BigDecimal.valueOf(item.getSoLuong())));
            }
        }

        BigDecimal shippingFee = request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        boolean voucherValid = false;
        String voucherMessage = "";

        // 2. XỬ LÝ VOUCHER
        if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
            Optional<PhieuGiamGia> pggOpt = phieuGiamGiaRepository.findByMaPhieuGiamGia(request.getVoucherCode());

            if (pggOpt.isPresent()) {
                PhieuGiamGia pgg = pggOpt.get();
                LocalDateTime now = LocalDateTime.now();

                // Check 1: Thời gian & Trạng thái & Số lượng
                if (pgg.getTrangThai() != 1 || (pgg.getSoLuong() != null && pgg.getSoLuong() <= 0)) {
                    voucherMessage = "Mã giảm giá đã hết lượt sử dụng!";
                }
                else if (now.isBefore(pgg.getNgayBatDau()) || now.isAfter(pgg.getNgayKetThuc())) {
                    voucherMessage = "Mã giảm giá chưa bắt đầu hoặc đã hết hạn!";
                }
                // Check 2: ĐIỀU KIỆN GIẢM GIÁ (Tên phương thức mới: getDieuKienGiamGia())
                else if (pgg.getDieuKienGiamGia() != null && subTotal.compareTo(pgg.getDieuKienGiamGia()) < 0) {
                    voucherMessage = "Đơn hàng chưa đạt tối thiểu " + String.format("%,.0f", pgg.getDieuKienGiamGia()) + "đ";
                }
                else {
                    // --- ĐỦ ĐIỀU KIỆN ---
                    BigDecimal giaTriGiam = pgg.getGiaTriGiam() == null ? BigDecimal.ZERO : pgg.getGiaTriGiam();
                    BigDecimal giamToiDa = pgg.getSoTienGiam(); // Tên phương thức mới: getSoTienGiam()

                    if (pgg.getHinhThucGiam() == 2) { // Giảm %
                        // Logic fix: Chặn max 100%
                        if(giaTriGiam.compareTo(new BigDecimal(100)) > 0) giaTriGiam = new BigDecimal(100);

                        discountAmount = subTotal.multiply(giaTriGiam).divide(new BigDecimal(100), 0, RoundingMode.HALF_UP);

                        // Check giảm tối đa
                        if (giamToiDa != null && discountAmount.compareTo(giamToiDa) > 0) {
                            discountAmount = giamToiDa;
                        }
                    } else { // Giảm tiền mặt
                        discountAmount = giaTriGiam;
                    }

                    // Chốt: Không giảm quá giá trị đơn hàng
                    if (discountAmount.compareTo(subTotal) > 0) discountAmount = subTotal;

                    voucherValid = true;
                    voucherMessage = "Áp dụng mã thành công: -" + String.format("%,.0f", discountAmount) + "đ";
                }
            } else {
                voucherMessage = "Mã giảm giá không tồn tại.";
            }
        }

        BigDecimal finalTotal = subTotal.add(shippingFee).subtract(discountAmount);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) finalTotal = BigDecimal.ZERO;

        return new CalculateTotalResponse(subTotal, shippingFee, discountAmount, finalTotal, voucherMessage, voucherValid);
    }

    // =========================================================================
    // 2. XỬ LÝ ĐẶT HÀNG (SỬA THEO ENTITY MỚI)
    // =========================================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckoutResponse placeOrder(CheckoutRequest req, String clientIp) {
        HoaDon hoaDon = new HoaDon();
        String maHoaDon = "HD" + System.currentTimeMillis();
        hoaDon.setMaHoaDon(maHoaDon);
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setHinhThucBanHang(0);
        hoaDon.setMoTa(req.getNote());

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

        if (req.getItems() == null || req.getItems().isEmpty()) throw new RuntimeException("Giỏ hàng trống!");

        BigDecimal subTotal = BigDecimal.ZERO;
        List<HoaDonChiTiet> chiTietList = new ArrayList<>();

        for (CheckoutRequest.CartItem itemDTO : req.getItems()) {
            SanPhamChiTiet spct = sanPhamCTRepository.findById(itemDTO.getSanPhamChiTietId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm (ID: " + itemDTO.getSanPhamChiTietId() + ") không tồn tại!"));

            if (spct.getSoLuong() < itemDTO.getSoLuong()) {
                throw new RuntimeException("Sản phẩm " + spct.getSanPham().getTenSanPham() + " không đủ hàng!");
            }

            HoaDonChiTiet cthd = new HoaDonChiTiet();
            cthd.setHoaDon(hoaDon);
            cthd.setSanPhamChiTiet(spct);
            cthd.setSoLuong(itemDTO.getSoLuong());
            cthd.setDonGia(spct.getGiaTien());
            cthd.setThanhTien(cthd.getDonGia().multiply(BigDecimal.valueOf(cthd.getSoLuong())));

            chiTietList.add(cthd);
            subTotal = subTotal.add(cthd.getThanhTien());
        }

        // --- TÍNH LẠI VOUCHER KHI LƯU (ĐÃ CẬP NHẬT LOGIC CHECK ĐƠN TỐI THIỂU) ---
        BigDecimal discountAmount = BigDecimal.ZERO;
        PhieuGiamGia voucherToUse = null;

        if (req.getVoucherCode() != null && !req.getVoucherCode().trim().isEmpty()) {
            Optional<PhieuGiamGia> pggOpt = phieuGiamGiaRepository.findByMaPhieuGiamGia(req.getVoucherCode());
            if (pggOpt.isPresent()) {
                PhieuGiamGia pgg = pggOpt.get();

                // 1. Kiểm tra điều kiện sử dụng
                boolean isTimeValid = !LocalDateTime.now().isBefore(pgg.getNgayBatDau()) && !LocalDateTime.now().isAfter(pgg.getNgayKetThuc());
                boolean isActive = pgg.getTrangThai() == 1 && (pgg.getSoLuong() == null || pgg.getSoLuong() > 0);

                // 2. Kiểm tra Đơn tối thiểu (Sử dụng getDieuKienGiamGia())
                boolean isMinOrderValid = true;
                if (pgg.getDieuKienGiamGia() != null && subTotal.compareTo(pgg.getDieuKienGiamGia()) < 0) {
                    isMinOrderValid = false;
                    // Nếu không đủ điều kiện đơn tối thiểu, không áp dụng voucher
                }

                if (isActive && isTimeValid && isMinOrderValid) {
                    BigDecimal giaTriGiam = pgg.getGiaTriGiam() == null ? BigDecimal.ZERO : pgg.getGiaTriGiam();
                    BigDecimal giamToiDa = pgg.getSoTienGiam(); // Sử dụng getSoTienGiam()

                    if (pgg.getHinhThucGiam() == 2) {
                        if(giaTriGiam.compareTo(new BigDecimal(100)) > 0) giaTriGiam = new BigDecimal(100);

                        discountAmount = subTotal.multiply(giaTriGiam).divide(new BigDecimal(100), 0, RoundingMode.HALF_UP);

                        if (giamToiDa != null && discountAmount.compareTo(giamToiDa) > 0) {
                            discountAmount = giamToiDa;
                        }
                    } else {
                        discountAmount = giaTriGiam;
                    }
                    if (discountAmount.compareTo(subTotal) > 0) discountAmount = subTotal;

                    voucherToUse = pgg;
                    hoaDon.setPhieuGiamGia(pgg);
                }
            }
        }

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

        String redirectUrl = "";

        if ("VNPAY".equals(req.getPaymentMethod())) {
            savedHoaDon.setTrangThai(5);
            savedHoaDon.setHinhThucThanhToan(2);
            hoaDonRepository.save(savedHoaDon);
            try {
                // Đảm bảo số tiền VNPay là Long và không có số thập phân
                long amountInCents = finalTotal.multiply(new BigDecimal(100)).longValue();
                redirectUrl = vnPayService.createOrder(amountInCents, "Thanh toan " + maHoaDon, maHoaDon, clientIp);
            } catch (Exception e) {
                throw new RuntimeException("Lỗi tạo link VNPay: " + e.getMessage());
            }
        } else {
            savedHoaDon.setTrangThai(1);
            savedHoaDon.setHinhThucThanhToan(0);
            hoaDonRepository.save(savedHoaDon);

            decrementInventory(mapToSanPhamItems(req.getItems()));

            if (voucherToUse != null) {
                decrementVoucher(voucherToUse);
            }

            thongBaoService.createNotification(
                    "Đơn hàng mới #" + maHoaDon,
                    "Khách " + req.getFullName() + " đặt đơn " + String.format("%,.0f", finalTotal) + "đ",
                    "ORDER",
                    "/admin/hoa-don/detail/" + savedHoaDon.getId()
            );

            redirectUrl = "/checkout/success?id=" + savedHoaDon.getId();
        }

        return new CheckoutResponse(true, "Thành công", redirectUrl);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VnPayResponseDTO taoThanhToanVnPay(CreateOrderRequest request, String ipAddress) {
        return null; // Logic POS giữ nguyên
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementInventory(List<CreateOrderRequest.SanPhamItem> items) {
        for (CreateOrderRequest.SanPhamItem item : items) {
            SanPhamChiTiet spct = sanPhamCTRepository.findById(item.getSanPhamChiTietId())
                    .orElseThrow(() -> new RuntimeException("SP không tồn tại ID: " + item.getSanPhamChiTietId()));

            int newStock = spct.getSoLuong() - item.getSoLuong();
            if (newStock < 0) throw new RuntimeException("Sản phẩm " + spct.getSanPham().getTenSanPham() + " hết hàng!");

            spct.setSoLuong(newStock);
            sanPhamCTRepository.save(spct);

            if (newStock <= 5) {
                thongBaoService.createNotification(
                        "Cảnh báo sắp hết hàng",
                        "Sản phẩm " + spct.getSanPham().getTenSanPham() + " chỉ còn " + newStock + ".",
                        "STOCK",
                        "/admin/san-pham/" + spct.getSanPham().getId()
                );
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementVoucher(PhieuGiamGia pgg) {
        phieuGiamgiaService.decrementVoucher(pgg);
    }

    private List<CreateOrderRequest.SanPhamItem> mapToSanPhamItems(List<CheckoutRequest.CartItem> cartItems) {
        List<CreateOrderRequest.SanPhamItem> list = new ArrayList<>();
        for (CheckoutRequest.CartItem c : cartItems) {
            list.add(new CreateOrderRequest.SanPhamItem(c.getSanPhamChiTietId(), c.getSoLuong(), c.getDonGia()));
        }
        return list;
    }
}