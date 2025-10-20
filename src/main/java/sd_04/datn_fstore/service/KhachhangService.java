package sd_04.datn_fstore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.KhachHang;
import sd_04.datn_fstore.repo.KhachHangRepo;

import java.util.Optional;

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

    public KhachHang save(KhachHang khachhang) {
        // Nếu là thêm mới (id == null), thiết lập trạng thái mặc định là 1 (Hoạt động)
        if (khachhang.getId() == null) {
            khachhang.setTrangThai(1);
            // Có thể thêm logic set vai trò mặc định nếu cần
            // khachhang.setVaiTro("KHACH_HANG");
        }
        return khachHangRepo.save(khachhang);
    }

    public Optional<KhachHang> findById(Integer id) {
        return khachHangRepo.findById(id);
    }

    public void softDeleteById(Integer id) {
        Optional<KhachHang> khachhangOpt = khachHangRepo.findById(id);
        if (khachhangOpt.isPresent()) {
            KhachHang khachhang = khachhangOpt.get();
            // CHỈ THAY ĐỔI TRẠNG THÁI
            khachhang.setTrangThai(0); // Đặt trạng thái về 0 (Đã xóa/Không hoạt động)
            khachHangRepo.save(khachhang);
        } else {
            // Ném lỗi nếu không tìm thấy, để Controller trả về 404
            throw new RuntimeException("Không tìm thấy khách hàng với ID: " + id);
        }
    }

}
