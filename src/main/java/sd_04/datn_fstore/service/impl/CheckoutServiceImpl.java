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
    // 1. TÍNH TOÁN TỔNG TIỀN (SỬA LOGIC VOUCHER)
    // =========================================================================
    @Override
    public CalculateTotalResponse calculateOrderTotal(CalculateTotalRequest request) {
        // 1. Tính tổng tiền hàng (SubTotal) - LẤY GIÁ TỪ DATABASE
        BigDecimal subTotal = BigDecimal.ZERO;
        if (request.getItems() != null) {
            for (CalculateTotalRequest.CartItem item : request.getItems()) {

                // 🔥 LUÔN LẤY GIÁ CHUẨN TỪ DATABASE
                Optional<SanPhamChiTiet> spctOpt = sanPhamCTRepository.findById(item.getSanPhamChiTietId());
                if (spctOpt.isEmpty()) {
                    continue; // Bỏ qua nếu SP không tồn tại
                }

                SanPhamChiTiet spct = spctOpt.get();
                BigDecimal realPrice = spct.getGiaTien() != null ? spct.getGiaTien() : BigDecimal.ZERO;

                subTotal = subTotal.add(realPrice.multiply(BigDecimal.valueOf(item.getSoLuong())));
            }
        }

        BigDecimal shippingFee = request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        boolean voucherValid = false;
        String voucherMessage = "";

        // 2. XỬ LÝ VOUCHER (Logic voucher đã được tách rõ ràng hơn)
        if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
            Optional<PhieuGiamGia> pggOpt = phieuGiamGiaRepository.findByMaPhieuGiamGia(request.getVoucherCode());

            if (pggOpt.isPresent()) {
                PhieuGiamGia pgg = pggOpt.get();
                LocalDateTime now = LocalDateTime.now();

                // 🔥 ĐÃ SỬA: Đảm bảo thứ tự kiểm tra: Thời gian -> Trạng thái -> Số lượng -> Điều kiện

                // Check 1: Thời gian (Ưu tiên hết hạn/chưa bắt đầu)
                if (now.isBefore(pgg.getNgayBatDau())) {
                    voucherMessage = "Mã giảm giá chưa đến ngày bắt đầu sử dụng!";
                }
                else if (now.isAfter(pgg.getNgayKetThuc())) {
                    voucherMessage = "Mã giảm giá đã hết hạn sử dụng!";
                }
                // Check 2: Trạng thái (0: Đang chạy)
                else if (pgg.getTrangThai() != 0) {
                    voucherMessage = "Mã giảm giá này đã bị dừng/hủy!";
                }
                // Check 3: Số lượng
                else if (pgg.getSoLuong() != null && pgg.getSoLuong() <= 0) {
                    voucherMessage = "Mã giảm giá đã hết lượt sử dụng!";
                }
                // Check 4: ĐIỀU KIỆN GIẢM GIÁ
                else if (pgg.getDieuKienGiamGia() != null && subTotal.compareTo(pgg.getDieuKienGiamGia()) < 0) {
                    voucherMessage = "Đơn hàng chưa đạt tối thiểu " + String.format("%,.0f", pgg.getDieuKienGiamGia()) + "đ";
                }
                else {
                    // --- ĐỦ ĐIỀU KIỆN ---
                    BigDecimal giaTriGiam = pgg.getGiaTriGiam() == null ? BigDecimal.ZERO : pgg.getGiaTriGiam();
                    BigDecimal giamToiDa = pgg.getSoTienGiam();

                    if (pgg.getHinhThucGiam() == 2) { // Giảm %
                        if(giaTriGiam.compareTo(new BigDecimal(100)) > 0) giaTriGiam = new BigDecimal(100);
                        // Dùng RoundingMode.HALF_UP để làm tròn lên khi số thập phân >= 0.5
                        discountAmount = subTotal.multiply(giaTriGiam).divide(new BigDecimal(100), 0, RoundingMode.HALF_UP);
                        if (giamToiDa != null && discountAmount.compareTo(giamToiDa) > 0) {
                            discountAmount = giamToiDa;
                        }
                    } else { // Giảm tiền mặt
                        discountAmount = giaTriGiam;
                    }

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
    // 2. XỬ LÝ ĐẶT HÀNG (SỬA THEO ENTITY MỚI) - Bao gồm cả xử lý VNPay
    // =========================================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckoutResponse placeOrder(CheckoutRequest req, String clientIp) {
        HoaDon hoaDon = new HoaDon();
        String maHoaDon = "HD" + System.currentTimeMillis();
        hoaDon.setMaHoaDon(maHoaDon);
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setHinhThucBanHang(0); // 0: Online
        hoaDon.setMoTa(req.getNote());

        // 1. LƯU ĐỊA CHỈ GIAO HÀNG
        DiaChi shippingInfo = new DiaChi();
        shippingInfo.setHoTen(req.getFullName());
        shippingInfo.setSoDienThoai(req.getPhone());
        shippingInfo.setDiaChiCuThe(req.getAddressDetail());
        shippingInfo.setXa(req.getWard());
        shippingInfo.setThanhPho(req.getDistrict() + " - " + req.getCity()); // Lưu Tỉnh/TP và Quận/Huyện
        shippingInfo.setGhiChu("Email: " + req.getEmail());
        shippingInfo.setLoaiDiaChi("Giao hàng");
        shippingInfo.setTrangThai(1); // Mặc định là Active
        DiaChi savedDiaChi = diaChiRepo.save(shippingInfo);
        hoaDon.setDiaChiGiaoHang(savedDiaChi);

        if (req.getItems() == null || req.getItems().isEmpty()) throw new RuntimeException("Giỏ hàng trống!");

        BigDecimal subTotal = BigDecimal.ZERO;
        List<HoaDonChiTiet> chiTietList = new ArrayList<>();

        // 2. TẠO CHI TIẾT VÀ CHECK TỒN KHO LẦN CUỐI
        for (CheckoutRequest.CartItem itemDTO : req.getItems()) {
            SanPhamChiTiet spct = sanPhamCTRepository.findById(itemDTO.getSanPhamChiTietId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm (ID: " + itemDTO.getSanPhamChiTietId() + ") không tồn tại!"));

            // Sử dụng getSoLuong() của SanPhamChiTiet
            if (spct.getSoLuong() < itemDTO.getSoLuong()) {
                throw new RuntimeException("Sản phẩm " + spct.getSanPham().getTenSanPham() + " không đủ hàng!");
            }

            HoaDonChiTiet cthd = new HoaDonChiTiet();
            cthd.setHoaDon(hoaDon);
            cthd.setSanPhamChiTiet(spct);
            cthd.setSoLuong(itemDTO.getSoLuong());
            cthd.setDonGia(spct.getGiaTien()); // Lấy giá từ SPCT
            cthd.setThanhTien(cthd.getDonGia().multiply(BigDecimal.valueOf(cthd.getSoLuong())));

            chiTietList.add(cthd);
            subTotal = subTotal.add(cthd.getThanhTien());
        }

        // 3. TÍNH LẠI VOUCHER KHI LƯU
        BigDecimal discountAmount = BigDecimal.ZERO;
        PhieuGiamGia voucherToUse = null;

        if (req.getVoucherCode() != null && !req.getVoucherCode().trim().isEmpty()) {
            Optional<PhieuGiamGia> pggOpt = phieuGiamGiaRepository.findByMaPhieuGiamGia(req.getVoucherCode());
            if (pggOpt.isPresent()) {
                PhieuGiamGia pgg = pggOpt.get();

                // Dùng logic tính toán đã có
                CalculateTotalRequest calcReq = new CalculateTotalRequest();
                calcReq.setVoucherCode(req.getVoucherCode());
                calcReq.setShippingFee(req.getShippingFee());
                // Map items từ CheckoutRequest sang CalculateTotalRequest.CartItem
                List<CalculateTotalRequest.CartItem> calcItems = req.getItems().stream()
                        .map(item -> {
                            CalculateTotalRequest.CartItem c = new CalculateTotalRequest.CartItem();
                            c.setSanPhamChiTietId(item.getSanPhamChiTietId());
                            c.setSoLuong(item.getSoLuong());
                            // Lấy DonGia từ DB để đảm bảo tính toán voucher
                            SanPhamChiTiet spct = sanPhamCTRepository.findById(item.getSanPhamChiTietId()).get();
                            c.setDonGia(spct.getGiaTien());
                            return c;
                        }).collect(java.util.stream.Collectors.toList());
                calcReq.setItems(calcItems);

                // Gọi lại hàm tính toán chính (đã được sửa logic voucher)
                CalculateTotalResponse calcRes = calculateOrderTotal(calcReq);

                if (calcRes.isVoucherValid()) {
                    discountAmount = calcRes.getDiscountAmount();
                    voucherToUse = pgg;
                    hoaDon.setPhieuGiamGia(pgg);
                }
            }
        }

        // Cập nhật lại final total sau khi tính toán
        BigDecimal shippingFee = req.getShippingFee() != null ? req.getShippingFee() : BigDecimal.ZERO;
        BigDecimal finalTotal = subTotal.subtract(discountAmount).add(shippingFee);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) finalTotal = BigDecimal.ZERO;

        // 4. LƯU HÓA ĐƠN
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

        // 5. XỬ LÝ THANH TOÁN
        if ("VNPAY".equals(req.getPaymentMethod())) {
            savedHoaDon.setTrangThai(1); // Chờ thanh toán
            savedHoaDon.setHinhThucThanhToan(2); // VNPay
            hoaDonRepository.save(savedHoaDon);
            try {
                // Đảm bảo số tiền VNPay là Long và không có số thập phân
                long amountInCents = finalTotal.multiply(new BigDecimal(100)).longValue();
                // Sử dụng mã hóa đơn làm mã giao dịch (TxnRef)
                redirectUrl = vnPayService.createOrder(amountInCents, "Thanh toan " + maHoaDon, maHoaDon, clientIp);
            } catch (Exception e) {
                // Rollback giao dịch nếu tạo link VNPay thất bại
                throw new RuntimeException("Lỗi tạo link VNPay: " + e.getMessage());
            }
            // Trả về redirect URL của VNPay
            return new CheckoutResponse(true, "Chuyển hướng VNPay", redirectUrl);

        } else {
            // Thanh toán COD (Thành công ngay)
            savedHoaDon.setTrangThai(1); // Chờ xác nhận
            savedHoaDon.setHinhThucThanhToan(0); // COD
            hoaDonRepository.save(savedHoaDon);

            // Trừ tồn kho và voucher
            decrementInventory(mapToSanPhamItems(req.getItems()));
            if (voucherToUse != null) {
                decrementVoucher(voucherToUse);
            }

            // Gửi thông báo đến Admin
            thongBaoService.createNotification(
                    "Đơn hàng mới #" + maHoaDon,
                    "Khách " + req.getFullName() + " đặt đơn " + String.format("%,.0f", finalTotal) + "đ",
                    "ORDER",
                    "/admin/hoa-don/detail/" + savedHoaDon.getId()
            );

            // Chuyển hướng đến trang thành công
            redirectUrl = "/checkout/success?id=" + savedHoaDon.getId();
            return new CheckoutResponse(true, "Đặt hàng thành công", redirectUrl);
        }
    }

// Đặt đoạn code này vào vị trí của phương thức taoThanhToanVnPay cũ trong CheckoutServiceImpl.java

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VnPayResponseDTO taoThanhToanVnPay(CheckoutRequest request, String ipAddress) {
        // 1. Set phương thức thanh toán là VNPAY để kích hoạt logic VNPAY trong placeOrder
        request.setPaymentMethod("VNPAY");

        // 2. Gọi lại placeOrder. placeOrder sẽ tạo HoaDon, lưu ChiTiet, và tạo URL VNPAY
        CheckoutResponse response = placeOrder(request, ipAddress);

        // 3. Kiểm tra và trả về DTO
        if (response.isSuccess() && response.getRedirectUrl() != null) {
            // placeOrder trả về CheckoutResponse, chuyển đổi sang VnPayResponseDTO
            return new VnPayResponseDTO(true, response.getMessage(), response.getRedirectUrl());
        } else {
            // Nếu placeOrder thất bại (hoặc không trả về URL), throw exception
            throw new RuntimeException("Đặt hàng thất bại hoặc không nhận được URL VNPAY: " + response.getMessage());
        }
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
        // Giả định phieuGiamgiaService đã có method này để trừ số lượng sử dụng
        phieuGiamgiaService.decrementVoucher(pgg);
    }

    // Map từ CheckoutRequest.CartItem sang CreateOrderRequest.SanPhamItem (Dùng cho hàm decrementInventory)
    private List<CreateOrderRequest.SanPhamItem> mapToSanPhamItems(List<CheckoutRequest.CartItem> cartItems) {
        List<CreateOrderRequest.SanPhamItem> list = new ArrayList<>();
        for (CheckoutRequest.CartItem c : cartItems) {
            // DonGia được set là BigDecimal.ZERO vì không cần thiết cho việc trừ kho
            list.add(new CreateOrderRequest.SanPhamItem(c.getSanPhamChiTietId(), c.getSoLuong(), BigDecimal.ZERO));
        }
        return list;
    }
}