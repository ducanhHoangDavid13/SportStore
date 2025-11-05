package sd_04.datn_fstore.service;

import lombok.RequiredArgsConstructor;
// import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.model.*;
import sd_04.datn_fstore.repository.KhachHangRepo;
import sd_04.datn_fstore.repository.PhieuGiamGiaRepo;
import sd_04.datn_fstore.repository.*;
import sd_04.datn_fstore.service.BanHangService;
import sd_04.datn_fstore.service.KhoService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BanHangServiceImpl implements BanHangService {

    // Inject tất cả
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final SanPhamCTRepository sanPhamCTRepository;
    private final KhachHangRepo khachHangRepo;
    private final NhanVienRepository nhanVienRepository;
    private final PhieuGiamGiaRepo phieuGiamGiaRepo;
    private final KhoService khoService;

    // Định nghĩa các trạng thái
    private static final int TT_HOA_DON_TAM = 0;
    private static final int TT_CHO_XAC_NHAN = 2; // (Theo bảng trạng thái của bạn)

    @Override
    @Transactional
    public HoaDon createPosPayment(Map<String, Object> requestBody) {
        return processOrder(requestBody, TT_CHO_XAC_NHAN, true);
    }

    @Override
    @Transactional
    public HoaDon saveDraftOrder(Map<String, Object> requestBody) {
        return processOrder(requestBody, TT_HOA_DON_TAM, false);
    }

    private HoaDon processOrder(Map<String, Object> requestBody, int trangThai, boolean truTonKho) {

        // 1. LẤY NHÂN VIÊN
        // (Tạm thời hardcode 1, bạn PHẢI thay bằng Spring Security)
        NhanVien nhanVien = nhanVienRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Nhân viên ID 1"));

        // 2. ĐỌC DỮ LIỆU "GIỎ HÀNG" TỪ MAP
        Integer khachHangId = (Integer) requestBody.get("khachHangId");
        String voucherCode = (String) requestBody.get("voucherCode");
        List<Map<String, Object>> itemsList = (List<Map<String, Object>>) requestBody.get("items");

        // 3. TÌM KHÁCH HÀNG
        KhachHang khachHang = khachHangRepo.findById(Optional.ofNullable(khachHangId).orElse(0)).orElse(null);

        // 4. KHỞI TẠO HÓA ĐƠN
        HoaDon hoaDon = new HoaDon();
        hoaDon.setNhanVien(nhanVien);
        hoaDon.setKhachHang(khachHang);
        hoaDon.setMaHoaDon("HD" + System.currentTimeMillis());
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setHinhThucBanHang(1); // 1 = Bán tại quầy (Khớp Model HoaDon)
        hoaDon.setTrangThai(trangThai);

        BigDecimal tongTienHang = BigDecimal.ZERO;
        List<HoaDonChiTiet> listHdct = new ArrayList<>();

        // 5. LẶP QUA "GIỎ HÀNG" (itemsList)
        for (Map<String, Object> itemMap : itemsList) {
            Integer spctId = (Integer) itemMap.get("id");
            Integer soLuong = (Integer) itemMap.get("quantity");

            SanPhamChiTiet spct = sanPhamCTRepository.findById(spctId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy SPCT ID: " + spctId));

            if (truTonKho) {
                khoService.truTonKho(spctId, soLuong);
            }

            BigDecimal donGia = spct.getGiaTien();
            BigDecimal thanhTien = donGia.multiply(new BigDecimal(soLuong));
            tongTienHang = tongTienHang.add(thanhTien);

            HoaDonChiTiet hdct = new HoaDonChiTiet();
            hdct.setHoaDon(hoaDon);
            hdct.setSanPhamChiTiet(spct);
            hdct.setSoLuong(soLuong);
            hdct.setDonGia(donGia); // (Khớp Model HDCT)
            hdct.setThanhTien(thanhTien); // (Khớp Model HDCT)
            listHdct.add(hdct);
        }

        // 6. LOGIC VOUCHER (Giảm tiền cố định)
        BigDecimal tienGiamGia = BigDecimal.ZERO;
        if (voucherCode != null && !voucherCode.isEmpty()) {
            Optional<PhieuGiamGia> optVoucher = phieuGiamGiaRepo.findByMaPhieuGiamGia(voucherCode);

            if (optVoucher.isPresent()) {
                PhieuGiamGia voucher = optVoucher.get();

                // (Khớp với Model PhieuGiamGia của bạn)
                if (tongTienHang.compareTo(voucher.getDieuKienGiamGia()) >= 0) {
                    tienGiamGia = voucher.getSoTienGiam(); // Lấy số tiền giảm cố định
                    hoaDon.setPhieuGiamGia(voucher); // Gán voucher vào hóa đơn

                    // (Trừ số lượng voucher)
                    // voucher.setSoLuong(voucher.getSoLuong() - 1);
                    // phieuGiamGiaRepo.save(voucher);
                }
                // (Nếu không đủ điều kiện, tienGiamGia vẫn = 0)
            }
        }

        // 7. GÁN TIỀN VÀO HÓA ĐƠN
        hoaDon.setTongTien(tongTienHang);
        hoaDon.setTienGiamGia(tienGiamGia);
        hoaDon.setTongTienSauGiam(tongTienHang.subtract(tienGiamGia));

        // 8. LƯU CSDL
        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

        for(HoaDonChiTiet hdct : listHdct) {
            hdct.setHoaDon(savedHoaDon);
        }
        hoaDonChiTietRepository.saveAll(listHdct);

        return savedHoaDon;
    }
}