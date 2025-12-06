package sd_04.datn_fstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sd_04.datn_fstore.model.SanPhamChiTiet;
import sd_04.datn_fstore.service.*;

@Controller
@RequestMapping("/admin/san-pham-chi-tiet")
@RequiredArgsConstructor
public class SanPhamChiTietController {

    private final SanPhamCTService sanPhamCTService;

    private final SanPhamService sanPhamService;
    private final MauSacService mauSacService;
    private final KichThuocService kichThuocService;
    private final ChatLieuService chatLieuService;
    private final TheLoaiService theLoaiService;
    private final XuatXuService xuatXuService;
    private final PhanLoaiService phanLoaiService;

    @GetMapping
    public String hienThiTrang(Model model,
                               @PageableDefault(size = 1000) Pageable pageable,
                               @RequestParam(required = false) Integer idSanPham,
                               @RequestParam(required = false) Integer idMauSac,
                               @RequestParam(required = false) Integer idKichThuoc,
                               @RequestParam(required = false) Integer idChatLieu,
                               @RequestParam(required = false) Integer idTheLoai,
                               @RequestParam(required = false) Integer idXuatXu,
                               @RequestParam(required = false) Integer idPhanLoai,
                               @RequestParam(required = false) Integer trangThai,
                               @RequestParam(required = false) String keyword
                               // ĐÃ XÓA minPrice, maxPrice
    ) {

        // Gọi hàm search mới (không có giá)
        Page<SanPhamChiTiet> spctPage = sanPhamCTService.search(
                pageable,
                idSanPham, idKichThuoc, idChatLieu,
                idTheLoai, idXuatXu, idMauSac, idPhanLoai,
                trangThai,
                keyword
        );
        model.addAttribute("spctPage", spctPage);
        model.addAttribute("spct", new SanPhamChiTiet());
        loadFormDependencies(model);

        // Giữ lại giá trị lọc để hiển thị trên View
        model.addAttribute("idSanPham", idSanPham);
        model.addAttribute("idMauSac", idMauSac);
        // ... thêm các attribute khác nếu cần

        return "view/admin/sanPhamCT";
    }

    private void loadFormDependencies(Model model) {
        model.addAttribute("listSanPham", sanPhamService.getAll());
        model.addAttribute("listMauSac", mauSacService.getAll());
        model.addAttribute("listKichThuoc", kichThuocService.getAll());
        model.addAttribute("listChatLieu", chatLieuService.getAll());
        model.addAttribute("listTheLoai", theLoaiService.getAll());
        model.addAttribute("listXuatXu", xuatXuService.getAll());
        model.addAttribute("listPhanLoai", phanLoaiService.getAll());
    }
}