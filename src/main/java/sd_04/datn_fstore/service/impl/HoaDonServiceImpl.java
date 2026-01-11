package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.model.HoaDonChiTiet;
import sd_04.datn_fstore.repository.HoaDonRepository;
import sd_04.datn_fstore.repository.HoaDonChiTietRepository;
import sd_04.datn_fstore.service.HoaDonService;
import sd_04.datn_fstore.service.KhoService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HoaDonServiceImpl implements HoaDonService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final KhoService khoService;

    private static final int TT_HOAN_THANH = 4;
    private static final int TT_DA_HUY = 5;
    private static final int TT_GIAO_THAT_BAI = 7;

    @Override
    public Page<HoaDon> search(Pageable pageable, List<Integer> trangThaiList,
                               LocalDateTime ngayBatDau, LocalDateTime ngayKetThuc, String keyword,
                               BigDecimal minPrice, BigDecimal maxPrice) {

        return hoaDonRepository.searchByTrangThaiAndNgayTao(
                pageable, trangThaiList, ngayBatDau, ngayKetThuc, keyword,
                minPrice, maxPrice
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
    @Transactional
    public void updateTrangThai(Integer hoaDonId, Integer newTrangThai) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy HĐ ID: " + hoaDonId));

        boolean isHuyOrThatBai = (newTrangThai == TT_DA_HUY || newTrangThai == TT_GIAO_THAT_BAI);
        boolean isChuaHoanThanh = (hoaDon.getTrangThai() < TT_HOAN_THANH);
        // Logic này đảm bảo: Nếu chuyển sang HỦY (5) thì mới hoàn kho
        if (isHuyOrThatBai && isChuaHoanThanh) {
            List<HoaDonChiTiet> items = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
            for (HoaDonChiTiet item : items) {
                // Gọi service trả hàng về kho
                khoService.hoanTonKho(
                        item.getSanPhamChiTiet().getId(),
                        item.getSoLuong()
                );
            }
        }

        hoaDon.setTrangThai(newTrangThai);
        hoaDonRepository.save(hoaDon);
    }

    // --- THÊM CÁC HÀM MỚI TỪ INTERFACE ---

    @Override
    public List<HoaDon> getAll() {
        return hoaDonRepository.findAll(Sort.by(Sort.Direction.DESC, "ngayTao"));
    }

    @Override
    public List<HoaDon> getByTrangThai(Integer trangThai) {
        return hoaDonRepository.findByTrangThaiOrderByNgayTaoDesc(trangThai);
    }

    @Override
    public List<HoaDon> getByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        return hoaDonRepository.findByNgayTaoBetweenOrderByNgayTaoDesc(startTime, endTime);
    }
    @Override
    @Transactional // Quan trọng: Đảm bảo tính toàn vẹn dữ liệu
    public void deleteByMaHoaDon(String maHoaDon) {
        // Bước 1: Kiểm tra xem hóa đơn có tồn tại không
        HoaDon hoaDon = hoaDonRepository.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn có mã: " + maHoaDon));

        // Bước 2: Kiểm tra điều kiện (Optional)
        // Ví dụ: Chỉ cho xóa nếu là hóa đơn chờ (chưa thanh toán)
        if (hoaDon.getTrangThai() == 1) { // Giả sử 1 là đã thanh toán
            throw new RuntimeException("Không thể xóa hóa đơn đã thanh toán!");
        }

        // Bước 3: Xóa hóa đơn
        // Nếu trong Entity bạn đã cấu hình CascadeType.ALL hoặc CascadeType.REMOVE
        // thì Hóa đơn chi tiết sẽ tự động bị xóa theo.
        hoaDonRepository.delete(hoaDon);

        // Nếu không cấu hình Cascade, bạn phải xóa chi tiết trước:
        // hoaDonChiTietRepository.deleteByHoaDonId(hoaDon.getId());
        // hoaDonRepository.delete(hoaDon);
    }
}