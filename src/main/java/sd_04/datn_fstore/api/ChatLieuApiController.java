package sd_04.datn_fstore.api;

import lombok.RequiredArgsConstructor;
// Thêm import cho Pageable và Page
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.model.ChatLieu;
import sd_04.datn_fstore.service.ChatLieuService;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/chat-lieu")
@RequiredArgsConstructor
public class ChatLieuApiController {

    private final ChatLieuService chatLieuService;

    // ----------------------------------------------------------------------
    // PHẦN BỔ SUNG: XỬ LÝ LẤY DANH SÁCH (GET /api/chat-lieu)
    // ----------------------------------------------------------------------
    /**
     * Lấy danh sách, hỗ trợ Tìm kiếm (keyword) và Lọc (trangThai) KẾT HỢP PHÂN TRANG.
     * Đây chính là phương thức xử lý lỗi 405 của bạn.
     */
    @GetMapping
    public ResponseEntity<Page<ChatLieu>> getAllPaginated(
            @PageableDefault(size = 5) Pageable pageable, // Lấy thông tin phân trang
            @RequestParam(required = false) String keyword,     // Lấy từ khóa tìm kiếm
            @RequestParam(required = false) Integer trangThai) { // Lấy trạng thái lọc

        // Gọi phương thức searchAndPaginate từ Service
        Page<ChatLieu> chatLieuPage = chatLieuService.searchAndPaginate(pageable, keyword, trangThai);

        return ResponseEntity.ok(chatLieuPage); // Trả về đối tượng Page (JSON)
    }

    // ----------------------------------------------------------------------
    // CÁC PHƯƠNG THỨC KHÁC (GIỮ NGUYÊN)
    // ----------------------------------------------------------------------

// File: ChatLieuApiController.java
// ... (các import và @RequestMapping)

    // 1. CHỈ DÙNG CHO THÊM MỚI (CREATE)
    @PostMapping
    public ResponseEntity<ChatLieu> addChatLieu(@RequestBody ChatLieu chatLieu) {
        try {
            // Đảm bảo ID là null để tránh ghi đè
            chatLieu.setId(null);
            ChatLieu savedChatLieu = chatLieuService.save(chatLieu);
            return new ResponseEntity<>(savedChatLieu, HttpStatus.CREATED);
        } catch (Exception e) {
            // Ví dụ: Lỗi validation hoặc CSDL
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateTrangThai(@PathVariable("id") Integer id, @RequestParam("trangThai") Integer trangThai) {
        try {
            return ResponseEntity.ok(chatLieuService.updateTrangThai(id, trangThai));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }

    // 2. CHỈ DÙNG CHO CẬP NHẬT (UPDATE)
    @PutMapping("/{id}")
    public ResponseEntity<ChatLieu> updateChatLieu(@PathVariable Integer id,
                                                   @RequestBody ChatLieu chatLieu) {
        try {
            // Gán ID từ URL vào đối tượng để service biết cần cập nhật
            chatLieu.setId(id);
            ChatLieu updatedChatLieu = chatLieuService.save(chatLieu);
            return ResponseEntity.ok(updatedChatLieu);
        } catch (Exception e) {
            // Ví dụ: Gửi ID không tồn tại
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // ... (Các phương thức @GetMapping và @DeleteMapping khác của bạn) ...

    // 2. READ (Lấy chi tiết)
    @GetMapping("/{id}")
    public ResponseEntity<ChatLieu> getById(@PathVariable Integer id) {
        Optional<ChatLieu> chatLieu = chatLieuService.getById(id);
        return chatLieu.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 3. DELETE (Xóa)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChatLieu(@PathVariable Integer id) {
        try {
            chatLieuService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Không thể xóa Chất Liệu này do ràng buộc dữ liệu.");
        }
    }
}