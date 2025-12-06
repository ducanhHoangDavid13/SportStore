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
        // 1. Tìm hóa đơn, ném lỗi rõ ràng nếu không thấy
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn có ID: " + hoaDonId));

        Integer oldTrangThai = hoaDon.getTrangThai();

        // 2. Kiểm tra hợp lệ: Nếu hóa đơn đã hủy rồi thì không cho thao tác nữa (tránh lỗi cộng dồn kho)
        if (oldTrangThai.equals(TT_DA_HUY)) {
            throw new RuntimeException("Hóa đơn này đã bị hủy, không thể cập nhật trạng thái!");
        }

        // 3. Xử lý logic Hoàn Kho khi Hủy Đơn
        // Điều kiện: Trạng thái mới là HỦY và trạng thái cũ CHƯA HOÀN THÀNH (ví dụ: đang chờ, đang giao...)
        if (newTrangThai.equals(TT_DA_HUY) && oldTrangThai < TT_HOAN_THANH) {
            List<HoaDonChiTiet> listChiTiet = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);

            // Dùng forEach cho gọn
            listChiTiet.forEach(item -> {
                khoService.hoanTonKho(
                        item.getSanPhamChiTiet().getId(),
                        item.getSoLuong()
                );
            });
        }

        // 4. Cập nhật trạng thái mới
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
    // =========================================================================
    // 1. CHỨC NĂNG XÓA HÓA ĐƠN TREO (Kèm hoàn tồn kho)
    // =========================================================================
    @Override
    @Transactional // Bắt buộc để đảm bảo tính toàn vẹn dữ liệu
    public void deleteByMaHoaDon(String maHoaDon) {
        // 1. Tìm hóa đơn theo mã
        HoaDon hd = hoaDonRepository.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn có mã: " + maHoaDon));

        // 2. Lấy danh sách sản phẩm trong hóa đơn đó
        // LƯU Ý QUAN TRỌNG: Phải dùng hd.getId() (Integer), không dùng maHoaDon (String)
        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hd.getId());

        // 3. HOÀN TRẢ TỒN KHO (Logic quan trọng)
        // Trước khi xóa, phải trả lại số lượng sản phẩm vào kho
        for (HoaDonChiTiet item : chiTiets) {
            khoService.hoanTonKho(
                    item.getSanPhamChiTiet().getId(),
                    item.getSoLuong()
            );
        }

        // 4. Xóa dữ liệu
        hoaDonChiTietRepository.deleteAll(chiTiets); // Xóa chi tiết trước
        hoaDonRepository.delete(hd);                 // Xóa hóa đơn sau
    }
}