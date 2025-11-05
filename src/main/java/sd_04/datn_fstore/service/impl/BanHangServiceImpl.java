package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.dto.CartItemDto;
import sd_04.datn_fstore.dto.CreateOrderRequest;
import sd_04.datn_fstore.dto.PaymentNotificationDto;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.model.HoaDonChiTiet;
import sd_04.datn_fstore.model.KhachHang;
import sd_04.datn_fstore.model.SanPhamChiTiet;
import sd_04.datn_fstore.repository.HoaDonChiTietRepository;
import sd_04.datn_fstore.repository.HoaDonRepository;
import sd_04.datn_fstore.repository.KhachHangRepo;
import sd_04.datn_fstore.repository.SanPhamCTRepository;
import sd_04.datn_fstore.service.BanHangService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service // <-- Đánh dấu đây là "Nhà bếp" (File chức năng)
@RequiredArgsConstructor
public class BanHangServiceImpl implements BanHangService { // <-- "implements" Interface

    // (Inject các Repository...)
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final SanPhamCTRepository sanPhamCTRepository;
    private final KhachHangRepo khachHangRepo;

    @Transactional
    @Override
    public HoaDon createPosPayment(CreateOrderRequest request) {
        // ========== TOÀN BỘ CODE CHỨC NĂNG CỦA BẠN NẰM ĐÂY ==========
        // 1. Tìm khách hàng (nếu có)
        KhachHang khachHang = null;
        if (request.getCustomerId() != null) {
            khachHang = khachHangRepo.findById(request.getCustomerId()).orElse(null);
        }

        // 2. Tạo Hóa Đơn
        HoaDon hoaDon = new HoaDon();
        hoaDon.setKhachHang(khachHang);
        hoaDon.setTongTien(request.getTotalAmount());
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setHinhThucBanHang(1); // 1 = Bán tại quầy (POS)
        hoaDon.setTrangThai(1); // 1 = Đã thanh toán

        // 3. Chuyển đổi phương thức thanh toán
        hoaDon.setHinhThucThanhToan(mapPaymentMethod(request.getPaymentMethod()));

        // 4. Lưu hóa đơn lần 1 để lấy ID
        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

        // 5. Tạo Mã Hóa Đơn (ví dụ: HD1, HD2) và lưu lại
        savedHoaDon.setMaHoaDon("HD" + savedHoaDon.getId());
        hoaDonRepository.save(savedHoaDon);

        List<HoaDonChiTiet> chiTietList = new ArrayList<>();

        // 6. Lặp qua các sản phẩm trong giỏ hàng
        for (CartItemDto itemDto : request.getItems()) {
            // 7. Tìm sản phẩm
            SanPhamChiTiet spct = sanPhamCTRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + itemDto.getProductId()));

            // 8. KIỂM TRA & TRỪ KHO
            if (spct.getSoLuong() < itemDto.getQuantity()) {
                String tenSP = spct.getSanPham() != null ? spct.getSanPham().getTenSanPham() : "Không rõ";
                throw new RuntimeException("Sản phẩm " + tenSP + " (ID: "+ spct.getId() +") không đủ số lượng.");
            }
            spct.setSoLuong(spct.getSoLuong() - itemDto.getQuantity());
            sanPhamCTRepository.save(spct);

            // 9. Tạo Hóa Đơn Chi Tiết
            HoaDonChiTiet chiTiet = new HoaDonChiTiet();
            chiTiet.setHoaDon(savedHoaDon);
            chiTiet.setSanPhamChiTiet(spct);
            chiTiet.setSoLuong(itemDto.getQuantity());
            chiTiet.setDonGia(itemDto.getPrice());
            chiTiet.setThanhTien(itemDto.getPrice().multiply(new BigDecimal(itemDto.getQuantity())));

            chiTietList.add(chiTiet);
        }

        // 10. Lưu tất cả chi tiết
        hoaDonChiTietRepository.saveAll(chiTietList);
        // ==========================================================

        System.out.println("Đã thực hiện xong chức năng THANH TOÁN cho Hóa đơn: " + savedHoaDon.getMaHoaDon());
        return savedHoaDon;
    }

    @Transactional
    @Override
    public HoaDon saveDraftOrder(CreateOrderRequest request) {
        // ========== TOÀN BỘ CODE CHỨC NĂNG CỦA BẠN NẰM ĐÂY ==========
        // 1. Tìm khách hàng
        KhachHang khachHang = null;
        if (request.getCustomerId() != null) {
            khachHang = khachHangRepo.findById(request.getCustomerId()).orElse(null);
        }

        // 2. Tạo Hóa Đơn
        HoaDon hoaDon = new HoaDon();
        hoaDon.setKhachHang(khachHang);
        hoaDon.setTongTien(request.getTotalAmount());
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setHinhThucBanHang(1); // 1 = Bán tại quầy
        hoaDon.setTrangThai(0); // 0 = Nháp (Lưu tạm)

        // 3. Lưu hóa đơn lần 1 để lấy ID
        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

        // 4. Tạo Mã Hóa Đơn và lưu lại
        savedHoaDon.setMaHoaDon("HD" + savedHoaDon.getId());
        hoaDonRepository.save(savedHoaDon);

        List<HoaDonChiTiet> chiTietList = new ArrayList<>();

        // 5. Lặp qua giỏ hàng
        for (CartItemDto itemDto : request.getItems()) {
            SanPhamChiTiet spct = sanPhamCTRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + itemDto.getProductId()));

            // !! KHÔNG KIỂM TRA KHO, KHÔNG TRỪ KHO KHI LƯU TẠM !!

            // 6. Tạo Hóa Đơn Chi Tiết
            HoaDonChiTiet chiTiet = new HoaDonChiTiet();
            chiTiet.setHoaDon(savedHoaDon);
            chiTiet.setSanPhamChiTiet(spct);
            chiTiet.setSoLuong(itemDto.getQuantity());
            chiTiet.setDonGia(itemDto.getPrice());
            chiTiet.setThanhTien(itemDto.getPrice().multiply(new BigDecimal(itemDto.getQuantity())));

            chiTietList.add(chiTiet);
        }

        // 7. Lưu tất cả chi tiết
        hoaDonChiTietRepository.saveAll(chiTietList);
        // ==========================================================

        System.out.println("Đã thực hiện xong chức năng LƯU TẠM cho Hóa đơn: " + savedHoaDon.getMaHoaDon());
        return savedHoaDon;
    }

    @Override
    public HoaDon createPosPayment(Map<String, Object> requestBody) {
        return null;
    }

    @Override
    public HoaDon saveDraftOrder(Map<String, Object> requestBody) {
        return null;
    }

    @Override // <-- "Thực hiện" món ăn 3
    @Transactional
    public void confirmPaymentByOrderCode(PaymentNotificationDto paymentData) {
        // ========== TOÀN BỘ CODE CHỨC NĂNG CỦA BẠN NẰM ĐÂY ==========
        // 1. Kiểm tra giao dịch có thành công không
        if (!paymentData.isSuccess()) {
            System.out.println("Webhook: Giao dịch thất bại, bỏ qua.");
            return;
        }

        // 2. Lấy mã hóa đơn từ nội dung chuyển khoản
        String orderCode = paymentData.getDescription();

        // 3. Tìm hóa đơn trong DB
        HoaDon hoaDon = hoaDonRepository.findByMaHoaDon(orderCode)
                .orElseThrow(() -> new RuntimeException("Webhook: Không tìm thấy hóa đơn với mã: " + orderCode));

        // 4. Kiểm tra: Chỉ cập nhật nếu hóa đơn đang ở trạng thái "Nháp" (0)
        if (hoaDon.getTrangThai() == 0) {

            // 5. KIỂM TRA SỐ TIỀN (QUAN TRỌNG)
            BigDecimal paidAmount = new BigDecimal(paymentData.getAmount());
            if (hoaDon.getTongTien().compareTo(paidAmount) != 0) {
                System.err.println("Webhook LỖI: Mã " + orderCode + " - Số tiền không khớp. Yêu cầu: " + hoaDon.getTongTien() + ", Nhận được: " + paidAmount);
                throw new RuntimeException("Số tiền thanh toán không khớp");
            }

            // 6. CẬP NHẬT TRẠNG THÁI HÓA ĐƠN
            hoaDon.setTrangThai(1); // 1 = Đã thanh toán
            hoaDon.setNgayTao(LocalDateTime.now()); // Cập nhật lại ngày (thành ngày thanh toán)
            hoaDon.setHinhThucThanhToan(3); // 3 = Thẻ/QR

            hoaDonRepository.save(hoaDon);

            // 7. TRỪ KHO (Vì đơn nháp chưa trừ kho)
            List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByHoaDonId(hoaDon.getId());
            for (HoaDonChiTiet chiTiet : chiTietList) {
                SanPhamChiTiet spct = chiTiet.getSanPhamChiTiet();

                // Kiểm tra kho
                if (spct.getSoLuong() < chiTiet.getSoLuong()) {
                    String tenSP = spct.getSanPham() != null ? spct.getSanPham().getTenSanPham() : "Không rõ";
                    throw new RuntimeException("Lỗi thanh toán QR: Sản phẩm " + tenSP + " đã hết hàng trong lúc bạn thanh toán.");
                }

                // Trừ kho
                spct.setSoLuong(spct.getSoLuong() - chiTiet.getSoLuong());
                sanPhamCTRepository.save(spct);
            }

            System.out.println("Webhook: Đã xác nhận thanh toán và trừ kho cho hóa đơn " + orderCode);

        } else {
            // Nếu hóa đơn đã được thanh toán (trangThai=1)
            System.out.println("Webhook: Hóa đơn " + orderCode + " đã ở trạng thái cuối, bỏ qua (tránh xử lý trùng).");
        }
        // ==========================================================
    }

    // Hàm tiện ích để chuyển đổi String PTTT sang Integer
    private Integer mapPaymentMethod(String method) {
        if ("cash".equals(method)) {
            return 1; // 1 = Tiền mặt
        } else if ("transfer".equals(method)) {
            return 2; // 2 = Chuyển khoản
        } else if ("qr".equals(method)) {
            return 3; // 3 = Thẻ/QR
        }
        return 1; // Mặc định là Tiền mặt
    }
}