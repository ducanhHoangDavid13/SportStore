package sd_04.datn_fstore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.KhachHang;
import sd_04.datn_fstore.repo.KhachHangRepo;

@Service
public class KhachhangService {
    @Autowired
    private KhachHangRepo khachHangRepo;

    // Hàm chính: Lấy danh sách khách hàng đã được LỌC và PHÂN TRANG
    public Page<KhachHang> getFilteredKhachHang(
            String keyword, String sdt, Boolean gioiTinh, int pageNo, int pageSize) {

        // Spring Data JPA dùng page index bắt đầu từ 0
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);

        // Chuẩn hóa tham số để dùng trong truy vấn @Query (đảm bảo truyền null nếu rỗng)
        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String searchSdt = (sdt != null && !sdt.trim().isEmpty()) ? sdt.trim() : null;

        return khachHangRepo.findFilteredKhachHang(searchKeyword, searchSdt, gioiTinh, pageable);
    }
}
