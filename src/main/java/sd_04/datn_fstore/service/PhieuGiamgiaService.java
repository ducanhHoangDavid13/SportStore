package sd_04.datn_fstore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.PhieuGiamGia;
import sd_04.datn_fstore.repo.PhieuGiamGiaRepo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class PhieuGiamgiaService {

    @Autowired
    private PhieuGiamGiaRepo phieuGiamGiaRepository;

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

    public PhieuGiamGia saveWithStatusCheck(PhieuGiamGia pgg) {

        // --- 1. Validation Backend Cơ bản (Dù Frontend đã validate) ---

        // Kiểm tra các trường bắt buộc (nếu có trường hợp form gửi null)
        if (pgg.getMaPhieuGiamGia() == null || pgg.getTenPhieuGiamGia() == null) {
            throw new IllegalArgumentException("Mã hoặc tên phiếu giảm giá không được để trống.");
        }

        // Kiểm tra số tiền giảm và điều kiện (giả sử chúng là BigDecimal hoặc Double trong model)
        if (pgg.getSoTienGiam() == null || pgg.getSoTienGiam().doubleValue() <= 0) {
            throw new IllegalArgumentException("Số tiền giảm phải lớn hơn 0.");
        }
        // Giả sử dieuKienGiamGia phải >= 0
        if (pgg.getDieuKienGiamGia() == null || pgg.getDieuKienGiamGia().doubleValue() < 0) {
            throw new IllegalArgumentException("Điều kiện giảm giá không hợp lệ.");
        }


        // --- 2. Logic Gán Trạng Thái Tự Động ---

        // Ngày bắt đầu đã được gửi dưới dạng LocalDateTime (ví dụ: "2025-10-25T00:00:00")
        LocalDateTime ngayBatDauDateTime = pgg.getNgayBatDau();
        if (ngayBatDauDateTime == null) {
            throw new IllegalArgumentException("Ngày bắt đầu không được để trống.");
        }

        // Chuyển sang LocalDate để so sánh chính xác ngày
        LocalDate ngayBatDauDate = ngayBatDauDateTime.toLocalDate();
        LocalDate ngayHienTai = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")); // Nên dùng TimeZone cụ thể

        if (ngayBatDauDate.isEqual(ngayHienTai)) {
            // Ngày bắt đầu là ngày hiện tại -> Đang hoạt động
            pgg.setTrangThai(0);
        } else if (ngayBatDauDate.isAfter(ngayHienTai)) {
            // Ngày bắt đầu là ngày mai trở đi -> Sắp diễn ra
            pgg.setTrangThai(2);
        } else {
            // Ngày đã trôi qua -> Coi là lỗi hoặc Dừng hoạt động (dù Frontend đã ngăn)
            pgg.setTrangThai(1);
        }

        // 3. Lưu vào Repository
        return phieuGiamGiaRepository.save(pgg);
    }
}
