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
        this.syncStatus(); // Đổi tên hàm cho ngắn gọn

        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return phieuGiamGiaRepository.searchAndFilter(trangThai, searchKeyword, ngayBatDau, ngayKetThuc, pageable);
    }

    public Optional<PhieuGiamGia> findById(Integer id) {
        return phieuGiamGiaRepository.findById(id);
    }

    public List<PhieuGiamGia> getActive() {
        // Trạng thái '0' là Đang hoạt động
        return phieuGiamGiaRepository.findByTrangThai(0);
    }

    // ==================== 2. THÊM MỚI & CẬP NHẬT ====================

    // Hàm validate logic giảm giá (Private helper)
    private void validateDiscount(PhieuGiamGia pgg) {
        if (pgg.getHinhThucGiam() != null && pgg.getHinhThucGiam() == 2) { // Nếu là %
            // Chuyển BigDecimal sang Double để so sánh đơn giản (do đã được validate ở front-end)
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
        // SỬ DỤNG HÀM CŨ:
        pgg.setTrangThai(determineStatus(pgg));

        return phieuGiamGiaRepository.save(pgg);
    }

    @Transactional
    public PhieuGiamGia update(Integer id, PhieuGiamGia updatedPhieu) {
        PhieuGiamGia existingPhieu = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá ID: " + id));

        // Check trùng mã nếu mã thay đổi
        if (!existingPhieu.getMaPhieuGiamGia().equalsIgnoreCase(updatedPhieu.getMaPhieuGiamGia())) {
            // Kiểm tra chỉ khi ID khác (đã có trong hàm findByMaPhieuGiamGia)
            if (phieuGiamGiaRepository.findByMaPhieuGiamGia(updatedPhieu.getMaPhieuGiamGia()).isPresent() &&
                    !phieuGiamGiaRepository.findByMaPhieuGiamGia(updatedPhieu.getMaPhieuGiamGia()).get().getId().equals(id)) {
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
        // SỬ DỤNG HÀM CŨ:
        existingPhieu.setTrangThai(determineStatus(existingPhieu));

        return phieuGiamGiaRepository.save(existingPhieu);
    }

    // ==================== 3. CÁC HÀM XỬ LÝ KHÁC (Tối ưu hóa) ====================

    // Dừng hoạt động (Chuyển về trạng thái 1)
    @Transactional
    public PhieuGiamGia softDelete(Integer id) {
        PhieuGiamGia existingPhieu = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ID: " + id));
        existingPhieu.setTrangThai(1); // 1 = Ngừng hoạt động thủ công
        return phieuGiamGiaRepository.save(existingPhieu);
    }

    // Đảo trạng thái (Toggle: Active <-> Inactive thủ công)
    @Transactional
    public void toggleStatus(Integer id) {
        PhieuGiamGia pgg = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu"));

        LocalDateTime now = LocalDateTime.now(VN_ZONE);
        int currentStatus = pgg.getTrangThai() != null ? pgg.getTrangThai() : determineStatus(pgg); // Lấy status hiện tại

        if (currentStatus != 1) {
            // --- TRƯỜNG HỢP MUỐN DỪNG (0, 2) -> (1) ---
            pgg.setTrangThai(1);
        } else {
            // --- TRƯỜNG HỢP MUỐN KÍCH HOẠT LẠI (1) -> (0 hoặc 2) ---

            // 1. Check Số lượng
            if (pgg.getSoLuong() != null && pgg.getSoLuong() <= 0) {
                throw new RuntimeException("Phiếu này đã hết số lượng dùng, không thể kích hoạt lại!");
            }

            // 2. Check Hạn
            if (pgg.getNgayKetThuc() != null && now.isAfter(pgg.getNgayKetThuc())) {
                throw new RuntimeException("Phiếu này đã hết hạn, không thể kích hoạt lại! Vui lòng sửa ngày kết thúc.");
            }

            // 3. Thiết lập trạng thái mới
            if (now.isBefore(pgg.getNgayBatDau())) {
                pgg.setTrangThai(2); // Về trạng thái Sắp tới
            } else {
                pgg.setTrangThai(0); // Về trạng thái Đang hoạt động
            }
        }

        phieuGiamGiaRepository.save(pgg);
    }

    // Trừ số lượng khi khách đặt hàng
    @Transactional
    public void decrementVoucher(PhieuGiamGia pgg) {
        if (pgg == null) return;

        // Sử dụng Lock/findById để đảm bảo tính toàn vẹn khi nhiều người dùng cùng lúc đặt hàng
        PhieuGiamGia current = phieuGiamGiaRepository.findById(pgg.getId())
                .orElseThrow(() -> new RuntimeException("Phiếu giảm giá không tồn tại"));

        if (current.getSoLuong() != null) {
            if (current.getSoLuong() <= 0) {
                // Có thể ném lỗi hoặc chỉ log nếu muốn bỏ qua
                throw new RuntimeException("Voucher đã hết lượt sử dụng");
            }
            current.setSoLuong(current.getSoLuong() - 1);

            // Nếu về 0 thì chuyển trạng thái Đang hoạt động (0) thành Đã dừng (1)
            // LƯU Ý: Chỉ chuyển nếu đang là 0 hoặc 2, nếu đang là 1 thì giữ nguyên.
            if (current.getSoLuong() == 0 && current.getTrangThai() != 1) {
                current.setTrangThai(1);
            }
            phieuGiamGiaRepository.save(current);
        }
    }

    // ==================== 4. LOGIC TỰ ĐỘNG HÓA (Đã tối ưu hóa) ====================

    /**
     * Hàm đồng bộ trạng thái tự động (SỬ DỤNG CÁC HÀM QUERY TỐI ƯU TRONG REPO)
     * Thay thế cho `capNhatTrangThaiTuDong` và `updateStatusAuto`
     */
    @Transactional
    public void syncStatus() {
        LocalDateTime now = LocalDateTime.now(VN_ZONE);

        // 1. Chuyển từ Sắp tới (2) sang Đang chạy (0) nếu ĐÃ ĐẾN NGÀY BẮT ĐẦU
        // Dùng hàm bạn khai báo trong Repo:
        List<PhieuGiamGia> upcomingToActivate = phieuGiamGiaRepository.findUpcomingPromotionsToActivate(now);
        for (PhieuGiamGia pgg : upcomingToActivate) {
            pgg.setTrangThai(0); // 0: Đang chạy
        }
        phieuGiamGiaRepository.saveAll(upcomingToActivate);


        // 2. Chuyển từ Đang chạy (0) sang Đã dừng (1) nếu HẾT HẠN
        // Dùng hàm bạn khai báo trong Repo:
        List<PhieuGiamGia> expiredActive = phieuGiamGiaRepository.findExpiredActivePromotions(now);
        for (PhieuGiamGia pgg : expiredActive) {
            pgg.setTrangThai(1); // 1: Đã dừng
        }
        phieuGiamGiaRepository.saveAll(expiredActive);
    }

    // Giữ lại hàm logic cốt lõi để dùng cho Save/Update/Toggle
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
     * DÙNG TRẠNG THÁI '0' LÀ ACTIVE để thống nhất với logic determineStatus
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
        LocalDateTime now = LocalDateTime.now(VN_ZONE);

        // 3. TÍNH LẠI TRẠNG THÁI CHUẨN: Để đảm bảo không bị dùng mã hết hạn/hết số lượng
        int statusChuan = determineStatus(voucher);

        // 4. Check Trạng thái
        if (statusChuan != 0) { // Nếu trạng thái chuẩn KHÔNG phải Đang hoạt động (0)
            String message;
            if (statusChuan == 1) {
                message = voucher.getSoLuong() != null && voucher.getSoLuong() <= 0 ?
                        "Mã giảm giá đã hết lượt sử dụng." : "Mã giảm giá đã hết hạn hoặc bị ngừng hoạt động.";
            } else if (statusChuan == 2) {
                message = "Đợt giảm giá chưa bắt đầu.";
            } else {
                message = "Mã giảm giá không áp dụng được lúc này.";
            }
            return new VoucherCheckResult(false, message, 0.0);
        }

        // **Bỏ qua check số lượng, thời gian vì đã được determineStatus check**

        // 5. Check ĐIỀU KIỆN ĐƠN HÀNG TỐI THIỂU
        BigDecimal dieuKien = voucher.getDieuKienGiamGia() != null ? voucher.getDieuKienGiamGia() : BigDecimal.ZERO;
        if (tongTienDonHang.compareTo(dieuKien) < 0) {
            String msg = String.format("Đơn hàng phải từ %,.0fđ mới được dùng mã này.", dieuKien.doubleValue());
            return new VoucherCheckResult(false, msg, 0.0);
        }

        // 6. Tính toán số tiền được giảm
        BigDecimal soTienGiam = tinhToanSoTienGiam(voucher, tongTienDonHang);

        // Trả về kết quả
        return new VoucherCheckResult(true, "Áp dụng mã thành công!", soTienGiam.doubleValue());
    }

    // Hàm Helper tính toán mức giảm (Giúp code gọn hơn)
    private BigDecimal tinhToanSoTienGiam(PhieuGiamGia voucher, BigDecimal tongTienDonHang) {
        BigDecimal soTienGiam = BigDecimal.ZERO;
        BigDecimal giaTriGiam = voucher.getGiaTriGiam() != null ? voucher.getGiaTriGiam() : BigDecimal.ZERO;

        if (voucher.getHinhThucGiam() == 1) {
            // --- TH1: Giảm tiền mặt (VND) ---
            soTienGiam = giaTriGiam;
        } else {
            // --- TH2: Giảm phần trăm (%) ---
            if (giaTriGiam.compareTo(new BigDecimal(100)) > 0) {
                giaTriGiam = new BigDecimal(100);
            }

            soTienGiam = tongTienDonHang.multiply(giaTriGiam)
                    .divide(new BigDecimal(100), 0, RoundingMode.HALF_UP);

            // Check số tiền giảm TỐI ĐA (Max Discount)
            if (voucher.getSoTienGiam() != null) {
                BigDecimal maxGiam = voucher.getSoTienGiam();
                if (soTienGiam.compareTo(maxGiam) > 0) {
                    soTienGiam = maxGiam;
                }
            }
        }

        // Chốt chặn cuối cùng: Không bao giờ giảm quá giá trị đơn hàng
        if (soTienGiam.compareTo(tongTienDonHang) > 0) {
            soTienGiam = tongTienDonHang;
        }
        return soTienGiam;
    }

    // ==================== 6. TÌM VOUCHER TỐT NHẤT ====================

    public String timVoucherTotNhat(BigDecimal subTotal) {
        // 1. Lấy tất cả voucher đang hoạt động (Trạng thái 0)
        // **LƯU Ý:** Hàm findByTrangThai(0) là cách đơn giản nhất, nhưng nếu hàm findAllActiveVouchers
        // trong Repo đã được sửa để lấy những thằng có số lượng > 0 và còn hạn, thì nên dùng nó.
        // Giả định dùng hàm findAllActiveVouchers đã được sửa trong Repo (trạng thái = 0)
        List<PhieuGiamGia> activeVouchers = phieuGiamGiaRepository.findAllActiveVouchers();

        String bestCode = null;
        BigDecimal maxDiscount = BigDecimal.ZERO;

        for (PhieuGiamGia v : activeVouchers) {
            // 2. Kiểm tra điều kiện đơn hàng tối thiểu
            BigDecimal dieuKien = v.getDieuKienGiamGia() != null ? v.getDieuKienGiamGia() : BigDecimal.ZERO;

            if (subTotal.compareTo(dieuKien) < 0) {
                continue;
            }

            // 3. Tính toán số tiền giảm thử
            BigDecimal currentDiscount = tinhToanSoTienGiam(v, subTotal);

            // 4. So sánh để tìm Best Option
            if (currentDiscount.compareTo(maxDiscount) > 0) {
                maxDiscount = currentDiscount;
                bestCode = v.getMaPhieuGiamGia();
            }
        }

        return bestCode; // Trả về mã tốt nhất (hoặc null nếu không tìm được)
    }

    public List<PhieuGiamGia> findAllActiveVouchers() {
        // Đồng bộ trạng thái trước khi lấy để đảm bảo tính chính xác
        this.syncStatus();

        // Lấy thời gian hiện tại
        LocalDateTime now = LocalDateTime.now(VN_ZONE);

        // Gọi hàm đã tạo trong Repository
        return phieuGiamGiaRepository.findAllActiveVouchers(now);
    }
    // Trong PhieuGiamgiaServiceImpl.java

// ... (Các imports và RequiredArgsConstructor)

    public PhieuGiamGia kiemTraVaLayVoucherHople(String maVoucher, BigDecimal subTotal) {
        if (maVoucher == null || maVoucher.trim().isEmpty()) {
            return null;
        }

        Optional<PhieuGiamGia> pggOpt = phieuGiamGiaRepository.findByMaPhieuGiamGia(maVoucher);
        if (pggOpt.isEmpty()) {
            throw new IllegalArgumentException("Mã giảm giá không tồn tại.");
        }

        PhieuGiamGia pgg = pggOpt.get();
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(pgg.getNgayBatDau())) {
            throw new IllegalArgumentException("Mã giảm giá chưa đến ngày bắt đầu sử dụng!");
        }
        if (now.isAfter(pgg.getNgayKetThuc())) {
            throw new IllegalArgumentException("Mã giảm giá đã hết hạn sử dụng!");
        }

        // 🔥 CHECK TRẠNG THÁI THEO ĐỊNH NGHĨA CỦA BẠN: Nếu 1 là Active
        if (pgg.getTrangThai() != 1) {
            throw new IllegalArgumentException("Mã giảm giá này đã bị dừng/hủy!");
        }

        if (pgg.getSoLuong() != null && pgg.getSoLuong() <= 0) {
            throw new IllegalArgumentException("Mã giảm giá đã hết lượt sử dụng!");
        }

        if (pgg.getDieuKienGiamGia() != null && subTotal.compareTo(pgg.getDieuKienGiamGia()) < 0) {
            // Thông báo điều kiện tối thiểu
            throw new IllegalArgumentException("Đơn hàng chưa đạt tối thiểu " + String.format("%,.0f", pgg.getDieuKienGiamGia()) + "đ");
        }

        return pgg;
    }

    // DTO Record để trả về kết quả
    public record VoucherCheckResult(boolean isValid, String message, Double discountAmount) {}
}