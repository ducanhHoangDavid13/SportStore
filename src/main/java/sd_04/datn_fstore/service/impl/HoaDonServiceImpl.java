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

    private static final int TT_HOAN_THANH = 5;
    private static final int TT_DA_HUY = 6;

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

        if (newTrangThai == TT_DA_HUY && hoaDon.getTrangThai() < TT_HOAN_THANH) {
            List<HoaDonChiTiet> items = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
            for (HoaDonChiTiet item : items) {
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

}