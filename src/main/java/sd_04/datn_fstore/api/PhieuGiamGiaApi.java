package sd_04.datn_fstore.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.model.PhieuGiamGia;
import sd_04.datn_fstore.service.PhieuGiamgiaService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/phieu-giam-gia")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PhieuGiamGiaApi {

    private final PhieuGiamgiaService phieuGiamGiaService;

    // --- UTILITY METHODS ---

    /** Helper để map lỗi validation từ BindingResult */
    private Map<String, String> getValidationErrors(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : result.getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return errors;
    }

    // ==================== 1. TÌM KIẾM & PHÂN TRANG ====================
    @GetMapping
    public ResponseEntity<Page<PhieuGiamGia>> getPromotions(
            @RequestParam(name = "trangThai", required = false) Integer trangThai,
            @RequestParam(name = "keyword", required = false) Optional<String> keyword,
            @RequestParam(name = "ngayBatDau", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ngayBatDau,
            @RequestParam(name = "ngayKetThuc", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ngayKetThuc,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "sortField", defaultValue = "id") String sortField,
            @RequestParam(name = "sortDir", defaultValue = "desc") String sortDir) {

        String searchKeyword = keyword.orElse(null);
        Page<PhieuGiamGia> result = phieuGiamGiaService.searchAndFilter(
                trangThai, searchKeyword, ngayBatDau, ngayKetThuc, page, size, sortField, sortDir);
        return ResponseEntity.ok(result);
    }

    // ==================== 2. THÊM MỚI ====================
    @PostMapping
    public ResponseEntity<?> createPromotion(@Valid @RequestBody PhieuGiamGia phieuGiamGia, BindingResult result) {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng kiểm tra lại dữ liệu", "errors", getValidationErrors(result)));
        }

        try {
            PhieuGiamGia savedPgg = phieuGiamGiaService.saveWithStatusCheck(phieuGiamGia);
            return new ResponseEntity<>(savedPgg, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    // ==================== 3. LẤY CHI TIẾT ====================
    @GetMapping("/{id}")
    public ResponseEntity<PhieuGiamGia> getById(@PathVariable Integer id) {
        return phieuGiamGiaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== 4. CẬP NHẬT ====================
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePromotion(@PathVariable Integer id, @Valid @RequestBody PhieuGiamGia phieuGiamGia, BindingResult result) {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng kiểm tra lại dữ liệu", "errors", getValidationErrors(result)));
        }

        try {
            PhieuGiamGia updatedPhieu = phieuGiamGiaService.update(id, phieuGiamGia);
            return ResponseEntity.ok(updatedPhieu);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    // ==================== 5. ĐẢO TRẠNG THÁI (Active <-> Inactive Thủ công) ====================
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id) {
        try {
            phieuGiamGiaService.toggleStatus(id);
            return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái thành công"));
        } catch (RuntimeException e) {
            // Bao gồm lỗi hết hạn, hết số lượng
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ==================== 6. ĐỒNG BỘ TRẠNG THÁI TỰ ĐỘNG (Dùng cho Scheduler) ====================
    @PostMapping("/sync-status")
    public ResponseEntity<?> syncStatus() {
        try {
            phieuGiamGiaService.syncStatus(); // Đã đổi tên hàm service
            return ResponseEntity.ok(Map.of("message", "Đã đồng bộ trạng thái phiếu giảm giá thành công"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi đồng bộ: " + e.getMessage()));
        }
    }

    // ==================== 7. LẤY VOUCHER CHO CLIENT (Hàm đã được tối ưu trong Service) ====================
    @GetMapping("/active") // Đổi tên để tránh nhầm lẫn, hoặc dùng /active
    public ResponseEntity<List<PhieuGiamGia>> getUsableVouchers() {
        // Dùng hàm đã được tối ưu hóa để lấy chính xác các voucher có thể sử dụng (còn hạn, còn số lượng, trạng thái 0)
        return ResponseEntity.ok(phieuGiamGiaService.findAllActiveVouchers());
    }

    // ==================== 8. VALIDATE & TÍNH TOÁN GIẢM GIÁ CHO CHECKOUT ====================
    @GetMapping("/validate-checkout")
    public ResponseEntity<PhieuGiamgiaService.VoucherCheckResult> validateVoucherForCheckout(
            @RequestParam("code") String code,
            @RequestParam("subTotal") BigDecimal subTotal) {

        PhieuGiamgiaService.VoucherCheckResult result = phieuGiamGiaService.kiemTraVoucherHople(code, subTotal);

        if (result.isValid()) {
            return ResponseEntity.ok(result);
        } else {
            // Trả về 200 OK nhưng kèm isValid=false để frontend xử lý thông báo lỗi (logic nghiệp vụ)
            return ResponseEntity.ok(result);
        }
    }

    // ==================== 9. TÌM MÃ GIẢM GIÁ TỐT NHẤT ====================
    @GetMapping("/best-voucher")
    public ResponseEntity<?> findBestVoucher(@RequestParam("subTotal") BigDecimal subTotal) {
        String bestCode = phieuGiamGiaService.timVoucherTotNhat(subTotal);

        if (bestCode != null) {
            return ResponseEntity.ok(Map.of("bestCode", bestCode));
        } else {
            return ResponseEntity.ok(Map.of("message", "Không tìm thấy mã giảm giá phù hợp.", "bestCode", (String) null));
        }
    }
}