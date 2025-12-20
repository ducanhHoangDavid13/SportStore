package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CheckoutServiceImpl implements CheckoutService {

    private final VnPayService vnPayService;
    private final PhieuGiamGiaRepo phieuGiamGiaRepository;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final SanPhamCTRepository sanPhamCTRepository;
    private final DiaChiRepo diaChiRepo;
    private final ThongBaoService thongBaoService;
    private final PhieuGiamgiaService phieuGiamgiaService;

    // BỔ SUNG: KhachHangRepo để tìm khách hàng bằng ID
    private final KhachHangRepo khachHangRepository;
    private final GioHangRepository gioHangRepository;

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
                } else if (now.isAfter(pgg.getNgayKetThuc())) {
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
                } else {
                    // --- ĐỦ ĐIỀU KIỆN ---
                    BigDecimal giaTriGiam = pgg.getGiaTriGiam() == null ? BigDecimal.ZERO : pgg.getGiaTriGiam();
                    BigDecimal giamToiDa = pgg.getSoTienGiam();

                    if (pgg.getHinhThucGiam() == 2) { // Giảm %
                        if (giaTriGiam.compareTo(new BigDecimal(100)) > 0) giaTriGiam = new BigDecimal(100);
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
    @Transactional
    public CheckoutResponse placeOrder(CheckoutRequest req, String clientIp) {

        // 1. KIỂM TRA ĐỊA CHỈ
        if (req.getAddressId() == null) {
            throw new RuntimeException("Vui lòng chọn địa chỉ giao hàng.");
        }

        DiaChi selectedDiaChi = diaChiRepo.findById(req.getAddressId())
                .orElseThrow(() -> new RuntimeException("Địa chỉ giao hàng không tồn tại."));

        // 2. TẠO THỰC THỂ HÓA ĐƠN
        HoaDon hoaDon = new HoaDon();
        String maHoaDon = "HD" + System.currentTimeMillis();
        hoaDon.setMaHoaDon(maHoaDon);
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setHinhThucBanHang(0); // 0: Online
        hoaDon.setMoTa(req.getNote());

        // Gán Khách hàng (nếu có đăng nhập)
        if (req.getKhachHangId() != null) {
            khachHangRepository.findById(req.getKhachHangId()).ifPresent(hoaDon::setKhachHang);
        }

        // 3. LƯU BẢN SAO ĐỊA CHỈ TẠI THỜI ĐIỂM ĐẶT
        // Điều này quan trọng để giữ thông tin lịch sử ngay cả khi khách hàng xóa địa chỉ trong hồ sơ
        DiaChi diaChiGiaoHangMoi = new DiaChi();
        diaChiGiaoHangMoi.setHoTen(selectedDiaChi.getHoTen());
        diaChiGiaoHangMoi.setSoDienThoai(selectedDiaChi.getSoDienThoai());
        diaChiGiaoHangMoi.setDiaChiCuThe(selectedDiaChi.getDiaChiCuThe());
        diaChiGiaoHangMoi.setXa(selectedDiaChi.getXa());
        diaChiGiaoHangMoi.setHuyen(selectedDiaChi.getHuyen());
        diaChiGiaoHangMoi.setThanhPho(selectedDiaChi.getThanhPho());
        diaChiGiaoHangMoi.setLoaiDiaChi(selectedDiaChi.getLoaiDiaChi());
        diaChiGiaoHangMoi.setTrangThai(1); // 1: Active cho đơn hàng này

        DiaChi savedDiaChiGiaoHang = diaChiRepo.save(diaChiGiaoHangMoi);
        hoaDon.setDiaChiGiaoHang(savedDiaChiGiaoHang);

        // 4. XỬ LÝ DANH SÁCH SẢN PHẨM
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new RuntimeException("Danh sách sản phẩm trống!");
        }

        BigDecimal subTotal = BigDecimal.ZERO;
        List<HoaDonChiTiet> chiTietList = new ArrayList<>();

        for (CheckoutRequest.CartItem itemDTO : req.getItems()) {
            SanPhamChiTiet spct = sanPhamCTRepository.findById(itemDTO.getSanPhamChiTietId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

            // Kiểm tra tồn kho thực tế
            if (spct.getSoLuong() < itemDTO.getSoLuong()) {
                throw new RuntimeException("Sản phẩm '" + spct.getSanPham().getTenSanPham() + "' không đủ hàng (Còn: " + spct.getSoLuong() + ")");
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

        // 5. TÍNH TOÁN VOUCHER
        BigDecimal discountAmount = BigDecimal.ZERO;
        PhieuGiamGia voucherToUse = null;
        if (req.getVoucherCode() != null && !req.getVoucherCode().trim().isEmpty()) {
            CalculateTotalRequest calcReq = new CalculateTotalRequest();
            calcReq.setVoucherCode(req.getVoucherCode());
            calcReq.setShippingFee(req.getShippingFee());
            calcReq.setItems(req.getItems().stream().map(i -> {
                CalculateTotalRequest.CartItem ci = new CalculateTotalRequest.CartItem();
                ci.setSanPhamChiTietId(i.getSanPhamChiTietId());
                ci.setSoLuong(i.getSoLuong());
                return ci;
            }).collect(java.util.stream.Collectors.toList()));

            CalculateTotalResponse calcRes = calculateOrderTotal(calcReq);
            if (calcRes.isVoucherValid()) {
                discountAmount = calcRes.getDiscountAmount();
                voucherToUse = phieuGiamGiaRepository.findByMaPhieuGiamGia(req.getVoucherCode()).orElse(null);
                hoaDon.setPhieuGiamGia(voucherToUse);
            }
        }

        // 6. CẬP NHẬT TỔNG TIỀN ĐƠN HÀNG
        BigDecimal shippingFee = req.getShippingFee() != null ? req.getShippingFee() : BigDecimal.ZERO;
        BigDecimal finalTotal = subTotal.subtract(discountAmount).add(shippingFee);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) finalTotal = BigDecimal.ZERO;

        hoaDon.setTongTien(subTotal);
        hoaDon.setTienGiamGia(discountAmount);
        hoaDon.setPhiVanChuyen(shippingFee);
        hoaDon.setTongTienSauGiam(finalTotal);

        // 7. PHÂN LUỒNG PHƯƠNG THỨC THANH TOÁN
        String redirectUrl = "";
        if ("VNPAY".equals(req.getPaymentMethod())) {
            // --- LUỒNG VNPAY ---
            hoaDon.setTrangThai(1); // 1: Chờ thanh toán
            hoaDon.setHinhThucThanhToan(4); // 4: VNPAY
            HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
            hoaDonChiTietRepository.saveAll(chiTietList);

            try {
                long amountInCents = finalTotal.multiply(new BigDecimal(100)).longValue();
                redirectUrl = vnPayService.createOrder(amountInCents, "Thanh toan đơn " + maHoaDon, maHoaDon, clientIp);
                return new CheckoutResponse(true, "Chuyển hướng VNPay", redirectUrl);
            } catch (Exception e) {
                throw new RuntimeException("Lỗi khởi tạo thanh toán VNPay: " + e.getMessage());
            }

        } else {
            // --- LUỒNG COD (THANH TOÁN KHI NHẬN HÀNG) ---
            hoaDon.setTrangThai(0); // 0: Chờ xác nhận
            hoaDon.setHinhThucThanhToan(1); // 1: COD
            HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
            hoaDonChiTietRepository.saveAll(chiTietList);

            // A. Trừ tồn kho & voucher ngay lập tức (vì là COD đã xác nhận đơn)
            decrementInventory(mapToSanPhamItems(req.getItems()));
            if (voucherToUse != null) decrementVoucher(voucherToUse);

            // B. Dọn dẹp giỏ hàng (Chỉ khi không phải Mua Ngay)
            boolean isBuyNow = (req.getIsBuyNow() != null && req.getIsBuyNow());
            if (!isBuyNow && req.getKhachHangId() != null) {
                try {
                    for (CheckoutRequest.CartItem item : req.getItems()) {
                        gioHangRepository.deleteFromCart(req.getKhachHangId(), item.getSanPhamChiTietId());
                    }
                    log.info("Dọn dẹp giỏ hàng thành công cho khách hàng: {}", req.getKhachHangId());
                } catch (Exception e) {
                    log.error("Lỗi dọn dẹp giỏ hàng: {}", e.getMessage());
                    // Không throw exception ở đây để tránh rollback đơn hàng chỉ vì lỗi giỏ hàng
                }
            }

            // C. Gửi thông báo hệ thống/Admin
            String tenKhach = (savedHoaDon.getKhachHang() != null) ? savedHoaDon.getKhachHang().getTenKhachHang() : savedDiaChiGiaoHang.getHoTen();
            thongBaoService.createNotification(
                    "Đơn hàng mới #" + maHoaDon,
                    "Khách hàng " + tenKhach + " vừa đặt đơn hàng trị giá " + String.format("%,.0f", finalTotal) + "đ",
                    "ORDER",
                    "/admin/hoa-don/detail/" + savedHoaDon.getId()
            );

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
            if (newStock < 0)
                throw new RuntimeException("Sản phẩm " + spct.getSanPham().getTenSanPham() + " hết hàng!");

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

    // Đặt đoạn code này vào CheckoutServiceImpl.java

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String maHoaDon) {
        // 1. Tìm Hóa Đơn bằng Mã
        HoaDon hoaDon = hoaDonRepository.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Hóa đơn với mã " + maHoaDon + " không tồn tại!"));

        // 2. Kiểm tra trạng thái hiện tại (Giả định: Chỉ được hủy nếu chưa hoàn thành hoặc đã hủy)
        // Tùy theo nghiệp vụ: 0: Chờ xác nhận, 1: Chờ thanh toán, 2: Đã xác nhận/Chuẩn bị hàng
        final int TRANG_THAI_HUY_THANH_CONG = 5; // Ví dụ: 5 là trạng thái "Đã Hủy"

        if (hoaDon.getTrangThai() == TRANG_THAI_HUY_THANH_CONG) {
            // Đơn đã bị hủy, không cần xử lý thêm
            return;
        }

        // Nếu đơn đã thành công (ví dụ trạng thái 3: Đã giao/Hoàn thành), KHÔNG CHO HỦY
        // if (hoaDon.getTrangThai() == 3) {
        //     throw new RuntimeException("Không thể hủy đơn hàng đã hoàn thành!");
        // }

        // 3. Cập nhật trạng thái thành Đã Hủy
        hoaDon.setTrangThai(TRANG_THAI_HUY_THANH_CONG);
//        hoaDon.setNgayCapNhat(LocalDateTime.now());
        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

        // 4. HOÀN LẠI TỒN KHO (INCREMENT INVENTORY)
        List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByHoaDon(savedHoaDon);
        for (HoaDonChiTiet cthd : chiTietList) {
            SanPhamChiTiet spct = cthd.getSanPhamChiTiet();

            // CỘNG LẠI SỐ LƯỢNG đã trừ khi đặt hàng
            int soLuongHoanLai = cthd.getSoLuong();
            int newStock = spct.getSoLuong() + soLuongHoanLai;

            spct.setSoLuong(newStock);
            sanPhamCTRepository.save(spct);
        }

        // 5. HOÀN LẠI LƯỢT VOUCHER nếu có (INCREMENT VOUCHER USAGE COUNT)
        PhieuGiamGia pgg = savedHoaDon.getPhieuGiamGia();
        if (pgg != null) {
            // Cần đảm bảo PhieuGiamgiaService có phương thức để tăng lại số lượng (hoàn lại lượt dùng)
            incrementVoucher(pgg);
        }

        // 6. Gửi thông báo đến Admin
        String khachHangName = savedHoaDon.getKhachHang() != null ? savedHoaDon.getKhachHang().getTenKhachHang() : savedHoaDon.getDiaChiGiaoHang().getHoTen();

        thongBaoService.createNotification(
                "Đơn hàng bị hủy #" + maHoaDon,
                "Đơn hàng của khách " + khachHangName + " đã bị hủy thành công. Tồn kho đã được hoàn lại.",
                "CANCEL",
                "/admin/hoa-don/detail/" + savedHoaDon.getId()
        );
    }

    // BỔ SUNG: Bạn cần thêm phương thức này để hoàn lại lượt voucher
    @Transactional(rollbackFor = Exception.class)
    public void incrementVoucher(PhieuGiamGia pgg) {
        // Cần phải triển khai phương thức này trong PhieuGiamgiaService.
        // Ví dụ: pgg.setSoLuong(pgg.getSoLuong() + 1); phieuGiamGiaRepository.save(pgg);
        // Hoặc gọi service như dưới:
        phieuGiamgiaService.incrementVoucher(pgg);
    }

// BỔ SUNG: Bạn cần phải thêm phương thức findByMaHoaDon vào HoaDonRepository
// Optional<HoaDon> findByMaHoaDon(String maHoaDon);


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