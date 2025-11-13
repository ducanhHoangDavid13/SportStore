package sd_04.datn_fstore.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
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

    // --- SỬA LỖI Ở ĐÂY ---
    // @Override // <-- XÓA DÒNG NÀY
    public List<PhieuGiamGia> getActive() {
        // (Lưu ý: Bạn cũng nên kiểm tra cả ngày bắt đầu/kết thúc ở đây nếu cần)

        // Gọi hàm findByTrangThai(1)
        return phieuGiamGiaRepository.findByTrangThai(1);
    }

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
            pgg.setTrangThai(0); // Đang hoạt động
        }
    }

    // --- Phương thức Thêm mới (Bổ sung check trùng mã) ---
    public PhieuGiamGia saveWithStatusCheck(PhieuGiamGia pgg) {

        // --- 1. Validation Backend Cơ bản (Giữ nguyên) ---
        // (Giữ nguyên code validation của bạn)

        // --- 2. Bổ sung check trùng mã khi thêm mới ---
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
        // (Cập nhật các trường khác...)
        existingPhieu.setNgayBatDau(updatedPhieu.getNgayBatDau());
        existingPhieu.setNgayKetThuc(updatedPhieu.getNgayKetThuc());

        // Cập nhật trạng thái thủ công nếu người dùng muốn
        if (updatedPhieu.getTrangThai() != null) {
            existingPhieu.setTrangThai(updatedPhieu.getTrangThai());
        }

        // 3. Tự động kiểm tra và cập nhật lại trạng thái dựa trên thời gian
        setStatusBasedOnDates(existingPhieu);

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
        List<PhieuGiamGia> expiredList = phieuGiamGiaRepository.findExpiredActivePromotions(now);
        for (PhieuGiamGia pgg : expiredList) {
            pgg.setTrangThai(1);
            phieuGiamGiaRepository.save(pgg);
            updatedCount++;
        }

        // 2. Cập nhật: Sắp diễn ra (2) -> Đang hoạt động (0)
        List<PhieuGiamGia> upcomingList = phieuGiamGiaRepository.findUpcomingPromotionsToActivate(now);
        for (PhieuGiamGia pgg : upcomingList) {
            pgg.setTrangThai(0);
            phieuGiamGiaRepository.save(pgg);
            updatedCount++;
        }

        return updatedCount;
    }
}