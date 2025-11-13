package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.dto.CartItemDto;
import sd_04.datn_fstore.dto.CreateOrderRequest;
import sd_04.datn_fstore.model.*;
import sd_04.datn_fstore.repository.*;
import sd_04.datn_fstore.service.BanHangService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BanHangServiceImpl implements BanHangService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final SanPhamCTRepository sanPhamCTRepository;
    private final NhanVienRepository nhanVienRepository;
    private final KhachHangRepo khachHangRepository; // Dùng tên KhachHangRepo

    @Override
    @Transactional
    public HoaDon thanhToanTienMat(CreateOrderRequest request) {
        HoaDon hoaDon = createHoaDonFromPayload(request);

        // SỬA 1: XÓA LỖI - Model HoaDon của bạn không có 'ngayThanhToan'
        // hoaDon.setNgayThanhToan(LocalDateTime.now()); // <-- Dòng này bị xóa

        // (Bạn nên thêm trường 'ngay_thanh_toan' vào Model HoaDon và bảng SQL)

        hoaDon.setTrangThai(1); // 1 = Đã hoàn thành

        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
        List<CartItemDto> itemsList = request.getItemsList();

        for (CartItemDto item : itemsList) {
            int spctId = item.getSanPhamChiTietId();
            int soLuong = item.getSoLuong();

            SanPhamChiTiet spct = sanPhamCTRepository.findById(spctId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy SPCT ID: " + spctId));

            if (spct.getSoLuong() < soLuong) {
                throw new RuntimeException("Hết hàng: " + spct.getSanPham().getTenSanPham());
            }

            HoaDonChiTiet hdct = new HoaDonChiTiet();
            hdct.setHoaDon(savedHoaDon);
            hdct.setSanPhamChiTiet(spct);
            hdct.setSoLuong(soLuong);
            hdct.setDonGia(BigDecimal.valueOf(item.getDonGia()));
            hoaDonChiTietRepository.save(hdct);

            spct.setSoLuong(spct.getSoLuong() - soLuong);
            sanPhamCTRepository.save(spct);
        }

        return savedHoaDon;
    }

    @Override
    @Transactional
    public HoaDon luuHoaDonTam(CreateOrderRequest request) {
        HoaDon hoaDon = createHoaDonFromPayload(request);

        String pttt = request.getPaymentMethod();
        if ("TRANSFER".equals(pttt) || "QR".equals(pttt)) {
            hoaDon.setTrangThai(5); // 5 = Chờ Chuyển Khoản
        } else {
            hoaDon.setTrangThai(0); // 0 = Hóa đơn tạm
        }

        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
        List<CartItemDto> itemsList = request.getItemsList();

        for (CartItemDto item : itemsList) {
            int spctId = item.getSanPhamChiTietId();
            SanPhamChiTiet spct = sanPhamCTRepository.findById(spctId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy SPCT ID: " + spctId));

            HoaDonChiTiet hdct = new HoaDonChiTiet();
            hdct.setHoaDon(savedHoaDon);
            hdct.setSanPhamChiTiet(spct);
            hdct.setSoLuong(item.getSoLuong());
            hdct.setDonGia(BigDecimal.valueOf(item.getDonGia()));
            hoaDonChiTietRepository.save(hdct);
        }

        return savedHoaDon;
    }
    @Override
    @Transactional(readOnly = true) // Chỉ đọc
    public List<HoaDon> getDraftOrders() {
        // Lấy HĐ Tạm (0) và HĐ Chờ (5)
        List<Integer> trangThais = List.of(0, 5);

        // (Bạn phải thêm hàm findByTrangThaiIn vào HoaDonRepository)
        return hoaDonRepository.findByTrangThaiInOrderByNgayTaoDesc(trangThais);
    }

    @Override
    @Transactional(readOnly = true) // Chỉ đọc
    public HoaDon getDraftOrderDetail(Integer id) {
        // (Bạn phải thêm hàm findByIdWithDetails vào HoaDonRepository)
        // Hàm này sẽ dùng JOIN FETCH để lấy HĐ + HĐCT + SPCT + SP
        HoaDon hoaDon = hoaDonRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Hóa Đơn Tạm ID: " + id));
        return hoaDon;
    }
    // Hàm tiện ích
    private HoaDon createHoaDonFromPayload(CreateOrderRequest request) {
        HoaDon hoaDon = new HoaDon();

        hoaDon.setMaHoaDon("HD" + System.currentTimeMillis());
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setTongTien(BigDecimal.valueOf(request.getTotalAmount()));

        // SỬA 2: Dùng setTienGiamGia() cho khớp với Model
        hoaDon.setTienGiamGia(BigDecimal.valueOf(request.getDiscountAmount()));

        int nhanVienId = request.getNhanVienId();
        NhanVien nv = nhanVienRepository.findById(nhanVienId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Nhân Viên ID: " + nhanVienId));
        hoaDon.setNhanVien(nv);

        Integer khachHangId = request.getKhachHangId();
        if (khachHangId != null) {
            KhachHang kh = khachHangRepository.findById(khachHangId).orElse(null);
            hoaDon.setKhachHang(kh);
        }

        // SỬA 3: Dùng setHinhThucBanHang() cho khớp với Model
        hoaDon.setHinhThucBanHang(1); // 1 = Tại quầy

        return hoaDon;
    }
}