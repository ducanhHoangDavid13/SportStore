package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.model.SanPhamChiTiet;
import sd_04.datn_fstore.repository.SanPhamCTRepository;
import sd_04.datn_fstore.service.KhoService;

@Service
@RequiredArgsConstructor
public class KhoServiceImpl implements KhoService {

    private final SanPhamCTRepository sanPhamCTRepository;

    @Override
    @Transactional
    public void truTonKho(Integer sanPhamChiTietId, Integer soLuongBan) {
        SanPhamChiTiet spct = sanPhamCTRepository.findById(sanPhamChiTietId)
                .orElseThrow(() -> new RuntimeException("SPCT không tồn tại: " + sanPhamChiTietId));

        if (spct.getSoLuong() == null || spct.getSoLuong() < soLuongBan) {
            throw new RuntimeException("Không đủ tồn kho cho: " + spct.getSanPham().getTenSanPham());
        }

        spct.setSoLuong(spct.getSoLuong() - soLuongBan);
        sanPhamCTRepository.save(spct);
    }

    @Override
    @Transactional
    public void hoanTonKho(Integer sanPhamChiTietId, Integer soLuongTra) {
        SanPhamChiTiet spct = sanPhamCTRepository.findById(sanPhamChiTietId)
                .orElseThrow(() -> new RuntimeException("SPCT không tồn tại: " + sanPhamChiTietId));

        // Cộng trả lại số lượng
        spct.setSoLuong(spct.getSoLuong() + soLuongTra);
        sanPhamCTRepository.save(spct);
    }
}