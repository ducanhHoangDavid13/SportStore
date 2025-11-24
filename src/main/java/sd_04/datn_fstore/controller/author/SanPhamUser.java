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
// Bỏ import và sử dụng SanPhamService nếu nó không được dùng trong method này
// import sd_04.datn_fstore.service.SanPhamService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/san-pham")
@RequiredArgsConstructor
public class SanPhamUser {

    // private final SanPhamService sanPhamService; // Bỏ hoặc chú thích nếu không dùng

    // Tiêm Repository bằng constructor injection (cleaner) hoặc @Autowired trên trường
    @Autowired
    private final SanPhamRepository sanPhamRepository;
    @GetMapping
    public String home(
            Model model,
            @RequestParam(value = "xuatXuIds", required = false) List<Integer> xuatXuIds, // Sửa Long -> Integer
            @RequestParam(value = "theLoaiIds", required = false) List<Integer> theLoaiIds, // Sửa Long -> Integer
            @RequestParam(value = "phanLoaiIds", required = false) List<Integer> phanLoaiIds, // Sửa Long -> Integer
            @RequestParam(value = "chatLieuIds", required = false) List<Integer> chatLieuIds, // Sửa Long -> Integer
            @RequestParam(value = "minPrice", required = false, defaultValue = "0") BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false, defaultValue = "999999999") BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort
    ) {
        // 1. Xử lý logic List rỗng (Sử dụng Integer List)
        List<Integer> finalXuatXuIds = Optional.ofNullable(xuatXuIds).filter(list -> !list.isEmpty()).orElse(null);
        List<Integer> finalTheLoaiIds = Optional.ofNullable(theLoaiIds).filter(list -> !list.isEmpty()).orElse(null);
        List<Integer> finalPhanLoaiIds = Optional.ofNullable(phanLoaiIds).filter(list -> !list.isEmpty()).orElse(null);
        List<Integer> finalChatLieuIds = Optional.ofNullable(chatLieuIds).filter(list -> !list.isEmpty()).orElse(null);

        // 2. Xử lý tham số sort và Pageable (Giữ nguyên)
        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        // 3. Gọi Repository trực tiếp với các tham số đã xử lý
        Page<SanPham> productsPage = sanPhamRepository.findFilteredProducts(
                finalXuatXuIds,
                finalTheLoaiIds,
                finalPhanLoaiIds,
                finalChatLieuIds,
                minPrice,
                maxPrice,
                pageable);

        // 4. & 5. Đưa dữ liệu và tham số lọc vào Model (Giữ nguyên)
        model.addAttribute("productsPage", productsPage);
        model.addAttribute("xuatXuIds", xuatXuIds);
        model.addAttribute("theLoaiIds", theLoaiIds);
        model.addAttribute("phanLoaiIds", phanLoaiIds);
        model.addAttribute("chatLieuIds", chatLieuIds);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);

        return "view/author/sanphamkh";
    }
}