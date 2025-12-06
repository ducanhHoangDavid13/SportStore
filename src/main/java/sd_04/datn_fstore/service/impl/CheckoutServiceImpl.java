package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.dto.*;
import sd_04.datn_fstore.model.*;
import sd_04.datn_fstore.repository.*;
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
import java.util.stream.Collectors;

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
    // 1. TÍNH TOÁN TỔNG TIỀN
    // =========================================================================
    @Override
    public CalculateTotalResponse calculateOrderTotal(CalculateTotalRequest request) {
        BigDecimal subTotal = BigDecimal.ZERO;

        // 1. Tính tổng tiền hàng từ DB (An toàn)
        if (request.getItems() != null) {
            for (CalculateTotalRequest.CartItem item : request.getItems()) {
                Optional<SanPhamChiTiet> spctOpt = sanPhamCTRepository.findById(item.getSanPhamChiTietId());
                if (spctOpt.isPresent()) {
                    BigDecimal realPrice = spctOpt.get().getGiaTien();
                    subTotal = subTotal.add(realPrice.multiply(BigDecimal.valueOf(item.getSoLuong())));
                }
            }
        }

        BigDecimal shippingFee = request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        boolean voucherValid = false;
        String voucherMessage = "";

        // 2. Xử lý Voucher
        if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
            Optional<PhieuGiamGia> pggOpt = phieuGiamGiaRepository.findByMaPhieuGiamGia(request.getVoucherCode());

            if (pggOpt.isPresent()) {
                PhieuGiamGia pgg = pggOpt.get();
                LocalDateTime now = LocalDateTime.now();

                if (now.isBefore(pgg.getNgayBatDau())) {
                    voucherMessage = "Mã chưa bắt đầu!";
                } else if (now.isAfter(pgg.getNgayKetThuc())) {
                    voucherMessage = "Mã đã hết hạn!";
                } else if (pgg.getTrangThai() != 1) {
                    voucherMessage = "Mã đã bị hủy!";
                } else if (pgg.getSoLuong() != null && pgg.getSoLuong() <= 0) {
                    voucherMessage = "Mã đã hết lượt dùng!";
                } else if (pgg.getDieuKienGiamGia() != null && subTotal.compareTo(pgg.getDieuKienGiamGia()) < 0) {
                    voucherMessage = "Đơn chưa đạt tối thiểu " + String.format("%,.0f", pgg.getDieuKienGiamGia()) + "đ";
                } else {
                    // Tính tiền giảm
                    BigDecimal giaTriGiam = pgg.getGiaTriGiam();
                    if (pgg.getHinhThucGiam() == 2) { // %
                        if (giaTriGiam.compareTo(new BigDecimal(100)) > 0) giaTriGiam = new BigDecimal(100);
                        discountAmount = subTotal.multiply(giaTriGiam).divide(new BigDecimal(100), 0, RoundingMode.HALF_UP);
                        if (pgg.getSoTienGiam() != null && discountAmount.compareTo(pgg.getSoTienGiam()) > 0) {
                            discountAmount = pgg.getSoTienGiam();
                        }
                    } else { // Tiền mặt
                        discountAmount = giaTriGiam;
                    }
                    if (discountAmount.compareTo(subTotal) > 0) discountAmount = subTotal;

                    voucherValid = true;
                    voucherMessage = "Áp dụng thành công: -" + String.format("%,.0f", discountAmount) + "đ";
                }
            } else {
                voucherMessage = "Mã không tồn tại";
            }
        }

        BigDecimal finalTotal = subTotal.add(shippingFee).subtract(discountAmount);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) finalTotal = BigDecimal.ZERO;

        return new CalculateTotalResponse(subTotal, shippingFee, discountAmount, finalTotal, voucherMessage, voucherValid);
    }

    // =========================================================================
    // 2. XỬ LÝ ĐẶT HÀNG (CORE)
    // =========================================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckoutResponse placeOrder(CheckoutRequest req, String clientIp) {
        HoaDon hoaDon = new HoaDon();
        String maHoaDon = "HD" + System.currentTimeMillis(); // Nên dùng UUID hoặc sequence để tránh trùng
        hoaDon.setMaHoaDon(maHoaDon);
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setHinhThucBanHang(0); // Online
        hoaDon.setMoTa(req.getNote());

        // Nếu khách đã đăng nhập (có ID), gắn vào hóa đơn
        if (req.getKhachHangId() != null) {
            // hoaDon.setKhachHang(khachHangRepo.findById(req.getKhachHangId()).orElse(null));
            // (Bạn tự bỏ comment dòng trên nếu có KhachHangRepo)
        }

        // 1. Lưu địa chỉ
        DiaChi shippingInfo = new DiaChi();
        shippingInfo.setHoTen(req.getFullName());
        shippingInfo.setSoDienThoai(req.getPhone());
        shippingInfo.setDiaChiCuThe(req.getAddressDetail());
        shippingInfo.setXa(req.getWard());
        shippingInfo.setThanhPho(req.getDistrict() + " - " + req.getCity());
        shippingInfo.setGhiChu("Email: " + req.getEmail());
        shippingInfo.setLoaiDiaChi("Giao hàng");
        shippingInfo.setTrangThai(1);

        // Nếu có User ID thì set id khách hàng cho địa chỉ luôn (nếu cần)
        // shippingInfo.setKhachHangId(req.getKhachHangId());

        DiaChi savedDiaChi = diaChiRepo.save(shippingInfo);
        hoaDon.setDiaChiGiaoHang(savedDiaChi);

        // 2. Tạo Hóa Đơn Chi Tiết
        if (req.getItems() == null || req.getItems().isEmpty()) throw new RuntimeException("Giỏ hàng trống!");

        BigDecimal subTotal = BigDecimal.ZERO;
        List<HoaDonChiTiet> chiTietList = new ArrayList<>();

        for (CheckoutRequest.CartItem itemDTO : req.getItems()) {
            SanPhamChiTiet spct = sanPhamCTRepository.findById(itemDTO.getSanPhamChiTietId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm ID " + itemDTO.getSanPhamChiTietId() + " không tồn tại"));

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

        // 3. Tính Voucher (Re-calculate logic)
        BigDecimal discountAmount = BigDecimal.ZERO;
        PhieuGiamGia voucherToUse = null;

        if (req.getVoucherCode() != null && !req.getVoucherCode().isEmpty()) {
            CalculateTotalRequest calcReq = new CalculateTotalRequest();
            calcReq.setVoucherCode(req.getVoucherCode());
            calcReq.setShippingFee(req.getShippingFee());

            // Convert CartItem
            List<CalculateTotalRequest.CartItem> calcItems = req.getItems().stream().map(i -> {
                CalculateTotalRequest.CartItem ci = new CalculateTotalRequest.CartItem();
                ci.setSanPhamChiTietId(i.getSanPhamChiTietId());
                ci.setSoLuong(i.getSoLuong());
                return ci;
            }).collect(Collectors.toList());
            calcReq.setItems(calcItems);

            CalculateTotalResponse calcRes = calculateOrderTotal(calcReq);
            if (calcRes.isVoucherValid()) {
                discountAmount = calcRes.getDiscountAmount();

                // Lấy đối tượng Voucher để lưu và trừ số lượng sau này
                voucherToUse = phieuGiamGiaRepository.findByMaPhieuGiamGia(req.getVoucherCode()).orElse(null);
                hoaDon.setPhieuGiamGia(voucherToUse);
            }
        }

        // 4. Chốt số tiền
        BigDecimal shippingFee = req.getShippingFee() != null ? req.getShippingFee() : BigDecimal.ZERO;
        BigDecimal finalTotal = subTotal.subtract(discountAmount).add(shippingFee);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) finalTotal = BigDecimal.ZERO;

        hoaDon.setTongTien(subTotal);
        hoaDon.setTienGiamGia(discountAmount);
        hoaDon.setPhiVanChuyen(shippingFee);
        hoaDon.setTongTienSauGiam(finalTotal);

        // Lưu Hóa đơn & Chi tiết vào DB
        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
        for (HoaDonChiTiet ct : chiTietList) {
            ct.setHoaDon(savedHoaDon);
            hoaDonChiTietRepository.save(ct);
        }

        // 5. Xử lý Thanh Toán
        String redirectUrl = "";

        if ("VNPAY".equalsIgnoreCase(req.getPaymentMethod())) {
            savedHoaDon.setTrangThai(1); // Chờ thanh toán
            savedHoaDon.setHinhThucThanhToan(2); // VNPAY
            hoaDonRepository.save(savedHoaDon);

            try {
                long amountInCents = finalTotal.multiply(new BigDecimal(100)).longValue();
                // Gọi Service VNPay
                redirectUrl = vnPayService.createOrder(amountInCents, "Thanh toan " + maHoaDon, maHoaDon, clientIp);
                return new CheckoutResponse(true, "Chuyển hướng VNPay", redirectUrl);
            } catch (Exception e) {
                throw new RuntimeException("Lỗi tạo link VNPay: " + e.getMessage());
            }

        } else {
            // COD hoặc Chuyển khoản thường
            savedHoaDon.setTrangThai(1); // Chờ xác nhận
            savedHoaDon.setHinhThucThanhToan(0); // COD
            hoaDonRepository.save(savedHoaDon);

            // Trừ kho & Voucher ngay lập tức
            decrementInventory(mapToSanPhamItems(req.getItems()));
            if (voucherToUse != null) {
                decrementVoucher(voucherToUse);
            }

            // Thông báo
            thongBaoService.createNotification("Đơn hàng mới #" + maHoaDon,
                    "Khách " + req.getFullName() + " đã đặt đơn hàng trị giá " + String.format("%,.0f", finalTotal),
                    "ORDER", "/admin/hoa-don/detail/" + savedHoaDon.getId());

            redirectUrl = "/checkout/success?id=" + savedHoaDon.getId();
            return new CheckoutResponse(true, "Đặt hàng thành công", redirectUrl);
        }
    }

    // =========================================================================
    // 3. HÀM GỌI VNPAY TỪ CONTROLLER (ĐÃ SỬA LỖI)
    // =========================================================================
    @Override
    public VnPayResponseDTO taoThanhToanVnPay(CheckoutRequest request, String ipAddress) {
        // 1. Ép kiểu thanh toán là VNPAY
        request.setPaymentMethod("VNPAY");

        // 2. Gọi hàm placeOrder (hàm này đã chứa logic lưu DB và tạo link VNPay)
        // Sửa lỗi: Truyền đúng 2 tham số (request, ipAddress)
        CheckoutResponse response = this.placeOrder(request, ipAddress);

        // 3. Trả về kết quả cho Controller
        if (response.isSuccess()) {
            return new VnPayResponseDTO(true, "Tạo link VNPAY thành công", response.getRedirectUrl());
        } else {
            throw new RuntimeException("Không thể tạo link thanh toán: " + response.getMessage());
        }
    }

    // =========================================================================
    // 4. HELPER METHODS
    // =========================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementInventory(List<CreateOrderRequest.SanPhamItem> items) {
        for (CreateOrderRequest.SanPhamItem item : items) {
            SanPhamChiTiet spct = sanPhamCTRepository.findById(item.getSanPhamChiTietId())
                    .orElseThrow(() -> new RuntimeException("SP ID " + item.getSanPhamChiTietId() + " không tồn tại"));

            int newStock = spct.getSoLuong() - item.getSoLuong();
            if (newStock < 0) throw new RuntimeException("Sản phẩm " + spct.getSanPham().getTenSanPham() + " hết hàng!");

            spct.setSoLuong(newStock);
            sanPhamCTRepository.save(spct);

            // Cảnh báo hết hàng
            if (newStock <= 5) {
                thongBaoService.createNotification("Cảnh báo sắp hết hàng",
                        "Sản phẩm " + spct.getSanPham().getTenSanPham() + " còn " + newStock,
                        "STOCK", "/admin/san-pham/" + spct.getSanPham().getId());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementVoucher(PhieuGiamGia pgg) {
        // Giảm số lượng voucher
        phieuGiamgiaService.decrementVoucher(pgg);
    }

    private List<CreateOrderRequest.SanPhamItem> mapToSanPhamItems(List<CheckoutRequest.CartItem> cartItems) {
        return cartItems.stream()
                .map(c -> new CreateOrderRequest.SanPhamItem(c.getSanPhamChiTietId(), c.getSoLuong(), BigDecimal.ZERO))
                .collect(Collectors.toList());
    }
}