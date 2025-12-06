package sd_04.datn_fstore.service;

import sd_04.datn_fstore.model.GioHang;
import sd_04.datn_fstore.model.KhachHang; // Import Model KhachHang
import sd_04.datn_fstore.model.SanPhamChiTiet; // Import Model SanPhamChiTiet
import sd_04.datn_fstore.repository.GioHangRepository;
import sd_04.datn_fstore.repository.KhachHangRepo; // Cần Import Repository này
import sd_04.datn_fstore.repository.SanPhamCTRepository; // Cần Import Repository này
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime; // Sử dụng LocalDateTime theo Model
import java.util.List;
import java.util.Optional;

@Service
public class GioHangService {

    @Autowired
    private GioHangRepository gioHangRepository;

    // 🔥 CẦN AUTOWIRED THÊM CÁC REPOSITORY NÀY
    @Autowired
    private SanPhamCTRepository sanPhamChiTietRepository;

    @Autowired
    private KhachHangRepo khachHangRepository;

    // Phương thức này cần được định nghĩa trong Repository: findByKhachHang_Id
    public List<GioHang> findByKhachHangId(Integer idKhachHang) {
        // Sử dụng KhachHang_id để truy vấn theo mối quan hệ trong Spring Data JPA
        return gioHangRepository.findByKhachHang_Id(idKhachHang);
    }

    public GioHang findById(Integer id) {
        return gioHangRepository.findById(id).orElse(null);
    }

    public GioHang save(GioHang gioHang) {
        return gioHangRepository.save(gioHang);
    }

    public void delete(Integer id) {
        gioHangRepository.deleteById(id);
    }

    // ====================================================================
    // 🔥 PHƯƠNG THỨC BỔ SUNG: Xử lý Thêm/Cập nhật sản phẩm vào giỏ hàng
    // ====================================================================

    public void themHoacCapNhat(Integer idKhachHang, Integer idSpCt, Integer soLuongMoi) {

        // 1. Tìm SPCT (SanPhamChiTiet) để lấy giá và kiểm tra tồn kho
        SanPhamChiTiet spct = sanPhamChiTietRepository.findById(idSpCt)
                .orElseThrow(() -> new RuntimeException("Sản phẩm chi tiết không tồn tại."));

        // 2. Tìm KhachHang (BẮT BUỘC)
        KhachHang khachHang = khachHangRepository.findById(idKhachHang)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại."));

        // 3. Tìm GioHang chi tiết (nếu đã có sản phẩm này trong giỏ)
        // Cần định nghĩa trong Repository: findByKhachHangAndSanPhamChiTiet
        Optional<GioHang> existingItemOpt = gioHangRepository.findByKhachHangAndSanPhamChiTiet(khachHang, spct);

        BigDecimal giaTien = spct.getGiaTien();

        if (existingItemOpt.isPresent()) {
            // TRƯỜNG HỢP 1: Cập nhật số lượng
            GioHang existingItem = existingItemOpt.get();
            int newSoLuong = existingItem.getSoLuong() + soLuongMoi;

            // Kiểm tra tồn kho (Không cho thêm vượt quá tồn kho)
            if (newSoLuong > spct.getSoLuong()) {
                throw new RuntimeException("Số lượng đặt vượt quá số lượng tồn kho: " + spct.getSoLuong());
            }

            existingItem.setSoLuong(newSoLuong);
            // Tính lại tổng tiền
            existingItem.setTongTien(giaTien.multiply(new BigDecimal(newSoLuong)));
            existingItem.setNgaySua(LocalDateTime.now());

            gioHangRepository.save(existingItem);

        } else {
            // TRƯỜNG HỢP 2: Tạo mới dòng GioHang

            // Kiểm tra tồn kho
            if (soLuongMoi > spct.getSoLuong()) {
                throw new RuntimeException("Số lượng đặt vượt quá số lượng tồn kho: " + spct.getSoLuong());
            }

            GioHang newItem = new GioHang();

            // Set các đối tượng Entity theo định nghĩa Model của bạn
            newItem.setKhachHang(khachHang);
            newItem.setSanPhamChiTiet(spct);

            newItem.setSoLuong(soLuongMoi);
            newItem.setTongTien(giaTien.multiply(new BigDecimal(soLuongMoi)));
            newItem.setNgayTao(LocalDateTime.now());
            newItem.setTrangThai(1); // Giả định trạng thái ban đầu là 1

            gioHangRepository.save(newItem);
        }
    }
}