package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.dto.*;
import sd_04.datn_fstore.model.*;
import sd_04.datn_fstore.repository.*;
import sd_04.datn_fstore.service.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutServiceImpl implements CheckoutService {

    private final VnPayService vnPayService;
    private final PhieuGiamGiaRepo phieuGiamGiaRepository;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final SanPhamCTRepository sanPhamCTRepository;
    private final SanPhamService sanPhamService;
    private final DiaChiRepo diaChiRepo;
    private final ThongBaoService thongBaoService;
    private final PhieuGiamgiaService phieuGiamgiaService;
    private final KhachHangRepo khachHangRepository;
    private final GioHangRepository gioHangRepository;

    // =========================================================================
    // 1. TÍNH TOÁN TỔNG TIỀN
    // =========================================================================
    @Override
    public CalculateTotalResponse calculateOrderTotal(CalculateTotalRequest request) {
        BigDecimal subTotal = BigDecimal.ZERO;
        if (request.getItems() != null) {
            for (CalculateTotalRequest.CartItem item : request.getItems()) {
                Optional<SanPhamChiTiet> spctOpt = sanPhamCTRepository.findById(item.getSanPhamChiTietId());
                if (spctOpt.isEmpty()) continue;

                SanPhamChiTiet spct = spctOpt.get();
                BigDecimal realPrice = spct.getGiaTien() != null ? spct.getGiaTien() : BigDecimal.ZERO;
                subTotal = subTotal.add(realPrice.multiply(BigDecimal.valueOf(item.getSoLuong())));
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
                LocalDateTime now = LocalDateTime.now();

                if (now.isBefore(pgg.getNgayBatDau())) {
                    voucherMessage = "Mã giảm giá chưa đến ngày bắt đầu sử dụng!";
                } else if (now.isAfter(pgg.getNgayKetThuc())) {
                    voucherMessage = "Mã giảm giá đã hết hạn sử dụng!";
                } else if (pgg.getTrangThai() != 0) {
                    voucherMessage = "Mã giảm giá này đã bị dừng/hủy!";
                } else if (pgg.getSoLuong() != null && pgg.getSoLuong() <= 0) {
                    voucherMessage = "Mã giảm giá đã hết lượt sử dụng!";
                } else if (pgg.getDieuKienGiamGia() != null && subTotal.compareTo(pgg.getDieuKienGiamGia()) < 0) {
                    voucherMessage = "Đơn hàng chưa đạt tối thiểu " + String.format("%,.0f", pgg.getDieuKienGiamGia()) + "đ";
                } else {
                    BigDecimal giaTriGiam = pgg.getGiaTriGiam() == null ? BigDecimal.ZERO : pgg.getGiaTriGiam();
                    BigDecimal giamToiDa = pgg.getSoTienGiam();

                    if (pgg.getHinhThucGiam() == 2) { // Giảm %
                        if (giaTriGiam.compareTo(new BigDecimal(100)) > 0) giaTriGiam = new BigDecimal(100);
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
    // 2. XỬ LÝ ĐẶT HÀNG (SỬA LẠI LOGIC TRỪ KHO & VNPAY)
    // =========================================================================
    @Transactional
    public CheckoutResponse placeOrder(CheckoutRequest req, String clientIp) {

        // 1. Validate & Địa chỉ
        if (req.getAddressId() == null) throw new RuntimeException("Vui lòng chọn địa chỉ giao hàng.");
        DiaChi selectedDiaChi = diaChiRepo.findById(req.getAddressId())
                .orElseThrow(() -> new RuntimeException("Địa chỉ giao hàng không tồn tại."));

        // 2. Khởi tạo Hóa đơn
        HoaDon hoaDon = new HoaDon();
        String timePart = String.valueOf(System.currentTimeMillis());
        String randomPart = String.valueOf((int)(Math.random() * 900) + 100);
        String maHoaDon = "OL" + timePart.substring(timePart.length() - 8) + randomPart;
        hoaDon.setMaHoaDon(maHoaDon);
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setHinhThucBanHang(0); // 0: Online
        hoaDon.setMoTa(req.getNote());

        if (req.getKhachHangId() != null) {
            khachHangRepository.findById(req.getKhachHangId()).ifPresent(hoaDon::setKhachHang);
        }

        // 3. Snapshot Địa chỉ
        DiaChi diaChiGiaoHangMoi = new DiaChi();
        // Copy các trường từ selectedDiaChi sang diaChiGiaoHangMoi...
        diaChiGiaoHangMoi.setHoTen(selectedDiaChi.getHoTen());
        diaChiGiaoHangMoi.setSoDienThoai(selectedDiaChi.getSoDienThoai());
        diaChiGiaoHangMoi.setDiaChiCuThe(selectedDiaChi.getDiaChiCuThe());
        diaChiGiaoHangMoi.setXa(selectedDiaChi.getXa());
        diaChiGiaoHangMoi.setHuyen(selectedDiaChi.getHuyen());
        diaChiGiaoHangMoi.setThanhPho(selectedDiaChi.getThanhPho());
        diaChiGiaoHangMoi.setLoaiDiaChi(selectedDiaChi.getLoaiDiaChi());
        diaChiGiaoHangMoi.setTrangThai(1);
        DiaChi savedDiaChiGiaoHang = diaChiRepo.save(diaChiGiaoHangMoi);
        hoaDon.setDiaChiGiaoHang(savedDiaChiGiaoHang);

        // 4. Xử lý sản phẩm & Tính SubTotal
        if (req.getItems() == null || req.getItems().isEmpty()) throw new RuntimeException("Giỏ hàng trống!");
        BigDecimal subTotal = BigDecimal.ZERO;
        List<HoaDonChiTiet> chiTietList = new ArrayList<>();

        for (CheckoutRequest.CartItem itemDTO : req.getItems()) {
            SanPhamChiTiet spct = sanPhamCTRepository.findById(itemDTO.getSanPhamChiTietId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));
            // Check tồn kho tại đây
            if (spct.getSoLuong() < itemDTO.getSoLuong()) {
                throw new RuntimeException("Sản phẩm '" + spct.getSanPham().getTenSanPham() + "' không đủ hàng!");
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

        // 5. Voucher
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

        // 6. Tổng tiền Final
        BigDecimal shippingFee = req.getShippingFee() != null ? req.getShippingFee() : BigDecimal.ZERO;
        BigDecimal finalTotal = subTotal.subtract(discountAmount).add(shippingFee);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) finalTotal = BigDecimal.ZERO;

        hoaDon.setTongTien(subTotal);
        hoaDon.setTienGiamGia(discountAmount);
        hoaDon.setPhiVanChuyen(shippingFee);
        hoaDon.setTongTienSauGiam(finalTotal);

        // =====================================================================
        // [QUAN TRỌNG] TRỪ TỒN KHO VÀ VOUCHER NGAY LẬP TỨC (GIỮ HÀNG)
        // =====================================================================
        decrementInventory(mapToSanPhamItems(req.getItems()));
        if (voucherToUse != null) decrementVoucher(voucherToUse);

        // Xóa giỏ hàng (nếu không phải mua ngay)
        boolean isBuyNow = (req.getIsBuyNow() != null && req.getIsBuyNow());
        if (!isBuyNow && req.getKhachHangId() != null) {
            try {
                for (CheckoutRequest.CartItem item : req.getItems()) {
                    gioHangRepository.deleteFromCart(req.getKhachHangId(), item.getSanPhamChiTietId());
                }
            } catch (Exception e) { log.error("Lỗi xóa giỏ hàng: {}", e.getMessage()); }
        }

        // 7. PHÂN LUỒNG THANH TOÁN
        String redirectUrl = "";

        if ("VNPAY".equals(req.getPaymentMethod())) {
            // --- VNPAY ---
            hoaDon.setTrangThai(6); // 6: Chờ thanh toán (Khớp với HTML badge)
            hoaDon.setHinhThucThanhToan(4);

            HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
            hoaDonChiTietRepository.saveAll(chiTietList);

            try {
                long amountInCents = finalTotal.multiply(new BigDecimal(100)).longValue();
                redirectUrl = vnPayService.createOrder(amountInCents, "Thanh toan don " + maHoaDon, maHoaDon, clientIp);
                return new CheckoutResponse(true, "Chuyển hướng VNPay", redirectUrl);
            } catch (Exception e) {
                // NẾU TẠO URL LỖI -> HOÀN LẠI KHO NGAY
                log.error("Lỗi tạo VNPAY URL: ", e);
                cancelOrder(maHoaDon);
                throw new RuntimeException("Lỗi hệ thống thanh toán: " + e.getMessage());
            }

        } else {
            // --- COD ---
            hoaDon.setTrangThai(0); // 0: Chờ xác nhận
            hoaDon.setHinhThucThanhToan(1); // 1: COD/Tiền mặt

            HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
            hoaDonChiTietRepository.saveAll(chiTietList);

            // Gửi thông báo
            String tenKhach = (savedHoaDon.getKhachHang() != null) ? savedHoaDon.getKhachHang().getTenKhachHang() : savedDiaChiGiaoHang.getHoTen();
            thongBaoService.createNotification("Đơn hàng mới #" + maHoaDon, "Khách " + tenKhach + " đặt hàng COD.", "ORDER", "/admin/hoa-don/detail/" + savedHoaDon.getId());

            redirectUrl = "/checkout/success?id=" + savedHoaDon.getId();
            return new CheckoutResponse(true, "Đặt hàng thành công", redirectUrl);
        }
    }

    // =========================================================================
    // 3. XỬ LÝ KẾT QUẢ VNPAY TRẢ VỀ (LOGIC CÒN THIẾU CỦA BẠN)
    // =========================================================================
    @Transactional
    public void processPaymentResult(String maHoaDon, boolean isSuccess) {
        HoaDon hoaDon = hoaDonRepository.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn: " + maHoaDon));

        // Mở rộng điều kiện: Chấp nhận cả trạng thái 0 và 6
        if (hoaDon.getTrangThai() == 6 || hoaDon.getTrangThai() == 0) {
            if (isSuccess) {
                hoaDon.setTrangThai(1); // 2: Đã thanh toán / Đang chuẩn bị
                hoaDon.setHinhThucThanhToan(4); // VNPAY
                hoaDon.setNgayTao(LocalDateTime.now());
                hoaDonRepository.save(hoaDon);

                // Gửi thông báo
                thongBaoService.createNotification("Thanh toán thành công #" + maHoaDon,
                        "Đơn hàng " + maHoaDon + " đã thanh toán qua VNPAY.", "ORDER", "/admin/hoa-don/detail/" + hoaDon.getId());
            } else {
                log.info("VNPAY Failed for Order: {}", maHoaDon);
                cancelOrder(maHoaDon);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String maHoaDon) {
        HoaDon hoaDon = hoaDonRepository.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại!"));

        // Nếu đơn đã hủy rồi thì không làm gì thêm để tránh cộng dồn sai
        if (hoaDon.getTrangThai() == 5) return;

        // 1. Cập nhật trạng thái Hóa đơn
        hoaDon.setTrangThai(5); // 5: Đã Hủy
        hoaDonRepository.save(hoaDon);

        // 2. Hoàn kho Chi tiết & Tổng cha
        Set<Integer> listIdCha = new HashSet<>(); // Dùng Set để tránh trùng lặp ID cha

        List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByHoaDon(hoaDon);
        for (HoaDonChiTiet cthd : chiTietList) {
            SanPhamChiTiet spct = cthd.getSanPhamChiTiet();

            // Cộng lại số lượng chi tiết
            spct.setSoLuong(spct.getSoLuong() + cthd.getSoLuong());

            // Nếu sản phẩm con đang bị ẩn (hết hàng) -> Mở bán lại (Trạng thái 1)
            if (spct.getTrangThai() == 0 && spct.getSoLuong() > 0) {
                spct.setTrangThai(1);
            }

            // Lưu ngay xuống DB (Flush) để đảm bảo dữ liệu mới nhất
            sanPhamCTRepository.saveAndFlush(spct);

            // Thêm ID cha vào danh sách cần cập nhật
            listIdCha.add(spct.getSanPham().getId());
        }

        // 3. --- BƯỚC QUAN TRỌNG: CẬP NHẬT LẠI SỐ LƯỢNG TỔNG CỦA CHA ---
        // Đây là bước VNPay đang thiếu, khiến số lượng hiển thị bên ngoài bị sai
        for (Integer idCha : listIdCha) {
            sanPhamService.updateTotalQuantity(idCha);
        }

        // 4. Hoàn lại voucher (nếu có dùng)
        if (hoaDon.getPhieuGiamGia() != null) {
            phieuGiamgiaService.incrementVoucher(hoaDon.getPhieuGiamGia());
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
            sanPhamService.updateTotalQuantity(spct.getSanPham().getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementVoucher(PhieuGiamGia pgg) {
        phieuGiamgiaService.decrementVoucher(pgg);
    }

    // Helper map function
    private List<CreateOrderRequest.SanPhamItem> mapToSanPhamItems(List<CheckoutRequest.CartItem> cartItems) {
        List<CreateOrderRequest.SanPhamItem> list = new ArrayList<>();
        for (CheckoutRequest.CartItem c : cartItems) {
            list.add(new CreateOrderRequest.SanPhamItem(c.getSanPhamChiTietId(), c.getSoLuong(), BigDecimal.ZERO));
        }
        return list;
    }

    // Giữ lại method cũ nếu cần, nhưng chuyển hướng gọi sang placeOrder
    @Override
    public VnPayResponseDTO taoThanhToanVnPay(CheckoutRequest request, String ipAddress) {
        request.setPaymentMethod("VNPAY");
        CheckoutResponse response = placeOrder(request, ipAddress);
        if (response.isSuccess() && response.getRedirectUrl() != null) {
            return new VnPayResponseDTO(true, response.getMessage(), response.getRedirectUrl());
        } else {
            throw new RuntimeException("Lỗi tạo VNPAY: " + response.getMessage());
        }
    }
}