package sd_04.datn_fstore.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.repository.HoaDonRepository;

import java.util.List;

@Service
public class DonHangCuaToiService {

    @Autowired
    private HoaDonRepository hoaDonRepository;

    public Page<HoaDon> getHoaDonByKhachHang(Integer idKhachHang, List<Integer> trangThai, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "ngayTao");
        Pageable pageable = PageRequest.of(page, size, sort);

        // Kiểm tra nếu danh sách trangThai không rỗng
        if (trangThai != null && !trangThai.isEmpty()) {
            // Sử dụng phương thức tìm kiếm theo danh sách (IN)
            return hoaDonRepository.findByKhachHang_IdAndTrangThaiIn(idKhachHang, trangThai, pageable);
        } else {
            return hoaDonRepository.findByKhachHang_Id(idKhachHang, pageable);
        }
    }

    public HoaDon updateOrderStatus(Integer id, Integer newStatus) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hóa đơn với ID: " + id));

        // Kiểm tra logic nghiệp vụ (Ví dụ)
        if (newStatus == 5 && hoaDon.getTrangThai() != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ có thể hủy đơn hàng khi đang ở trạng thái Chờ xác nhận.");
        }

        if (newStatus == 4 && hoaDon.getTrangThai() != 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ có thể xác nhận đã nhận hàng khi đơn hàng đang Giao.");
        }

        hoaDon.setTrangThai(newStatus);
        return hoaDonRepository.save(hoaDon);
    }
}
