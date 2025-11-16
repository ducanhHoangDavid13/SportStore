package sd_04.datn_fstore.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.repository.Query; // <-- BẠN SẼ CẦN IMPORT NÀY
import sd_04.datn_fstore.model.PhieuGiamGia;
import sd_04.datn_fstore.repository.PhieuGiamGiaRepo;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class PhieuGiamgiaService {

    @Autowired
    private PhieuGiamGiaRepo phieuGiamGiaRepository;
    private final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    public Page<PhieuGiamGia> searchAndFilter(
            Integer trangThai,
            String keyword,
            LocalDateTime ngayBatDau,
            LocalDateTime ngayKetThuc,
            int page,
            int size,
            String sortField,
            String sortDir) {

        // 1. Chuẩn hóa Keyword
        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

        // 2. Tạo đối tượng Sort
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();

        // 3. Tạo đối tượng Pageable
        Pageable pageable = PageRequest.of(page, size, sort);

        // 4. Gọi Repository
        return phieuGiamGiaRepository.searchAndFilter(
                trangThai,
                searchKeyword,
                ngayBatDau,
                ngayKetThuc,
                pageable);
    }

    public Optional<PhieuGiamGia> findById(Integer id) {
        return phieuGiamGiaRepository.findById(id);
    }

    // === 1. SỬA LỖI LOGIC Ở ĐÂY ===
    public List<PhieuGiamGia> getActive() {
        // Lấy voucher ĐANG HOẠT ĐỘNG (trangThai = 0)
        // và còn lượt sử dụng (soLuong > 0 hoặc soLuong = null)
        // và trong thời gian diễn ra
        LocalDateTime now = LocalDateTime.now(VN_ZONE);

        // Bạn cần thêm hàm này vào PhieuGiamGiaRepo.java:
        /*
         @Query("SELECT p FROM PhieuGiamGia p WHERE p.trangThai = 0 " +
                "AND (p.soLuong IS NULL OR p.soLuong > 0) " +
                "AND p.ngayBatDau <= :now " +
                "AND (p.ngayKetThuc IS NULL OR p.ngayKetThuc >= :now)")
         List<PhieuGiamGia> findActiveVouchers(@Param("now") LocalDateTime now);
        */
        return phieuGiamGiaRepository.findActiveVouchers(now);
    } // <-- DẤU NGOẶC } CỦA HÀM getActive() PHẢI Ở ĐÂY

    // === 2. HÀM NÀY PHẢI NẰM NGOÀI ===
    /**
     * Giảm lượt sử dụng của Voucher.
     * Được gọi bởi BanHangService sau khi thanh toán thành công.
     */
    @Transactional
    public void decrementVoucher(PhieuGiamGia pgg) {
        if (pgg == null) return;

        // Chỉ giảm số lượng nếu nó được quản lý (không phải null)
        if (pgg.getSoLuong() != null) {
            if (pgg.getSoLuong() <= 0) {
                // Double check, ném lỗi nếu voucher đã hết
                throw new RuntimeException("Voucher đã hết lượt sử dụng: " + pgg.getMaPhieuGiamGia());
            }
            pgg.setSoLuong(pgg.getSoLuong() - 1);

            // Tự động chuyển trạng thái nếu hết
            if (pgg.getSoLuong() == 0) {
                pgg.setTrangThai(1); // 1 = Hết/Dừng
            }
            phieuGiamGiaRepository.save(pgg);
        }
    }
    // <-- DẤU NGOẶC } CỦA HÀM decrementVoucher() Ở ĐÂY

    // --- Phương thức gán trạng thái tự động (Đã sửa lỗi và dùng LocalDateTime) ---
    private void setStatusBasedOnDates(PhieuGiamGia pgg) {
        // Khắc phục lỗi: Dùng LocalDateTime.now(VN_ZONE)
        LocalDateTime now = LocalDateTime.now(VN_ZONE);

        // (Logic trạng thái của bạn: 0=Active, 1=Stopped/Expired, 2=Upcoming)
        if (pgg.getNgayBatDau().isAfter(now)) {
            pgg.setTrangThai(2); // Sắp diễn ra
        } else if (pgg.getNgayKetThuc() != null && pgg.getNgayKetThuc().isBefore(now)) {
            pgg.setTrangThai(1); // Hết hạn
        } else {
            // Chỉ gán = 0 (Hoạt động) nếu nó không bị dừng thủ công (trangThai != 1)
            if (pgg.getTrangThai() != 1) {
                pgg.setTrangThai(0); // Đang hoạt động
            }
        }
    }

    // --- Phương thức Thêm mới (Bổ sung check trùng mã) ---
    public PhieuGiamGia saveWithStatusCheck(PhieuGiamGia pgg) {

        // --- 1. Validation Backend Cơ bản (Giữ nguyên) ---
        // (Giữ nguyên code validation của bạn)

        // --- 2. Bổ sung check trùng mã khi thêm mới ---
        if (pgg.getMaPhieuGiamGia() == null || pgg.getMaPhieuGiamGia().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phiếu giảm giá không được để trống.");
        }
        if (phieuGiamGiaRepository.findByMaPhieuGiamGia(pgg.getMaPhieuGiamGia()).isPresent()) {
            throw new IllegalArgumentException("Mã phiếu giảm giá đã tồn tại.");
        }

        // --- 3. Logic Gán Trạng Thái Tự Động ---
        setStatusBasedOnDates(pgg);

        // 4. Lưu vào Repository
        return phieuGiamGiaRepository.save(pgg);
    }

    // --- THÊM: Phương thức Cập nhật (Update) ---
    public PhieuGiamGia update(Integer id, PhieuGiamGia updatedPhieu) {
        PhieuGiamGia existingPhieu = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Phiếu Giảm Giá có ID: " + id));

        // 1. Validation: Kiểm tra trùng lặp Mã (nếu mã mới khác mã cũ)
        if (!existingPhieu.getMaPhieuGiamGia().equalsIgnoreCase(updatedPhieu.getMaPhieuGiamGia())) {
            Optional<PhieuGiamGia> duplicateMa = phieuGiamGiaRepository.findByMaPhieuGiamGia(updatedPhieu.getMaPhieuGiamGia());
            if (duplicateMa.isPresent()) {
                throw new IllegalArgumentException("Mã phiếu giảm giá đã tồn tại.");
            }
        }

        // 2. Cập nhật các trường dữ liệu
        existingPhieu.setMaPhieuGiamGia(updatedPhieu.getMaPhieuGiamGia());
        existingPhieu.setTenPhieuGiamGia(updatedPhieu.getTenPhieuGiamGia());
        existingPhieu.setSoTienGiam(updatedPhieu.getSoTienGiam());
        existingPhieu.setDieuKienGiamGia(updatedPhieu.getDieuKienGiamGia());
        existingPhieu.setSoLuong(updatedPhieu.getSoLuong());
        existingPhieu.setNgayBatDau(updatedPhieu.getNgayBatDau());
        existingPhieu.setNgayKetThuc(updatedPhieu.getNgayKetThuc());

        // 3. Tự động kiểm tra và cập nhật lại trạng thái dựa trên thời gian
        // Chỉ cập nhật tự động nếu người dùng không chủ động set là 1 (Dừng)
        if (updatedPhieu.getTrangThai() != null && updatedPhieu.getTrangThai() == 1) {
            existingPhieu.setTrangThai(1);
        } else {
            setStatusBasedOnDates(existingPhieu);
        }

        return phieuGiamGiaRepository.save(existingPhieu);
    }

    // --- THÊM: Phương thức Xóa Mềm (Soft Delete) ---
    public PhieuGiamGia softDelete(Integer id) {
        PhieuGiamGia existingPhieu = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Phiếu Giảm Giá có ID: " + id));

        existingPhieu.setTrangThai(1); // 1 = Dừng hoạt động

        return phieuGiamGiaRepository.save(existingPhieu);
    }

    @Transactional
    public int syncPromotionStatus() {
        LocalDateTime now = LocalDateTime.now(VN_ZONE);
        int updatedCount = 0;

        // 1. Cập nhật: Đang hoạt động (0) -> Dừng hoạt động (1)
        // Bạn cần thêm hàm này vào Repo:
        // @Query("SELECT p FROM PhieuGiamGia p WHERE p.trangThai = 0 AND p.ngayKetThuc IS NOT NULL AND p.ngayKetThuc < :now")
        // List<PhieuGiamGia> findExpiredActivePromotions(@Param("now") LocalDateTime now);
        List<PhieuGiamGia> expiredList = phieuGiamGiaRepository.findExpiredActivePromotions(now);
        for (PhieuGiamGia pgg : expiredList) {
            pgg.setTrangThai(1);
            phieuGiamGiaRepository.save(pgg);
            updatedCount++;
        }

        // 2. Cập nhật: Sắp diễn ra (2) -> Đang hoạt động (0)
        // Bạn cần thêm hàm này vào Repo:
        // @Query("SELECT p FROM PhieuGiamGia p WHERE p.trangThai = 2 AND p.ngayBatDau <= :now")
        // List<PhieuGiamGia> findUpcomingPromotionsToActivate(@Param("now") LocalDateTime now);
        List<PhieuGiamGia> upcomingList = phieuGiamGiaRepository.findUpcomingPromotionsToActivate(now);
        for (PhieuGiamGia pgg : upcomingList) {
            pgg.setTrangThai(0);
            phieuGiamGiaRepository.save(pgg);
            updatedCount++;
        }

        return updatedCount;
    }
}