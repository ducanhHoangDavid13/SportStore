package sd_04.datn_fstore.controller.author;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Import các Model và DTO
import sd_04.datn_fstore.dto.ProductVariantDTO;
import sd_04.datn_fstore.model.HinhAnh;
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
        // (Lưu vào biến tạm tenHinhAnhChinh để dùng chung cho cả cha và con)
        List<HinhAnh> hinhAnhList = sp.getHinhAnh();
        if (hinhAnhList != null && !hinhAnhList.isEmpty()) {
            sp.setTenHinhAnhChinh(hinhAnhList.get(0).getTenHinhAnh());
        } else {
            sp.setTenHinhAnhChinh("default.png");
        }

        // 3. LẤY DANH SÁCH BIẾN THỂ TỪ DB
        List<SanPhamChiTiet> listEntity = sanPhamCTService.getBySanPhamId(id);

        // 4. CHUYỂN ĐỔI ENTITY -> DTO (SỬA LỖI TẠI ĐÂY)
        List<ProductVariantDTO> listVariantDTO = listEntity.stream()
                .map(spct -> new ProductVariantDTO(
                        spct.getId(),
                        spct.getMauSac() != null ? spct.getMauSac().getTenMauSac() : "",
                        spct.getKichThuoc() != null ? spct.getKichThuoc().getTenKichThuoc() : "",
                        spct.getGiaTien(),
                        spct.getSoLuong(),
                        // 🛠️ SỬA LỖI: Lấy ảnh từ sản phẩm cha (sp) thay vì spct
                        sp.getTenHinhAnhChinh()
                ))
                .collect(Collectors.toList());

        // 5. LẤY DANH SÁCH MÀU VÀ SIZE DUY NHẤT
        Set<String> listMau = listVariantDTO.stream()
                .map(ProductVariantDTO::getTenMau)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        Set<String> listSize = listVariantDTO.stream()
                .map(ProductVariantDTO::getTenSize)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        // 6. ĐẨY DỮ LIỆU SANG VIEW
        model.addAttribute("product", sp);
        model.addAttribute("variantsJSON", listVariantDTO);
        model.addAttribute("listMau", listMau);
        model.addAttribute("listSize", listSize);

        return "view/author/sanPhamChiTiet";
    }
}