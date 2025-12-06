package sd_04.datn_fstore.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; // Thêm thư viện này
import org.springframework.security.core.Authentication; // Thêm thư viện này
import org.springframework.security.core.context.SecurityContextHolder; // Thêm thư viện này
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Thêm thư viện này
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.dto.DiaChiDTO;
import sd_04.datn_fstore.model.DiaChi;
import sd_04.datn_fstore.model.KhachHang; // Thêm KhachHang Model
import sd_04.datn_fstore.service.DiaChiService;
import sd_04.datn_fstore.service.KhachhangService; // Thêm Khachhang Service để tìm User ID

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/dia-chi")
@RequiredArgsConstructor
public class DiaChiApiController {

    private final DiaChiService diaChiService;
    private final KhachhangService khachhangService; // Inject KhachhangService

    /**
     * HÀM QUAN TRỌNG: Lấy ID Khách hàng đang đăng nhập từ Spring Security Context.
     * @return ID của Khách hàng
     * @throws AccessDeniedException nếu người dùng chưa đăng nhập.
     */
    private Integer getLoggedInCustomerId() {
        // 1. Lấy thông tin xác thực từ Context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. Kiểm tra nếu chưa xác thực hoặc là người dùng ẩn danh
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            // Ném ngoại lệ, Spring Security sẽ bắt và trả về 401/403 (như bạn đã cấu hình)
            throw new AccessDeniedException("Unauthorized access: User not logged in.");
        }

        // 3. Lấy tên đăng nhập (thường là email/username)
        String username = authentication.getName();

        // 4. Tìm Khách hàng Entity bằng username/email và lấy ID
        KhachHang khachHang = khachhangService.findByEmail(username); // Giả định KhachhangService có hàm findByEmail

        if (khachHang == null) {
            throw new UsernameNotFoundException("Không tìm thấy khách hàng với username: " + username);
        }

        return khachHang.getId();
    }

    // ---------------------------------------------------------------------
    // PHƯƠNG THỨC API VẪN GIỮ NGUYÊN LOGIC, CHỈ THAY ĐỔI CÁCH LẤY ID KH
    // ---------------------------------------------------------------------

    /**
     * API LẤY TẤT CẢ ĐỊA CHỈ CỦA USER ĐANG ĐĂNG NHẬP
     * Endpoint: GET /api/dia-chi
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

        // Trả về 404 (Not Found) thay vì 403 (Forbidden) để tăng tính bảo mật
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /**
     * API TẠO MỘT ĐỊA CHỈ MỚI
     * Endpoint: POST /api/dia-chi
     */
    @PostMapping
    public ResponseEntity<?> createAddress(@Valid @RequestBody DiaChiDTO diaChiDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        Integer idKhachHang = getLoggedInCustomerId();

        // Tối ưu: Nếu có thể, sử dụng Mapper (MapStruct/ModelMapper) ở đây
        DiaChi newDiaChi = new DiaChi();
        newDiaChi.setHoTen(diaChiDTO.getHoTen());
        newDiaChi.setSoDienThoai(diaChiDTO.getSoDienThoai());
        newDiaChi.setDiaChiCuThe(diaChiDTO.getDiaChiCuThe());
        newDiaChi.setXa(diaChiDTO.getXa());
        newDiaChi.setThanhPho(diaChiDTO.getThanhPho());
        newDiaChi.setLoaiDiaChi(diaChiDTO.getLoaiDiaChi());
        newDiaChi.setGhiChu(diaChiDTO.getGhiChu());

        DiaChi savedDiaChi = diaChiService.saveNewAddress(newDiaChi, idKhachHang);
        return new ResponseEntity<>(savedDiaChi, HttpStatus.CREATED);
    }

    /**
     * API CẬP NHẬT MỘT ĐỊA CHỈ HIỆN CÓ
     * Endpoint: PUT /api/dia-chi/{id}
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy địa chỉ hoặc bạn không có quyền sửa.");
        }

        // Tối ưu: Nếu có thể, sử dụng Mapper (MapStruct/ModelMapper) ở đây
        DiaChi updatedInfo = new DiaChi();
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy địa chỉ hoặc bạn không có quyền xóa.");
        }

        diaChiService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-khachhang/{khachHangId}")
    // ⚠️ Bạn nên thêm chú thích bảo mật: Ví dụ: @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DiaChi>> getAddressesByKhachHangId(@PathVariable Integer khachHangId) {
        // Trong môi trường Admin, không cần kiểm tra idKhachHang == id đang đăng nhập
        // Chỉ cần kiểm tra quyền Admin/Staff ở lớp bảo mật (Security Config hoặc @PreAuthorize)

        List<DiaChi> diaChis = diaChiService.getDiaChiByKhachHangId(khachHangId);

        if (diaChis.isEmpty()) {
            // Trả về 404 nếu không tìm thấy địa chỉ nào cho ID đó
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(diaChis);
    }
}