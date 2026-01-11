package sd_04.datn_fstore.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.model.KhachHang;
import sd_04.datn_fstore.service.DonHangCuaToiService;
import sd_04.datn_fstore.service.KhachhangService;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/don-hang-cua-toi")
public class DonHangCuaToiApi {

    @Autowired
    private DonHangCuaToiService donHangCuaToiService;
    @Autowired
    private KhachhangService khachHangService;


    private Integer getLoggedInCustomerId() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. Kiểm tra nếu chưa xác thực hoặc là người dùng ẩn danh
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            // Ném ngoại lệ, Spring Security sẽ bắt và trả về 401/403 (như bạn đã cấu hình)
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized access: User not logged in.");
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

    @GetMapping
    public ResponseEntity<Page<HoaDon>> getHoaDonByKhachHang(
            @RequestParam(required = false) List<Integer> trangThai, // Đổi từ Integer sang List<Integer>
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Integer idKhachHang = getLoggedInCustomerId();

            // Truyền List xuống service
            Page<HoaDon> hoaDons = donHangCuaToiService.getHoaDonByKhachHang(idKhachHang, trangThai, page, size);

            return new ResponseEntity<>(hoaDons, HttpStatus.OK);
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/cap-nhat-trang-thai/{id}")
    public ResponseEntity<HoaDon> updateStatus(
            @PathVariable Integer id,
            @RequestBody Integer trangThai) { // Nhận trực tiếp Integer

        // Kiểm tra trangThai có null không
        if (trangThai == null) {
            return ResponseEntity.badRequest().build();
        }

        HoaDon updatedHoaDon = donHangCuaToiService.updateOrderStatus(id, trangThai);
        return ResponseEntity.ok(updatedHoaDon);
    }
}
