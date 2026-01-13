package sd_04.datn_fstore.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.model.HoaDonChiTiet;
import sd_04.datn_fstore.model.SanPham;
import sd_04.datn_fstore.model.SanPhamChiTiet;
import sd_04.datn_fstore.repository.HoaDonRepository;
import sd_04.datn_fstore.repository.SanPhamCTRepository;
import sd_04.datn_fstore.repository.SanPhamRepository;

import java.util.List;

@Service
public class DonHangCuaToiService {

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private SanPhamCTRepository sanPhamChiTietRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository; // Inject thêm để cập nhật tổng số lượng sản phẩm cha

    public Page<HoaDon> getHoaDonByKhachHang(Integer idKhachHang, List<Integer> trangThai, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "ngayTao");
        Pageable pageable = PageRequest.of(page, size, sort);

        if (trangThai != null && !trangThai.isEmpty()) {
            return hoaDonRepository.findByKhachHang_IdAndTrangThaiIn(idKhachHang, trangThai, pageable);
        } else {
            return hoaDonRepository.findByKhachHang_Id(idKhachHang, pageable);
        }
    }

    @Transactional
    public HoaDon updateOrderStatus(Integer id, Integer newStatus) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hóa đơn với ID: " + id));

        // LOGIC HỦY ĐƠN: Trạng thái 0 (Chờ xác nhận) -> 5 (Hủy)
        if (newStatus == 5) {
            if (hoaDon.getTrangThai() != 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ có thể hủy đơn hàng khi đang ở trạng thái Chờ xác nhận.");
            }

            List<HoaDonChiTiet> chiTiets = hoaDon.getHoaDonChiTiets();
            if (chiTiets != null && !chiTiets.isEmpty()) {
                for (HoaDonChiTiet ct : chiTiets) {
                    SanPhamChiTiet spct = ct.getSanPhamChiTiet();
                    if (spct != null) {
                        int soLuongHuy = ct.getSoLuong();

                        // 1. Hoàn số lượng cho SanPhamChiTiet (biến thể)
                        spct.setSoLuong(spct.getSoLuong() + soLuongHuy);
                        sanPhamChiTietRepository.save(spct);

                        // 2. Hoàn số lượng cho SanPham (sản phẩm cha)
                        SanPham sp = spct.getSanPham(); // Lấy sản phẩm cha từ SPCT
                        if (sp != null) {
                            sp.setSoLuong(sp.getSoLuong() + soLuongHuy);
                            sanPhamRepository.save(sp);
                        }
                    }
                }
            }
        }

        // LOGIC XÁC NHẬN NHẬN HÀNG: Trạng thái 3 -> 4
        if (newStatus == 4 && hoaDon.getTrangThai() != 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ có thể xác nhận đã nhận hàng khi đơn hàng đang Giao.");
        }

        hoaDon.setTrangThai(newStatus);
        return hoaDonRepository.save(hoaDon);
    }
}
