package sd_04.datn_fstore.controller.author;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Import các Model và DTO
import sd_04.datn_fstore.model.HinhAnh;
import sd_04.datn_fstore.model.KichThuoc; // ⬅️ IMPORT KICHCO
import sd_04.datn_fstore.model.MauSac; // ⬅️ IMPORT MAUSAC
import sd_04.datn_fstore.model.SanPham;
import sd_04.datn_fstore.model.SanPhamChiTiet;

import sd_04.datn_fstore.service.SanPhamService;
import sd_04.datn_fstore.service.SanPhamCTService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SPCTController {

    private final SanPhamService sanPhamService;
    private final SanPhamCTService sanPhamCTService;

    @GetMapping("/san-pham/chi-tiet/{id}")
    @Transactional
    public String chiTietSanPham(@PathVariable Integer id, Model model) {

        // 1. LẤY SẢN PHẨM CHA
        Optional<SanPham> optionalSp = sanPhamService.getById(id);
        if (optionalSp.isEmpty()) {
            return "redirect:/san-pham?error=not_found";
        }
        SanPham sp = optionalSp.get();

        // 2. XỬ LÝ ẢNH ĐẠI DIỆN CHO SẢN PHẨM CHA
        List<HinhAnh> hinhAnhList = sp.getHinhAnh();
        if (hinhAnhList != null && !hinhAnhList.isEmpty()) {
            sp.setTenHinhAnhChinh(hinhAnhList.get(0).getTenHinhAnh());
        } else {
            sp.setTenHinhAnhChinh("default.png");
        }

        // 3. LẤY DANH SÁCH BIẾN THỂ TỪ DB
        // listEntity chính là "allSpctList"
        List<SanPhamChiTiet> listEntity = sanPhamCTService.getBySanPhamId(id);

        // 4. LẤY DANH SÁCH MÀU VÀ SIZE DUY NHẤT (DÙNG ENTITY CHỨ KHÔNG DÙNG CHUỖI)

        // Trích xuất MauSac Entity duy nhất
        Set<MauSac> listMauSac = listEntity.stream()
                .map(SanPhamChiTiet::getMauSac)
                .filter(mau -> mau != null)
                .collect(Collectors.toSet());

        // Trích xuất KichCo Entity duy nhất
        Set<KichThuoc> listKichCo = listEntity.stream()
                .map(SanPhamChiTiet::getKichThuoc)
                .filter(size -> size != null)
                .collect(Collectors.toSet());

        // 5. ĐẨY DỮ LIỆU SANG VIEW
        model.addAttribute("product", sp);

        // 5.1. Dữ liệu cần cho HTML để lặp qua nút (Phải là entity để lấy ID)
        model.addAttribute("listMauSac", listMauSac); // ⬅️ Sửa tên biến và loại dữ liệu
        model.addAttribute("listKichCo", listKichCo); // ⬅️ Sửa tên biến và loại dữ liệu

        // 5.2. Dữ liệu cần cho JavaScript để tìm kiếm SPCT (Entity list)
        model.addAttribute("allSpctList", listEntity); // ⬅️ Đẩy listEntity gốc (SPCT)

        // Bỏ qua listVariantDTO và variantsJSON nếu không còn cần thiết cho việc hiển thị
        // Nếu vẫn cần, bạn nên đổi tên biến để tránh nhầm lẫn.
        // model.addAttribute("variantsJSON", listVariantDTO);

        return "view/author/sanPhamChiTiet";
    }
}