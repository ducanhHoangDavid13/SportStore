package sd_04.datn_fstore.api;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.dto.KhachHangRequest;
import sd_04.datn_fstore.model.KhachHang;
// import org.springframework.beans.factory.annotation.Autowired; // <-- 1. XÓA DÒNG NÀY
import sd_04.datn_fstore.service.KhachhangService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/khach-hang")
@RequiredArgsConstructor // <-- Giữ cái này
public class KhachHangApi {

    // @Autowired // <-- 2. XÓA DÒNG NÀY
    private final KhachhangService khachHangService; // <-- 3. THÊM 'final' VÀO ĐÂY

    private final int pageSize = 5; // Kích thước trang mặc định

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
    public ResponseEntity<KhachHang> addKhachHang(@RequestBody KhachHangRequest khachhang) {
        try {
            KhachHang newKh = khachHangService.save(khachhang);
            return new ResponseEntity<>(newKh, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // (Hàm này dùng cho Admin)
    @PutMapping("/{id}")
    public ResponseEntity<KhachHang> updateKhachHang(@PathVariable Integer id, @RequestBody KhachHang khachhangDetails) {
        // ... (Logic của bạn đã ổn) ...
        return khachHangService.findById(id)
                .map(khachhang -> {
                    khachhang.setMaKhachHang(khachhangDetails.getMaKhachHang());
                    khachhang.setTenKhachHang(khachhangDetails.getTenKhachHang());
                    // ...
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
}