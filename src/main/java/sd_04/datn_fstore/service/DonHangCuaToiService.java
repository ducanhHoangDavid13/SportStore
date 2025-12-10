package sd_04.datn_fstore.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.repository.HoaDonRepository;

@Service
public class DonHangCuaToiService {

    @Autowired
    private HoaDonRepository hoaDonRepository;

    public Page<HoaDon> getHoaDonByKhachHang(Integer idKhachHang, Integer trangThai, int page, int size) {
        // Sắp xếp theo 'ngayTao' (ngày tạo) giảm dần (DESC)
        Sort sort = Sort.by(Sort.Direction.DESC, "ngayTao");

        // Tạo đối tượng Pageable
        Pageable pageable = PageRequest.of(page, size, sort);

        if (trangThai != null) {
            // Có lọc theo trạng thái
            return hoaDonRepository.findByKhachHang_IdAndTrangThai(idKhachHang, trangThai, pageable);
        } else {
            // Không lọc theo trạng thái (lấy tất cả)
            return hoaDonRepository.findByKhachHang_Id(idKhachHang, pageable);
        }
    }
}
