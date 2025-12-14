package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.model.*;
import sd_04.datn_fstore.repository.*;
import sd_04.datn_fstore.service.HinhAnhService;
import sd_04.datn_fstore.service.SanPhamCTService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SanPhamCTServiceImpl implements SanPhamCTService {

    private final SanPhamCTRepository sanPhamChiTietRepository;
    private final HinhAnhService hinhAnhService;
    private final SanPhamRepository sanPhamRepository;

    // ********** KHAI BÁO CÁC REPOSITORY KHÓA NGOẠI BẮT BUỘC **********
    private final MauSacRepository mauSacRepository;
    private final KichThuocRepository kichThuocRepository;
    private final ChatLieuRepository chatLieuRepository;
    private final XuatXuRepository xuatXuRepository;
    private final TheLoaiRepository theLoaiRepository;
    private final PhanLoaiRepository phanLoaiRepository;
    // *************************************************************************

    @Override
    public List<SanPhamChiTiet> getAll() {
        // Sử dụng phương thức repository đã được tối ưu JOIN FETCH nếu có
        List<SanPhamChiTiet> list = sanPhamChiTietRepository.findAll();
        list.forEach(this::loadTenHinhAnhChinh);
        return list;
    }

    @Override
    public Page<SanPhamChiTiet> getAll(Pageable pageable) {
        Page<SanPhamChiTiet> page = sanPhamChiTietRepository.findAll(pageable);
        page.getContent().forEach(this::loadTenHinhAnhChinh);
        return page;
    }

    @Override
    public Optional<SanPhamChiTiet> getById(Integer id) {
        Optional<SanPhamChiTiet> optSpct = sanPhamChiTietRepository.findById(id);
        optSpct.ifPresent(this::loadTenHinhAnhChinh);
        return optSpct;
    }

    /**
     * Hàm lưu/cập nhật biến thể sản phẩm.
     * BỔ SUNG: Logic tự động chuyển trạng thái Ngừng bán -> Đang bán nếu tồn kho tăng (> 0).
     */
    @Override
    @Transactional
    public SanPhamChiTiet save(SanPhamChiTiet sanPhamChiTiet) {
        boolean isAdding = sanPhamChiTiet.getId() == null;

        // --- 1. GÁN LẠI ENTITY CHO TẤT CẢ CÁC KHÓA NGOẠI (HYDRATE + GÁN ID THÔ) ---
        hydrateForeignKeys(sanPhamChiTiet);

        // --- 2. XỬ LÝ KHI THÊM MỚI (Tạo mã SPCT và trạng thái) ---
        if (isAdding) {
            sanPhamChiTiet.setTrangThai(1);
            if (sanPhamChiTiet.getMaSanPhamChiTiet() == null || sanPhamChiTiet.getMaSanPhamChiTiet().isEmpty()) {
                sanPhamChiTiet.setMaSanPhamChiTiet("SPCT" + System.currentTimeMillis());
            }
        } else {
            // Khi CẬP NHẬT: Lấy lại trạng thái HIỆN TẠI trong DB để xử lý logic tồn kho/trạng thái
            SanPhamChiTiet existingSpct = sanPhamChiTietRepository.findById(sanPhamChiTiet.getId()).orElse(null);
            if (existingSpct != null) {
                if (sanPhamChiTiet.getMaSanPhamChiTiet() == null) {
                    sanPhamChiTiet.setMaSanPhamChiTiet(existingSpct.getMaSanPhamChiTiet());
                }
                if (sanPhamChiTiet.getTrangThai() == null) {
                    sanPhamChiTiet.setTrangThai(existingSpct.getTrangThai());
                }
            }
        }

        // Lấy giá từ SP Cha (thực hiện sau khi hydrate SanPham)
        if (sanPhamChiTiet.getSanPham() != null) {
            sanPhamChiTiet.setGiaTien(sanPhamChiTiet.getSanPham().getGiaTien());
        }

        // ********** LOGIC TỰ ĐỘNG CẬP NHẬT TRẠNG THÁI THEO TỒN KHO **********
        if (sanPhamChiTiet.getSoLuong() != null) {
            if (sanPhamChiTiet.getSoLuong() <= 0) {
                sanPhamChiTiet.setTrangThai(0); // Hết hàng -> Ngừng bán
            } else if (sanPhamChiTiet.getSoLuong() > 0 && sanPhamChiTiet.getTrangThai() == 0) {
                // Nếu tồn kho > 0 VÀ đang ở trạng thái Ngừng bán (do hết hàng trước đó) -> Kích hoạt lại
                sanPhamChiTiet.setTrangThai(1); // Set Đang bán
            }
        }
        // *****************************************************************************

        // --- 3. LƯU VÀO DB VÀ CẬP NHẬT TỔNG SL ---
        SanPhamChiTiet savedSpct = sanPhamChiTietRepository.save(sanPhamChiTiet);

        if (savedSpct.getSanPham() != null) {
            updateTotalQuantitySanPham(savedSpct.getSanPham().getId());
        }
        return savedSpct;
    }

    /**
     * Hàm phụ trách Hydrate 7 Entity Khóa ngoại và GÁN GIÁ TRỊ ID THÔ.
     * Đây là bước bắt buộc để ghi ID vào các cột DB do Model bị chặn ghi Entity.
     */
    private void hydrateForeignKeys(SanPhamChiTiet sanPhamChiTiet) {
        // GÁN SanPham (SP Cha) - Bắt buộc
        if (sanPhamChiTiet.getSanPham() != null && sanPhamChiTiet.getSanPham().getId() != null) {
            SanPham spCha = sanPhamRepository.findById(sanPhamChiTiet.getSanPham().getId()).orElseThrow(
                    () -> new RuntimeException("Lỗi tham chiếu: Không tìm thấy Sản phẩm gốc ID: " + sanPhamChiTiet.getSanPham().getId()));
            sanPhamChiTiet.setSanPham(spCha);
            sanPhamChiTiet.setIdSanPham(spCha.getId()); // <-- BẮT BUỘC: GÁN ID THÔ
        } else {
            throw new RuntimeException("Sản phẩm gốc (SanPham) là bắt buộc.");
        }

        // GÁN MauSac - Bắt buộc
        if (sanPhamChiTiet.getMauSac() != null && sanPhamChiTiet.getMauSac().getId() != null) {
            MauSac ms = mauSacRepository.findById(sanPhamChiTiet.getMauSac().getId()).orElseThrow(
                    () -> new RuntimeException("Lỗi tham chiếu: Không tìm thấy Màu sắc ID: " + sanPhamChiTiet.getMauSac().getId()));
            sanPhamChiTiet.setMauSac(ms);
            sanPhamChiTiet.setIdMauSac(ms.getId()); // <-- BẮT BUỘC: GÁN ID THÔ
        } else {
            throw new RuntimeException("Màu sắc là bắt buộc.");
        }

        // GÁN KichThuoc - Bắt buộc
        if (sanPhamChiTiet.getKichThuoc() != null && sanPhamChiTiet.getKichThuoc().getId() != null) {
            KichThuoc kt = kichThuocRepository.findById(sanPhamChiTiet.getKichThuoc().getId()).orElseThrow(
                    () -> new RuntimeException("Lỗi tham chiếu: Không tìm thấy Kích thước ID: " + sanPhamChiTiet.getKichThuoc().getId()));
            sanPhamChiTiet.setKichThuoc(kt);
            sanPhamChiTiet.setIdKichThuoc(kt.getId()); // <-- BẮT BUỘC: GÁN ID THÔ
        } else {
            throw new RuntimeException("Kích thước là bắt buộc.");
        }

        // GÁN ChatLieu - Bắt buộc
        if (sanPhamChiTiet.getChatLieu() != null && sanPhamChiTiet.getChatLieu().getId() != null) {
            ChatLieu cl = chatLieuRepository.findById(sanPhamChiTiet.getChatLieu().getId()).orElseThrow(
                    () -> new RuntimeException("Lỗi tham chiếu: Không tìm thấy Chất liệu ID: " + sanPhamChiTiet.getChatLieu().getId()));
            sanPhamChiTiet.setChatLieu(cl);
            sanPhamChiTiet.setIdChatLieu(cl.getId()); // <-- BẮT BUỘC: GÁN ID THÔ
        } else {
            throw new RuntimeException("Chất liệu là bắt buộc.");
        }

        // GÁN XuatXu - Bắt buộc
        if (sanPhamChiTiet.getXuatXu() != null && sanPhamChiTiet.getXuatXu().getId() != null) {
            XuatXu xx = xuatXuRepository.findById(sanPhamChiTiet.getXuatXu().getId()).orElseThrow(
                    () -> new RuntimeException("Lỗi tham chiếu: Không tìm thấy Xuất xứ ID: " + sanPhamChiTiet.getXuatXu().getId()));
            sanPhamChiTiet.setXuatXu(xx);
            sanPhamChiTiet.setIdXuatXu(xx.getId()); // <-- BẮT BUỘC: GÁN ID THÔ
        } else {
            throw new RuntimeException("Xuất xứ là bắt buộc.");
        }

        // GÁN TheLoai - Bắt buộc
        if (sanPhamChiTiet.getTheLoai() != null && sanPhamChiTiet.getTheLoai().getId() != null) {
            TheLoai tl = theLoaiRepository.findById(sanPhamChiTiet.getTheLoai().getId()).orElseThrow(
                    () -> new RuntimeException("Lỗi tham chiếu: Không tìm thấy Thể loại ID: " + sanPhamChiTiet.getTheLoai().getId()));
            sanPhamChiTiet.setTheLoai(tl);
            sanPhamChiTiet.setIdTheLoai(tl.getId()); // <-- BẮT BUỘC: GÁN ID THÔ
        } else {
            throw new RuntimeException("Thể loại là bắt buộc.");
        }

        // GÁN PhanLoai - Bắt buộc
        if (sanPhamChiTiet.getPhanLoai() != null && sanPhamChiTiet.getPhanLoai().getId() != null) {
            PhanLoai pl = phanLoaiRepository.findById(sanPhamChiTiet.getPhanLoai().getId()).orElseThrow(
                    () -> new RuntimeException("Lỗi tham chiếu: Không tìm thấy Phân loại ID: " + sanPhamChiTiet.getPhanLoai().getId()));
            sanPhamChiTiet.setPhanLoai(pl);
            sanPhamChiTiet.setIdPhanLoai(pl.getId()); // <-- BẮT BUỘC: GÁN ID THÔ
        } else {
            throw new RuntimeException("Phân loại là bắt buộc.");
        }
    }
    // *****************************************************************************

    @Override
    @Transactional
    public void delete(Integer id) {
        Optional<SanPhamChiTiet> optional = sanPhamChiTietRepository.findById(id);
        if (optional.isPresent()) {
            SanPhamChiTiet spct = optional.get();
            Integer sanPhamId = spct.getSanPham().getId();

            spct.setTrangThai(0); // Soft delete
            sanPhamChiTietRepository.save(spct);
            updateTotalQuantitySanPham(sanPhamId);
        }
    }

    @Override
    @Transactional
    public SanPhamChiTiet updateTrangThai(Integer id, Integer newStatus) {
        Optional<SanPhamChiTiet> optional = sanPhamChiTietRepository.findById(id);
        if (optional.isPresent()) {
            SanPhamChiTiet spct = optional.get();
            spct.setTrangThai(newStatus);
            SanPhamChiTiet saved = sanPhamChiTietRepository.save(spct);
            updateTotalQuantitySanPham(spct.getSanPham().getId());
            return saved;
        } else {
            throw new RuntimeException("Không tìm thấy biến thể sản phẩm ID: " + id);
        }
    }

    @Override
    public Page<SanPhamChiTiet> search(
            Pageable pageable,
            Integer idSanPham,
            Integer idKichThuoc,
            Integer idChatLieu,
            Integer idTheLoai,
            Integer idXuatXu,
            Integer idMauSac,
            Integer idPhanLoai,
            Integer trangThai,
            String keyword
    ) {
        Page<SanPhamChiTiet> page = sanPhamChiTietRepository.search(
                pageable,
                idSanPham,
                idKichThuoc,
                idChatLieu,
                idTheLoai,
                idXuatXu,
                idMauSac,
                idPhanLoai,
                trangThai,
                keyword
        );
        page.getContent().forEach(this::loadTenHinhAnhChinh);
        return page;
    }

    @Override
    public List<SanPhamChiTiet> getAvailableProducts(Integer idSanPham) {
        List<SanPhamChiTiet> list = sanPhamChiTietRepository.findAvailableVariants(idSanPham);
        list.forEach(this::loadTenHinhAnhChinh);
        return list;
    }

    @Override
    public List<SanPhamChiTiet> getAllActive() {
        List<SanPhamChiTiet> list = sanPhamChiTietRepository.getAvailableProductsWithDetails(1, 0);
        list.forEach(this::loadTenHinhAnhChinh);
        return list;
    }

    @Override
    public List<SanPhamChiTiet> timTheoKhoangGia(BigDecimal maxPrice) {
        return sanPhamChiTietRepository.findBySanPham_GiaTienLessThanEqual(maxPrice);
    }

    @Override
    public List<SanPhamChiTiet> getBySanPhamId(Integer idSanPham) {
        List<SanPhamChiTiet> list = sanPhamChiTietRepository.findBySanPhamId(idSanPham);
        list.forEach(this::loadTenHinhAnhChinh);
        return list;
    }

    @Override
    public List<SanPhamChiTiet> searchBySanPhamTen(String tenSp) {
        List<SanPhamChiTiet> list = sanPhamChiTietRepository.findBySanPhamTenSanPham(tenSp);
        list.forEach(this::loadTenHinhAnhChinh);
        return list;
    }

    @Override
    public void updateBatchTotalQuantity(List<SanPham> sanPhamList) {
        for (SanPham sanPham : sanPhamList) {
            int total = 0;
            List<SanPhamChiTiet> variants = sanPhamChiTietRepository.findBySanPhamId(sanPham.getId());

            for (SanPhamChiTiet ct : variants) {
                if (ct.getTrangThai() == 1 && ct.getSoLuong() != null) {
                    total += ct.getSoLuong();
                }
            }
            sanPham.setSoLuong(total);
        }
    }

    private void updateTotalQuantitySanPham(Integer sanPhamId) {
        SanPham sanPham = sanPhamRepository.findById(sanPhamId).orElse(null);
        if (sanPham != null) {
            int total = 0;
            List<SanPhamChiTiet> variants = sanPhamChiTietRepository.findBySanPhamId(sanPhamId);
            for (SanPhamChiTiet ct : variants) {
                if (ct.getTrangThai() == 1 && ct.getSoLuong() != null) {
                    total += ct.getSoLuong();
                }
            }
            sanPham.setSoLuong(total);
            sanPhamRepository.save(sanPham);
        }
    }

    private void loadTenHinhAnhChinh(SanPhamChiTiet spct) {
        if (spct.getSanPham() == null) return;
        SanPham sanPhamCha = spct.getSanPham();
        Integer sanPhamId = sanPhamCha.getId();

        Optional<HinhAnh> avatarOpt = hinhAnhService.getAvatar(sanPhamId);
        if (avatarOpt.isPresent()) {
            sanPhamCha.setTenHinhAnhChinh(avatarOpt.get().getTenHinhAnh());
        } else {
            List<HinhAnh> allImages = hinhAnhService.getBySanPhamId(sanPhamId);
            if (!allImages.isEmpty()) {
                sanPhamCha.setTenHinhAnhChinh(allImages.get(0).getTenHinhAnh());
            }
        }
    }
}