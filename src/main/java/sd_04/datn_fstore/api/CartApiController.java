//package sd_04.datn_fstore.api;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import java.util.HashMap; // 👈 Cần thiết
//import java.util.Map;     // 👈 Cần thiết
//
//@RestController
//@RequiredArgsConstructor
//public class CartApiController {
//
//    // 💡 BƯỚC THIẾT YẾU: Khai báo và Inject CartService của bạn vào đây
//    // private final CartService cartService;
//
//    // API này nhận chi tiết sản phẩm và số lượng
//    @PostMapping("/api/cart/add")
//    public Map<String, Object> addToCart(
//            @RequestParam Long productDetailId,
//            @RequestParam int quantity) {
//
//        // --- BƯỚC 1: LOGIC THỰC TẾ ---
//        // int totalCartItems = cartService.addProductToCart(productDetailId, quantity);
//
//        // --- BƯỚC 2: MOCK DATA (Thay thế bằng kết quả từ BƯỚC 1) ---
//        // Giả sử sau khi thêm, giỏ hàng có 3 sản phẩm
//        int totalCartItems = 3;
//
//        // --- BƯỚC 3: TRẢ VỀ JSON ---
//        Map<String, Object> response = new HashMap<>();
//        response.put("success", true);
//        response.put("newCount", totalCartItems); // Trả về số lượng mới để JS cập nhật
//
//        return response;
//    }
//}