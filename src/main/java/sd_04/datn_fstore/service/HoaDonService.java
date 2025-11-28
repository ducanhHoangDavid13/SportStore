package sd_04.datn_fstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sd_04.datn_fstore.model.HoaDon;

import java.math.BigDecimal; // <-- THÊM IMPORT NÀY
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HoaDonService {

    /**
     * SỬA LẠI: Thêm 2 tham số lọc theo giá
     * (Dùng cho API /api/admin/hoadon/search)
     */
    Page<HoaDon> search(Pageable pageable, List<Integer> trangThaiList,
                        LocalDateTime ngayBatDau, LocalDateTime ngayKetThuc,
                        String keyword, BigDecimal minPrice, BigDecimal maxPrice);
    Optional<HoaDon> getById(Integer id);

    HoaDon add(HoaDon hoaDon);

    /**
     * (Giữ nguyên) Hàm cập nhật trạng thái
     * (Dùng cho API /api/admin/hoadon/update-status)
     */
    void updateTrangThai(Integer hoaDonId, Integer newTrangThai);

    // --- THÊM 3 HÀM MỚI (Để cung cấp API cho JavaScript) ---

    /**
     * THÊM MỚI: Dùng cho API /api/hoadon/list
     */
    List<HoaDon> getAll();

    /**
     * THÊM MỚI: Dùng cho API /api/hoadon/trangthai
     */
    List<HoaDon> getByTrangThai(Integer trangThai);

    /**
     * THÊM MỚI: Dùng cho API /api/hoadon/date
     */
    List<HoaDon> getByDateRange(LocalDateTime startTime, LocalDateTime endTime);

}