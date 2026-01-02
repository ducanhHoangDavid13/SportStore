package sd_04.datn_fstore.api;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.dto.KhachHangRegistration;
import sd_04.datn_fstore.model.KhachHang;
// import org.springframework.beans.factory.annotation.Autowired; // <-- 1. XÓA DÒNG NÀY
import sd_04.datn_fstore.service.KhachhangService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/khach-hang")
@RequiredArgsConstructor // <-- Giữ cái này
public class KhachHangApi {

    // @Autowired // <-- 2. XÓA DÒNG NÀY
    private final KhachhangService khachHangService; // <-- 3. THÊM 'final' VÀO ĐÂY

    private final int pageSize = 5; // Kích thước trang mặc định

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
        KhachHang khachHang = khachHangService.findByEmail(username); // Giả định KhachhangService có hàm findByEmail

        if (khachHang == null) {
            throw new UsernameNotFoundException("Không tìm thấy khách hàng với username: " + username);
        }

        return khachHang.getId();
    }

    // (Hàm này dùng cho Admin)
    @GetMapping
    public Page<KhachHang> getKhachHangList(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sdt,
            @RequestParam(required = false) Boolean gioiTinh) {

        return khachHangService.getFilteredKhachHang(keyword, sdt, gioiTinh, pageNo, pageSize);
    }

    // (Hàm này dùng cho Admin)
    @GetMapping("/{id}")
    public KhachHang getKhachHangById(@PathVariable Integer id) {
        return khachHangService.findById(id)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại: " + id));
    }

    // (Hàm này dùng cho Admin)
    @PostMapping
    public ResponseEntity<KhachHang> addKhachHang(@RequestBody KhachHangRegistration registration) {
        try {
            KhachHang newKh = khachHangService.save(registration);
            return new ResponseEntity<>(newKh, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // (Hàm này dùng cho Admin)
    @PutMapping("/{id}")
    public ResponseEntity<KhachHang> updateKhachHang(@PathVariable Integer id, @RequestBody KhachHang khachhangDetails) {
        return khachHangService.findById(id)
                .map(khachhang -> {
                    // Giữ lại các trường không cho phép sửa (như Mã KH)
                    khachhang.setMaKhachHang(khachhangDetails.getMaKhachHang());

                    // CẬP NHẬT CÁC TRƯỜNG CÓ THỂ CHỈNH SỬA:
                    khachhang.setTenKhachHang(khachhangDetails.getTenKhachHang());

                    // Thêm các trường bị thiếu:
                    khachhang.setSoDienThoai(khachhangDetails.getSoDienThoai());
                    khachhang.setGioiTinh(khachhangDetails.getGioiTinh());
                    khachhang.setNamSinh(khachhangDetails.getNamSinh());

                     khachhang.setEmail(khachhangDetails.getEmail());

                    KhachHang updatedKh = khachHangService.update(khachhang);
                    return new ResponseEntity<>(updatedKh, HttpStatus.OK);
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // (Hàm này dùng cho Admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteKhachHang(@PathVariable Integer id) {
        // ... (Logic của bạn đã ổn) ...
        try {
            khachHangService.softDeleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * HÀM QUAN TRỌNG: Dùng cho Giao diện POS (banHang.html)
     */
    @GetMapping("/search")
    public ResponseEntity<List<KhachHang>> searchCustomer(@RequestParam String keyword) {
        // (Bạn cần tạo hàm searchCustomerByNameOrPhone trong Service/Repo)
        List<KhachHang> customers = khachHangService.searchCustomerByNameOrPhone(keyword);
        return ResponseEntity.ok(customers);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addKhachHang(@RequestBody KhachHang kh) {
        try {
            KhachHang newKh = khachHangService.saveKH(kh);
            return new ResponseEntity<>(newKh, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi thêm khách hàng: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<KhachHang> getMyProfile() {
        try {
            Integer idKhachHang = getLoggedInCustomerId();
            Optional<KhachHang> khachHangOpt = khachHangService.findById(idKhachHang);

            // Kiểm tra lại lần nữa (dù hàm getLoggedInCustomerId đã tìm thấy)
            if (khachHangOpt.isPresent()) {
                return ResponseEntity.ok(khachHangOpt.get());
            }

            // Trường hợp lỗi bảo mật/đồng bộ dữ liệu
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (AccessDeniedException e) {
            // Spring Security sẽ thường bắt lỗi này và trả về 401/403
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (UsernameNotFoundException e) {
            // Lỗi không tìm thấy user trong DB (dù đã đăng nhập)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}