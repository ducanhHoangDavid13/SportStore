package sd_04.datn_fstore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.model.HoaDonChiTiet;
import sd_04.datn_fstore.repository.HoaDonRepository;
import sd_04.datn_fstore.repository.HoaDonChiTietRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HoaDonServiceImpl implements HoaDonService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final KhoService khoService; // Dịch vụ Lõi

    // Định nghĩa các trạng thái (Theo ảnh của bạn)
    private static final int TT_HOAN_THANH = 5;
    private static final int TT_DA_HUY = 6;

    @Override
    public Page<HoaDon> search(Pageable pageable, List<Integer> trangThaiList,
                               LocalDateTime ngayBatDau, LocalDateTime ngayKetThuc, String keyword) {

        return hoaDonRepository.searchByTrangThaiAndNgayTao(
                pageable, trangThaiList, ngayBatDau, ngayKetThuc, keyword
        );
    }

    @Override
    public Optional<HoaDon> getById(Integer id) {
        return hoaDonRepository.findById(id);
    }

    @Override
    public HoaDon add(HoaDon hoaDon) {
        if (hoaDon.getNgayTao() == null) {
            hoaDon.setNgayTao(LocalDateTime.now());
        }
        return hoaDonRepository.save(hoaDon);
    }

    @Override
    @Transactional // Quan trọng: Đảm bảo Hủy đơn và Hoàn kho cùng lúc
    public void updateTrangThai(Integer hoaDonId, Integer newTrangThai) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy HĐ ID: " + hoaDonId));

        // Logic nghiệp vụ: Hoàn kho khi HỦY ĐƠN (chỉ khi đơn chưa hoàn thành)
        if (newTrangThai == TT_DA_HUY && hoaDon.getTrangThai() < TT_HOAN_THANH) {

            // Lấy tất cả sản phẩm trong hóa đơn (Sử dụng Repo HDCT)
            List<HoaDonChiTiet> items = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);

            for (HoaDonChiTiet item : items) {
                // Gọi service Kho để hoàn kho
                khoService.hoanTonKho(
                        item.getSanPhamChiTiet().getId(),
                        item.getSoLuong()
                );
            }
        }

        // Cập nhật trạng thái cuối cùng
        hoaDon.setTrangThai(newTrangThai);
        hoaDonRepository.save(hoaDon);
    }
}