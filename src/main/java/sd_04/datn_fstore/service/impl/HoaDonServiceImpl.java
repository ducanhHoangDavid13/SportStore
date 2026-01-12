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
import sd_04.datn_fstore.service.PhieuGiamgiaService;
import sd_04.datn_fstore.service.SanPhamService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HoaDonServiceImpl implements HoaDonService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final KhoService khoService;
    private final SanPhamService sanPhamService;
    private final PhieuGiamgiaService phieuGiamgiaService;

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

        // Logic hoàn kho: Nếu chuyển sang HỦY (5) hoặc GIAO THẤT BẠI (7)
        // Và đơn hàng cũ chưa hoàn thành (< 4)
        boolean isHuyOrThatBai = (newTrangThai == TT_DA_HUY || newTrangThai == TT_GIAO_THAT_BAI);
        boolean isChuaHoanThanh = (hoaDon.getTrangThai() < TT_HOAN_THANH);

        if (isHuyOrThatBai && isChuaHoanThanh) {
            List<HoaDonChiTiet> items = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);

            // Dùng Set để lưu ID sản phẩm cha (tránh tính lại nhiều lần nếu 1 đơn mua nhiều size của cùng 1 áo)
            Set<Integer> listIdCha = new HashSet<>();

            for (HoaDonChiTiet item : items) {
                // 1. Hoàn kho cho biến thể con
                khoService.hoanTonKho(item.getSanPhamChiTiet().getId(), item.getSoLuong());

                // 2. Lưu ID cha vào danh sách cần cập nhật
                listIdCha.add(item.getSanPhamChiTiet().getSanPham().getId());
            }

            // 3. --- QUAN TRỌNG: CẬP NHẬT LẠI TỔNG SỐ LƯỢNG CHA ---
            for (Integer idCha : listIdCha) {
                sanPhamService.updateTotalQuantity(idCha);
            }

            // 4. Hoàn lại Voucher (nếu có)
            if (hoaDon.getPhieuGiamGia() != null) {
                phieuGiamgiaService.incrementVoucher(hoaDon.getPhieuGiamGia());
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
    @Override
    public List<HoaDon> getHoaDonChoTaiQuay() {
        // Tham số 1: trangThai = 0 (Trạng thái hóa đơn chờ/treo)
        // Tham số 2: hinhThucBanHang = 1 (Bắt buộc là TẠI QUẦY)
        // -> Điều này giúp loại bỏ đơn Online (hinhThucBanHang = 0) dù nó cũng đang trạng thái 0.
        return hoaDonRepository.findByTrangThaiAndHinhThucBanHangOrderByNgayTaoDesc(0, 1);
    }


    @Override
    @Transactional
    public void updatePaymentStatus(Long orderId, Integer newStatus) {
        Integer id = orderId.intValue(); // Chuyển đổi ID

        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn ID: " + id));

        // Nếu trạng thái hiện tại đã là Hoàn thành (4) hoặc Giao thất bại (7) thì không sửa nữa
        // Để tránh xung đột logic
        int currentStatus = hoaDon.getTrangThai();
        if (currentStatus == 4 || currentStatus == 5 || currentStatus == 7) {
            return;
        }

        // 1. Nếu trạng thái mới là ĐÃ XÁC NHẬN (Thành công)
        if (newStatus == 1) {
            hoaDon.setTrangThai(1);
            hoaDon.setNgayTao(LocalDateTime.now());
            hoaDonRepository.save(hoaDon);
        }
        // 2. Nếu trạng thái mới là ĐÃ HỦY (Khách bấm hủy)
        else if (newStatus == 5) {
            // Gọi hàm updateTrangThai cũ của bạn để nó TỰ ĐỘNG HOÀN KHO
            this.updateTrangThai(id, 5);
        }
        // 3. Nếu là CHỜ THANH TOÁN (Lỗi hoặc chưa xong)
        else {
            hoaDon.setTrangThai(6); // Hoặc trạng thái chờ của bạn
            hoaDonRepository.save(hoaDon);
        }
    }
}