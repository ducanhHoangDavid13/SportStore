package sd_04.datn_fstore.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.PhieuGiamGia;
import sd_04.datn_fstore.repository.PhieuGiamGiaRepo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PhieuGiamgiaService {

    private final PhieuGiamGiaRepo phieuGiamGiaRepository;
    private final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    // ==================== 1. TÌM KIẾM & HIỂN THỊ ====================

    public Page<PhieuGiamGia> searchAndFilter(Integer trangThai, String keyword, LocalDateTime ngayBatDau, LocalDateTime ngayKetThuc, int page, int size, String sortField, String sortDir) {
        // Trước khi tìm kiếm, chạy đồng bộ trạng thái để dữ liệu chính xác nhất
        this.capNhatTrangThaiTuDong();

        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return phieuGiamGiaRepository.searchAndFilter(trangThai, searchKeyword, ngayBatDau, ngayKetThuc, pageable);
    }

    public Optional<PhieuGiamGia> findById(Integer id) {
        return phieuGiamGiaRepository.findById(id);
    }

    public List<PhieuGiamGia> getActive() {
        // Lấy các phiếu đang active (trangThai = 0)
        return phieuGiamGiaRepository.findByTrangThai(0);
    }

    // ==================== 2. THÊM MỚI & CẬP NHẬT ====================

    // Hàm validate logic giảm giá (Private helper)
    private void validateDiscount(PhieuGiamGia pgg) {
        if (pgg.getHinhThucGiam() != null && pgg.getHinhThucGiam() == 2) { // Nếu là %
            if (pgg.getGiaTriGiam() != null && pgg.getGiaTriGiam().doubleValue() > 100) {
                throw new IllegalArgumentException("Giảm giá phần trăm không được quá 100%");
            }
        }
    }

    @Transactional
    public PhieuGiamGia saveWithStatusCheck(PhieuGiamGia pgg) {
        // Validate cơ bản
        if (pgg.getMaPhieuGiamGia() == null || pgg.getMaPhieuGiamGia().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phiếu không được trống.");
        }
        if (phieuGiamGiaRepository.findByMaPhieuGiamGia(pgg.getMaPhieuGiamGia()).isPresent()) {
            throw new IllegalArgumentException("Mã phiếu đã tồn tại: " + pgg.getMaPhieuGiamGia());
        }

        // Validate logic %
        validateDiscount(pgg);

        // Tự động tính trạng thái chuẩn
        pgg.setTrangThai(determineStatus(pgg));

        return phieuGiamGiaRepository.save(pgg);
    }

    @Transactional
    public PhieuGiamGia update(Integer id, PhieuGiamGia updatedPhieu) {
        PhieuGiamGia existingPhieu = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá ID: " + id));

        // Check trùng mã nếu mã thay đổi
        if (!existingPhieu.getMaPhieuGiamGia().equalsIgnoreCase(updatedPhieu.getMaPhieuGiamGia())) {
            if (phieuGiamGiaRepository.findByMaPhieuGiamGia(updatedPhieu.getMaPhieuGiamGia()).isPresent()) {
                throw new IllegalArgumentException("Mã phiếu đã tồn tại.");
            }
        }

        // Validate logic %
        validateDiscount(updatedPhieu);

        // Cập nhật thông tin cơ bản
        existingPhieu.setMaPhieuGiamGia(updatedPhieu.getMaPhieuGiamGia());
        existingPhieu.setTenPhieuGiamGia(updatedPhieu.getTenPhieuGiamGia());

        // Cập nhật các trường giảm giá mới
        existingPhieu.setHinhThucGiam(updatedPhieu.getHinhThucGiam());
        existingPhieu.setGiaTriGiam(updatedPhieu.getGiaTriGiam());
        existingPhieu.setSoTienGiam(updatedPhieu.getSoTienGiam()); // Max Discount
        existingPhieu.setDieuKienGiamGia(updatedPhieu.getDieuKienGiamGia());

        existingPhieu.setSoLuong(updatedPhieu.getSoLuong());
        existingPhieu.setNgayBatDau(updatedPhieu.getNgayBatDau());
        existingPhieu.setNgayKetThuc(updatedPhieu.getNgayKetThuc());
        existingPhieu.setMoTa(updatedPhieu.getMoTa());

        // Tính lại trạng thái dựa trên thông tin mới
        existingPhieu.setTrangThai(determineStatus(existingPhieu));

        return phieuGiamGiaRepository.save(existingPhieu);
    }

    // ==================== 3. CÁC HÀM XỬ LÝ KHÁC ====================

    // Dừng hoạt động (Xóa mềm)
    public PhieuGiamGia softDelete(Integer id) {
        PhieuGiamGia existingPhieu = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ID: " + id));
        existingPhieu.setTrangThai(1); // 1 = Ngừng hoạt động
        return phieuGiamGiaRepository.save(existingPhieu);
    }

    // Đảo trạng thái (Toggle: Active <-> Inactive)
    @Transactional
    public void toggleStatus(Integer id) {
        PhieuGiamGia pgg = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá ID: " + id));

        if (pgg.getTrangThai() == 1) {
            // Logic: Đang dừng (1) -> Muốn kích hoạt lại
            // Cần kiểm tra xem phiếu có hợp lệ (còn hạn, còn số lượng) không
            int statusChuan = determineStatus(pgg);

            if (statusChuan == 1) {
                throw new RuntimeException("Không thể kích hoạt! Phiếu đã hết hạn hoặc hết số lượng.");
            }
            // Nếu hợp lệ thì cho về 0 (Active) hoặc 2 (Upcoming) tùy thời gian
            pgg.setTrangThai(statusChuan);
        } else {
            // Logic: Đang chạy (0) hoặc Sắp tới (2) -> Muốn dừng
            pgg.setTrangThai(1);
        }

        phieuGiamGiaRepository.save(pgg);
    }

    // Trừ số lượng khi khách đặt hàng
    @Transactional
    public void decrementVoucher(PhieuGiamGia pgg) {
        if (pgg == null) return;
        PhieuGiamGia current = phieuGiamGiaRepository.findById(pgg.getId())
                .orElseThrow(() -> new RuntimeException("Phiếu giảm giá không tồn tại"));

        if (current.getSoLuong() != null) {
            if (current.getSoLuong() <= 0) {
                throw new RuntimeException("Voucher đã hết lượt sử dụng");
            }
            current.setSoLuong(current.getSoLuong() - 1);
            // Nếu về 0 thì dừng hoạt động
            if (current.getSoLuong() == 0) {
                current.setTrangThai(1);
            }
            phieuGiamGiaRepository.save(current);
        }
    }

    // ==================== 4. LOGIC TỰ ĐỘNG HÓA ====================

    /**
     * Hàm này QUAN TRỌNG NHẤT:
     * Nó quét toàn bộ DB và sửa lại trạng thái đúng với thời gian thực.
     */
    @Transactional
    public void capNhatTrangThaiTuDong() {
        List<PhieuGiamGia> listPhieu = phieuGiamGiaRepository.findAll();
        boolean isChanged = false;

        for (PhieuGiamGia p : listPhieu) {
            int statusChuan = determineStatus(p);

            if (p.getTrangThai() == null || p.getTrangThai() != statusChuan) {
                // Chỉ update nếu trạng thái hiện tại KHÁC trạng thái chuẩn
                p.setTrangThai(statusChuan);
                isChanged = true;
            }
        }

        if (isChanged) {
            phieuGiamGiaRepository.saveAll(listPhieu);
        }
    }

    /**
     * Logic cốt lõi xác định trạng thái
     * 0: Đang hoạt động (Active)
     * 1: Ngừng hoạt động / Hết hạn / Hết số lượng (Inactive)
     * 2: Sắp diễn ra (Upcoming)
     */
    private int determineStatus(PhieuGiamGia pgg) {
        LocalDateTime now = LocalDateTime.now(VN_ZONE);

        // 1. Ưu tiên check số lượng
        if (pgg.getSoLuong() == null || pgg.getSoLuong() <= 0) {
            return 1; // Hết số lượng -> Ngừng
        }

        // 2. Check thời gian
        if (now.isBefore(pgg.getNgayBatDau())) {
            return 2; // Chưa đến ngày -> Sắp diễn ra
        }

        if (pgg.getNgayKetThuc() != null && now.isAfter(pgg.getNgayKetThuc())) {
            return 1; // Đã quá ngày kết thúc -> Ngừng
        }

        // 3. Nếu còn số lượng + Trong khoảng thời gian -> Hoạt động
        return 0;
    }

    // ==================== 5. LOGIC VALIDATE CHO CHECKOUT (QUAN TRỌNG) ====================

    /**
     * Hàm kiểm tra xem Voucher có dùng được cho đơn hàng hiện tại không
     * Dùng cho API /api/checkout/calculate
     */
    public VoucherCheckResult kiemTraVoucherHople(String code, BigDecimal tongTienDonHang) {
        // 1. Validate đầu vào
        if (code == null || code.trim().isEmpty()) {
            return new VoucherCheckResult(false, "Mã giảm giá không hợp lệ.", 0.0);
        }
        if (tongTienDonHang == null) tongTienDonHang = BigDecimal.ZERO;

        // 2. Tìm voucher
        Optional<PhieuGiamGia> voucherOpt = phieuGiamGiaRepository.findByMaPhieuGiamGia(code);
        if (voucherOpt.isEmpty()) {
            return new VoucherCheckResult(false, "Mã giảm giá không tồn tại.", 0.0);
        }

        PhieuGiamGia voucher = voucherOpt.get();
        LocalDateTime now = LocalDateTime.now(); // Server time

        // 3. Check Trạng thái (QUAN TRỌNG: Dựa theo file cũ của bạn, 1 là Active)
        // Nếu hệ thống bạn quy định 0 là Active thì sửa số 1 thành 0 nhé.
        if (voucher.getTrangThai() != 1) {
            return new VoucherCheckResult(false, "Mã giảm giá đã ngừng hoạt động.", 0.0);
        }

        // 4. Check Số lượng
        if (voucher.getSoLuong() != null && voucher.getSoLuong() <= 0) {
            return new VoucherCheckResult(false, "Mã giảm giá đã hết lượt sử dụng.", 0.0);
        }

        // 5. Check Thời gian
        if (voucher.getNgayBatDau() != null && now.isBefore(voucher.getNgayBatDau())) {
            return new VoucherCheckResult(false, "Đợt giảm giá chưa bắt đầu.", 0.0);
        }
        if (voucher.getNgayKetThuc() != null && now.isAfter(voucher.getNgayKetThuc())) {
            return new VoucherCheckResult(false, "Mã giảm giá đã hết hạn.", 0.0);
        }

        // 6. Check ĐIỀU KIỆN ĐƠN HÀNG TỐI THIỂU (Dùng BigDecimal compareTo)
        BigDecimal dieuKien = voucher.getDieuKienGiamGia() != null ? voucher.getDieuKienGiamGia() : BigDecimal.ZERO;
        // tongTien < dieuKien (compareTo trả về -1)
        if (tongTienDonHang.compareTo(dieuKien) < 0) {
            // Format tiền tệ để hiển thị thông báo đẹp hơn
            String msg = String.format("Đơn hàng phải từ %,.0fđ mới được dùng mã này.", dieuKien.doubleValue());
            return new VoucherCheckResult(false, msg, 0.0);
        }

        // 7. Tính toán số tiền được giảm
        BigDecimal soTienGiam = BigDecimal.ZERO;
        BigDecimal giaTriGiam = voucher.getGiaTriGiam() != null ? voucher.getGiaTriGiam() : BigDecimal.ZERO;

        if (voucher.getHinhThucGiam() == 1) {
            // --- TH1: Giảm tiền mặt (VND) ---
            soTienGiam = giaTriGiam;
        } else {
            // --- TH2: Giảm phần trăm (%) ---
            // Logic: (Tổng tiền * % giảm) / 100
            // Chặn % không quá 100
            if (giaTriGiam.compareTo(new BigDecimal(100)) > 0) {
                giaTriGiam = new BigDecimal(100);
            }

            soTienGiam = tongTienDonHang.multiply(giaTriGiam)
                    .divide(new BigDecimal(100), 0, RoundingMode.HALF_UP); // Làm tròn

            // Check số tiền giảm TỐI ĐA (Max Discount)
            if (voucher.getSoTienGiam() != null) { // Lưu ý: Cột này là mức giảm tối đa
                BigDecimal maxGiam = voucher.getSoTienGiam();
                if (soTienGiam.compareTo(maxGiam) > 0) {
                    soTienGiam = maxGiam;
                }
            }
        }

        // 8. Chốt chặn cuối cùng: Không bao giờ giảm quá giá trị đơn hàng
        if (soTienGiam.compareTo(tongTienDonHang) > 0) {
            soTienGiam = tongTienDonHang;
        }

        // Trả về kết quả (Convert BigDecimal sang double để khớp với DTO cũ của bạn)
        return new VoucherCheckResult(true, "Áp dụng mã thành công!", soTienGiam.doubleValue());
    }

    // Helper format tiền tệ cho thông báo lỗi đẹp
    private String formatCurrency(Double amount) {
        return String.format("%,.0f đ", amount);
    }
// Trong PhieuGiamgiaService.java

    public String timVoucherTotNhat(BigDecimal subTotal) {
        // 1. Lấy tất cả voucher đang hoạt động
        List<PhieuGiamGia> activeVouchers = phieuGiamGiaRepository.findAllActiveVouchers();

        String bestCode = null;
        BigDecimal maxDiscount = BigDecimal.ZERO;

        for (PhieuGiamGia v : activeVouchers) {
            // 2. Kiểm tra điều kiện đơn hàng tối thiểu
            BigDecimal dieuKien = v.getDieuKienGiamGia() != null ? v.getDieuKienGiamGia() : BigDecimal.ZERO;

            // Nếu tổng tiền < điều kiện -> Bỏ qua voucher này
            if (subTotal.compareTo(dieuKien) < 0) {
                continue;
            }

            // 3. Tính toán số tiền giảm thử
            BigDecimal currentDiscount = BigDecimal.ZERO;

            if (v.getHinhThucGiam() == 1) {
                // -- Giảm tiền mặt --
                currentDiscount = v.getGiaTriGiam();
            } else {
                // -- Giảm phần trăm --
                BigDecimal phanTram = v.getGiaTriGiam();
                // Chặn > 100%
                if (phanTram.compareTo(new BigDecimal(100)) > 0) phanTram = new BigDecimal(100);

                currentDiscount = subTotal.multiply(phanTram).divide(new BigDecimal(100));

                // Kiểm tra giảm tối đa (Max Cap)
                if (v.getSoTienGiam() != null && currentDiscount.compareTo(v.getSoTienGiam()) > 0) {
                    currentDiscount = v.getSoTienGiam();
                }
            }

            // Không được giảm quá tổng tiền đơn hàng
            if (currentDiscount.compareTo(subTotal) > 0) {
                currentDiscount = subTotal;
            }

            // 4. So sánh để tìm Best Option
            // Nếu mức giảm này lớn hơn mức giảm lớn nhất hiện tại -> Cập nhật
            if (currentDiscount.compareTo(maxDiscount) > 0) {
                maxDiscount = currentDiscount;
                bestCode = v.getMaPhieuGiamGia();
            }
        }

        return bestCode; // Trả về mã tốt nhất (hoặc null nếu không tìm được)
    }
    // DTO Record để trả về kết quả
    public record VoucherCheckResult(boolean isValid, String message, Double discountAmount) {}
}