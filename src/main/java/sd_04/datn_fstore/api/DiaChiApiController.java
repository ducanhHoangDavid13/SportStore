package sd_04.datn_fstore.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.dto.DiaChiDTO;
import sd_04.datn_fstore.model.DiaChi;
import sd_04.datn_fstore.service.DiaChiService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/dia-chi")
@RequiredArgsConstructor // Sử dụng constructor injection, an toàn và dễ test hơn
public class DiaChiApiController {

    private final DiaChiService diaChiService;

    // TODO: HÀM QUAN TRỌNG - Thay thế bằng logic lấy ID khách hàng đang đăng nhập thực tế
    // Ví dụ: Lấy từ Principal của Spring Security
    private Integer getLoggedInCustomerId() {
        // --- GIẢ LẬP ---
        // Trong thực tế, bạn sẽ làm như sau:
        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // String username = authentication.getName();
        // KhachHang kh = khachHangRepository.findByEmail(username);
        // return kh.getId();
        return 1; // Tạm thời trả về ID=1 để test
    }

    /**
     * API LẤY TẤT CẢ ĐỊA CHỈ CỦA USER ĐANG ĐĂNG NHẬP
     * Endpoint: GET /api/dia-chi
     * Mô tả: Tự động lấy ID của user đang đăng nhập và trả về danh sách địa chỉ của họ.
     *       Đây là cách làm bảo mật và đúng chuẩn.
     */
    @GetMapping
    public ResponseEntity<List<DiaChi>> getAllAddressesForCurrentUser() {
        Integer idKhachHang = getLoggedInCustomerId();
        List<DiaChi> diaChis = diaChiService.getDiaChiByKhachHangId(idKhachHang);
        return ResponseEntity.ok(diaChis);
    }

    /**
     * API LẤY CHI TIẾT MỘT ĐỊA CHỈ BẰNG ID
     * Endpoint: GET /api/dia-chi/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<DiaChi> getAddressById(@PathVariable Integer id) {
        Integer idKhachHang = getLoggedInCustomerId();
        Optional<DiaChi> diaChiOpt = diaChiService.getById(id);

        // Kiểm tra xem địa chỉ có tồn tại và có thuộc về user đang đăng nhập không
        if (diaChiOpt.isPresent() && diaChiOpt.get().getKhachhang().getId().equals(idKhachHang)) {
            return ResponseEntity.ok(diaChiOpt.get());
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Hoặc .notFound() tùy vào yêu cầu bảo mật
    }

    /**
     * API TẠO MỘT ĐỊA CHỈ MỚI
     * Endpoint: POST /api/dia-chi
     * Body: JSON chứa thông tin của DiaChiDTO
     */
    @PostMapping
    public ResponseEntity<?> createAddress(@Valid @RequestBody DiaChiDTO diaChiDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        Integer idKhachHang = getLoggedInCustomerId();

        DiaChi newDiaChi = new DiaChi();
        // Chuyển đổi từ DTO sang Entity
        newDiaChi.setHoTen(diaChiDTO.getHoTen());
        newDiaChi.setSoDienThoai(diaChiDTO.getSoDienThoai());
        newDiaChi.setDiaChiCuThe(diaChiDTO.getDiaChiCuThe());
        newDiaChi.setXa(diaChiDTO.getXa());
        newDiaChi.setThanhPho(diaChiDTO.getThanhPho());
        newDiaChi.setLoaiDiaChi(diaChiDTO.getLoaiDiaChi());
        newDiaChi.setGhiChu(diaChiDTO.getGhiChu());

        DiaChi savedDiaChi = diaChiService.saveNewAddress(newDiaChi, idKhachHang);
        return new ResponseEntity<>(savedDiaChi, HttpStatus.CREATED); // Trả về 201 Created
    }

    /**
     * API CẬP NHẬT MỘT ĐỊA CHỈ HIỆN CÓ
     * Endpoint: PUT /api/dia-chi/{id}
     * Body: JSON chứa thông tin của DiaChiDTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable Integer id, @Valid @RequestBody DiaChiDTO diaChiDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        Integer idKhachHang = getLoggedInCustomerId();
        Optional<DiaChi> existingDiaChiOpt = diaChiService.getById(id);

        // Kiểm tra xem địa chỉ có tồn tại và thuộc về user không
        if (existingDiaChiOpt.isEmpty() || !existingDiaChiOpt.get().getKhachhang().getId().equals(idKhachHang)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền sửa địa chỉ này.");
        }

        DiaChi updatedInfo = new DiaChi();
        // Chuyển đổi DTO sang Entity
        updatedInfo.setHoTen(diaChiDTO.getHoTen());
        updatedInfo.setSoDienThoai(diaChiDTO.getSoDienThoai());
        updatedInfo.setDiaChiCuThe(diaChiDTO.getDiaChiCuThe());
        updatedInfo.setXa(diaChiDTO.getXa());
        updatedInfo.setThanhPho(diaChiDTO.getThanhPho());
        updatedInfo.setLoaiDiaChi(diaChiDTO.getLoaiDiaChi());
        updatedInfo.setGhiChu(diaChiDTO.getGhiChu());

        DiaChi result = diaChiService.updateAddress(id, updatedInfo);
        return ResponseEntity.ok(result);
    }

    /**
     * API XÓA MỀM MỘT ĐỊA CHỈ
     * Endpoint: DELETE /api/dia-chi/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable Integer id) {
        Integer idKhachHang = getLoggedInCustomerId();
        Optional<DiaChi> existingDiaChiOpt = diaChiService.getById(id);

        // Kiểm tra xem địa chỉ có tồn tại và thuộc về user không
        if (existingDiaChiOpt.isEmpty() || !existingDiaChiOpt.get().getKhachhang().getId().equals(idKhachHang)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền xóa địa chỉ này.");
        }

        diaChiService.deleteAddress(id);
        return ResponseEntity.noContent().build(); // Trả về 204 No Content, là chuẩn cho việc xóa thành công
    }
}