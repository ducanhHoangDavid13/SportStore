package sd_04.datn_fstore.api;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.model.KhachHang;
import org.springframework.beans.factory.annotation.Autowired;
import sd_04.datn_fstore.service.KhachhangService;

import java.util.List;

@RestController
@RequestMapping("/api/khach-hang")
public class KhachHangApi {
    @Autowired
    private KhachhangService khachHangService;

    private final int pageSize = 5; // Kích thước trang mặc định

    @GetMapping
    public Page<KhachHang> getKhachHangList(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sdt,
            @RequestParam(required = false) Boolean gioiTinh) {

        return khachHangService.getFilteredKhachHang(keyword, sdt, gioiTinh, pageNo, pageSize);
    }

    @GetMapping("/{id}")
    public KhachHang getKhachHangById(@PathVariable Integer id) {
        return khachHangService.findById(id)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại: " + id));
    }

    // 3. POST (CREATE) -> Xử lý yêu cầu Thêm mới
    @PostMapping
    public ResponseEntity<KhachHang> addKhachHang(@RequestBody KhachHang khachhang) {
        try {
            KhachHang newKh = khachHangService.save(khachhang);
            return new ResponseEntity<>(newKh, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // 4. PUT (UPDATE) -> Xử lý yêu cầu Cập nhật
    @PutMapping("/{id}")
    public ResponseEntity<KhachHang> updateKhachHang(@PathVariable Integer id, @RequestBody KhachHang khachhangDetails) {
        if (!id.equals(khachhangDetails.getId())) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        return khachHangService.findById(id)
                .map(khachhang -> {
                    // Cập nhật các trường dữ liệu
                    khachhang.setMaKhachHang(khachhangDetails.getMaKhachHang());
                    khachhang.setTenKhachHang(khachhangDetails.getTenKhachHang());
                    khachhang.setSoDienThoai(khachhangDetails.getSoDienThoai());
                    khachhang.setEmail(khachhangDetails.getEmail());
                    khachhang.setNamSinh(khachhangDetails.getNamSinh());
                    khachhang.setGioiTinh(khachhangDetails.getGioiTinh());

                    KhachHang updatedKh = khachHangService.save(khachhang);
                    return new ResponseEntity<>(updatedKh, HttpStatus.OK);
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // 5. DELETE (SOFT DELETE) -> Xử lý yêu cầu Xóa mềm
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteKhachHang(@PathVariable Integer id) {
        try {
            khachHangService.softDeleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Error
        }
    }

}
