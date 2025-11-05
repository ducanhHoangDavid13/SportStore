package sd_04.datn_fstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sd_04.datn_fstore.model.HoaDon;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HoaDonService {

    /**
     * Hàm tìm kiếm phân trang và lọc theo nhiều điều kiện cho Admin
     */
    Page<HoaDon> search(Pageable pageable, List<Integer> trangThaiList,
                        LocalDateTime ngayBatDau, LocalDateTime ngayKetThuc, String keyword);

    Optional<HoaDon> getById(Integer id);

    HoaDon add(HoaDon hoaDon);

    /**
     * Hàm quan trọng: Xử lý logic nghiệp vụ khi thay đổi trạng thái
     * (Ví dụ: Hủy đơn thì phải hoàn kho)
     */
    void updateTrangThai(Integer hoaDonId, Integer newTrangThai);
}