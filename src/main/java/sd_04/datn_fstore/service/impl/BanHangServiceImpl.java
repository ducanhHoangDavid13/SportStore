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
        PhieuGiamGia pgg = getPhieuGiamGiaFromRequest(request);

        HoaDon hoaDon = createHoaDonFromPayload(request, pgg);
        hoaDon.setTrangThai(1); // 1 = Đã thanh toán / Chờ giao

        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

        // SỬA: getItemsList() -> getDanhSachSanPham()
        List<CreateOrderRequest.SanPhamItem> itemsList = request.getDanhSachSanPham();

        decrementInventory(itemsList);
        decrementVoucher(pgg);

        for (CreateOrderRequest.SanPhamItem item : itemsList) {
            saveHoaDonChiTiet(savedHoaDon, item);
        }

        String title = "Đơn hàng tại quầy";
        String content = "Mã hóa đơn #" + savedHoaDon.getMaHoaDon() + " đã thanh toán thành công.";
        String url = "/admin/hoa-don/detail/" + savedHoaDon.getId();
        thongBaoService.createNotification(title, content, "ORDER", url);

        return savedHoaDon;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HoaDon luuHoaDonTam(CreateOrderRequest request) {
        PhieuGiamGia pgg = getPhieuGiamGiaFromRequest(request);
        HoaDon hoaDon = createHoaDonFromPayload(request, pgg);

        // SỬA: getPaymentMethod() -> getPhuongThucThanhToan()
        String pttt = request.getPhuongThucThanhToan();

        if ("VNPAY".equals(pttt)) {
            hoaDon.setTrangThai(5); // Chờ VNPAY
        } else if ("TRANSFER".equals(pttt) || "QR".equals(pttt)) {
            hoaDon.setTrangThai(5); // Chờ Chuyển khoản
        } else {
            hoaDon.setTrangThai(0); // Lưu tạm
        }

        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

        // SỬA: getDanhSachSanPham()
        List<CreateOrderRequest.SanPhamItem> itemsList = request.getDanhSachSanPham();

        for (CreateOrderRequest.SanPhamItem item : itemsList) {
            saveHoaDonChiTiet(savedHoaDon, item);
        }

        return savedHoaDon;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HoaDon> getDraftOrders() {
        List<Integer> trangThais = List.of(0, 5);
        return hoaDonRepository.findByTrangThaiInOrderByNgayTaoDesc(trangThais);
    }

    @Override
    @Transactional(readOnly = true)
    public HoaDon getDraftOrderDetail(Integer id) {
        HoaDon hoaDon = hoaDonRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Hóa Đơn Tạm ID: " + id));
        hoaDon.getHoaDonChiTiets().size();
        return hoaDon;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VnPayResponseDTO taoThanhToanVnPay(CreateOrderRequest request, String ipAddress) {

        // SỬA: setPhuongThucThanhToan
        request.setPhuongThucThanhToan("VNPAY");
        HoaDon savedHoaDon = this.luuHoaDonTam(request);

        long amountInVNDcents = savedHoaDon.getTongTienSauGiam().multiply(new BigDecimal("100")).longValue();
        String orderCode = savedHoaDon.getMaHoaDon();
        String orderInfo = "Thanh toan don hang " + orderCode;

        try {
            String paymentUrl = vnPayService.createOrder(amountInVNDcents, orderInfo, orderCode, ipAddress);
            return new VnPayResponseDTO(true, "Tạo link VNPAY thành công", paymentUrl);

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Lỗi khi tạo link VNPAY: " + e.getMessage());
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

            if (soLuongTon < soLuongMua) {
                throw new RuntimeException("Sản phẩm '" + spct.getSanPham().getTenSanPham() + "' không đủ hàng.");
            }

            int newStock = soLuongTon - soLuongMua;
            spct.setSoLuong(newStock);
            sanPhamCTRepository.save(spct);

            // [3] GỬI THÔNG BÁO SẮP HẾT HÀNG (Low Stock Alert)
            if (newStock <= 10) {
                String spName = spct.getSanPham().getTenSanPham() + " (" + spct.getMauSac().getTenMauSac() + ")";
                thongBaoService.createNotification(
                        "Cảnh báo hết hàng",
                        "Sản phẩm " + spName + " chỉ còn " + newStock + " sản phẩm.",
                        "STOCK",
                        "/admin/san-pham/" + spct.getSanPham().getId()
                );
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementVoucher(PhieuGiamGia pgg) {
        if (pgg == null) return;
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

    // SỬA: Tham số Item -> SanPhamItem
    private void saveHoaDonChiTiet(HoaDon savedHoaDon, CreateOrderRequest.SanPhamItem item) {
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

    private HoaDon createHoaDonFromPayload(CreateOrderRequest request, PhieuGiamGia pgg) {
        HoaDon hoaDon = new HoaDon();

        // --- SỬA LỖI Ở ĐÂY ---
        // 1. Kiểm tra null: Nếu request gửi lên null thì gán mặc định là 0
        BigDecimal totalAmount = request.getTongTien() != null ? request.getTongTien() : BigDecimal.ZERO;
        BigDecimal discountAmount = request.getTienGiamGia() != null ? request.getTienGiamGia() : BigDecimal.ZERO;

        // 2. Tính toán lại: Tổng tiền hàng (Gốc) = Số tiền khách phải trả + Số tiền được giảm
        BigDecimal subtotalAmount = totalAmount.add(discountAmount);

        // Map dữ liệu
        hoaDon.setMaHoaDon(request.getMaHoaDon());
        hoaDon.setNgayTao(LocalDateTime.now(VN_ZONE));

        // setTongTien = Tổng giá trị hàng (chưa trừ giảm giá)
        hoaDon.setTongTien(subtotalAmount);
        hoaDon.setTienGiamGia(discountAmount);
        // setTongTienSauGiam = Khách phải trả
        hoaDon.setTongTienSauGiam(totalAmount);

        hoaDon.setPhieuGiamGia(pgg);

        // 3. Xử lý Nhân viên
        if (request.getNhanVienId() != null) {
            NhanVien nv = nhanVienRepository.findById(request.getNhanVienId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Nhân Viên ID: " + request.getNhanVienId()));
            hoaDon.setNhanVien(nv);
        }

        // 4. Xử lý Khách hàng (cho phép null nếu là khách lẻ)
        Integer khachHangId = request.getKhachHangId();
        if (khachHangId != null) {
            KhachHang kh = khachHangRepository.findById(khachHangId).orElse(null);
            hoaDon.setKhachHang(kh);
        }

        hoaDon.setHinhThucBanHang(1); // 1 = Tại quầy
        return hoaDon;
    }
}