package sd_04.datn_fstore.controller.author;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sd_04.datn_fstore.model.SanPham;
import sd_04.datn_fstore.repository.SanPhamRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/san-pham")
@RequiredArgsConstructor
public class SanPhamUser {

    @Autowired
    private final SanPhamRepository sanPhamRepository;

    @GetMapping
    public String home(
            Model model,
            @RequestParam(value = "xuatXuIds", required = false) List<Integer> xuatXuIds,
            @RequestParam(value = "theLoaiIds", required = false) List<Integer> theLoaiIds,
            @RequestParam(value = "phanLoaiIds", required = false) List<Integer> phanLoaiIds,
            @RequestParam(value = "chatLieuIds", required = false) List<Integer> chatLieuIds,
            @RequestParam(value = "minPrice", required = false, defaultValue = "0") BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false, defaultValue = "999999999") BigDecimal maxPrice,
            // 🌟 BỔ SUNG KEYWORD VÀO ĐÂY
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort // Đổi default sort để khớp với JS
    ) {
        // 1. Xử lý logic List rỗng
        List<Integer> finalXuatXuIds = Optional.ofNullable(xuatXuIds).filter(list -> !list.isEmpty()).orElse(null);
        List<Integer> finalTheLoaiIds = Optional.ofNullable(theLoaiIds).filter(list -> !list.isEmpty()).orElse(null);
        List<Integer> finalPhanLoaiIds = Optional.ofNullable(phanLoaiIds).filter(list -> !list.isEmpty()).orElse(null);
        List<Integer> finalChatLieuIds = Optional.ofNullable(chatLieuIds).filter(list -> !list.isEmpty()).orElse(null);

        // 🌟 XỬ LÝ KEYWORD (Giữ nguyên cho tiện lợi, vì API Rest sẽ dùng hàm riêng)
        String finalKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

        // 2. Xử lý tham số sort và Pageable
        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        // 3. Gọi Repository với tham số keyword
        Page<SanPham> productsPage = sanPhamRepository.findFilteredProducts(
                finalXuatXuIds,
                finalTheLoaiIds,
                finalPhanLoaiIds,
                finalChatLieuIds,
                minPrice,
                maxPrice,
                finalKeyword, // 🌟 TRUYỀN KEYWORD VÀO ĐÂY
                pageable);

        // 4. & 5. Đưa dữ liệu và tham số lọc vào Model
        model.addAttribute("productsPage", productsPage);
        model.addAttribute("xuatXuIds", xuatXuIds);
        model.addAttribute("theLoaiIds", theLoaiIds);
        model.addAttribute("phanLoaiIds", phanLoaiIds);
        model.addAttribute("chatLieuIds", chatLieuIds);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("keyword", keyword); // 🌟 ĐẨY KEYWORD VÀO MODEL CHO THANH SEARCH

        return "view/author/sanphamkh";
    }
}