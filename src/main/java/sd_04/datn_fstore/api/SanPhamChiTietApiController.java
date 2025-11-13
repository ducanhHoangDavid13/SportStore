package sd_04.datn_fstore.api;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.model.SanPhamChiTiet;
import sd_04.datn_fstore.service.SanPhamCTService;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/san-pham-chi-tiet")
@RequiredArgsConstructor
@CrossOrigin("*")
public class SanPhamChiTietApiController {

    private final SanPhamCTService sanPhamCTService;

    // API này sẽ được JS gọi để lấy dữ liệu cho bảng
    @GetMapping
    public ResponseEntity<Page<SanPhamChiTiet>> search(
            Pageable pageable,
            @RequestParam(required = false) Integer idSanPham,
            @RequestParam(required = false) Integer idKichThuoc,
            @RequestParam(required = false) Integer idPhanLoai,
            @RequestParam(required = false) Integer idXuatXu,
            @RequestParam(required = false) Integer idChatLieu,
            @RequestParam(required = false) Integer idMauSac,
            @RequestParam(required = false) Integer idTheLoai,
            @RequestParam(required = false) BigDecimal minPrice, // Sửa tên biến
            @RequestParam(required = false) BigDecimal maxPrice, // Sửa tên biến
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) String keyword
    ) {
        Page<SanPhamChiTiet> spctPage = sanPhamCTService.search(
                pageable, idSanPham, idKichThuoc, idChatLieu, idTheLoai,
                idXuatXu, idMauSac, idPhanLoai, minPrice, maxPrice, trangThai, keyword
        );
        return ResponseEntity.ok(spctPage);
    }

//    // API này sẽ được JS gọi để lấy dữ liệu cho modal "Sửa" SPCT
//    @GetMapping("/{id}")
//    public ResponseEntity<SanPhamChiTiet> getById(@PathVariable Integer id) {
//        return sanPhamCTService.getById(id)
//                .map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }

    // API để Thêm mới biến thể
    @PostMapping
    public ResponseEntity<?> addVariant(@RequestBody SanPhamChiTiet sanPhamChiTiet) {
        try {
            sanPhamChiTiet.setId(null); // Đảm bảo là thêm mới
            SanPhamChiTiet savedSpct = sanPhamCTService.save(sanPhamChiTiet);
            return new ResponseEntity<>(savedSpct, HttpStatus.CREATED);
        } catch (Exception e) {
            // Thường là lỗi UNIQUE constraint (biến thể đã tồn tại)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Thêm mới thất bại: Biến thể này có thể đã tồn tại.");
        }
    }

    /**
     * SỬA: API để Cập nhật biến thể (Sử dụng logic an toàn hơn)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVariant(@PathVariable Integer id,
                                           @RequestBody SanPhamChiTiet dataTuJavaScript) {

        // 1. Lấy đối tượng GỐC từ CSDL để tránh ghi đè null
        Optional<SanPhamChiTiet> optSpct = sanPhamCTService.getById(id);
        if (optSpct.isEmpty()) {
            return new ResponseEntity<>("Không tìm thấy biến thể với ID: " + id, HttpStatus.NOT_FOUND);
        }
        SanPhamChiTiet spctTrongDB = optSpct.get();

        // 2. Cập nhật các trường có thể thay đổi (giá, số lượng,...)
        spctTrongDB.setGiaTien(dataTuJavaScript.getGiaTien());
        spctTrongDB.setSoLuong(dataTuJavaScript.getSoLuong());
        // Bạn có thể set thêm các trường khác như moTa, trangThai nếu cần
        // spctTrongDB.setMoTa(dataTuJavaScript.getMoTa());
        // spctTrongDB.setTrangThai(dataTuJavaScript.getTrangThai());

        // 3. Cập nhật các mối quan hệ
        spctTrongDB.setSanPham(dataTuJavaScript.getSanPham());
        spctTrongDB.setMauSac(dataTuJavaScript.getMauSac());
        spctTrongDB.setKichThuoc(dataTuJavaScript.getKichThuoc());
        spctTrongDB.setTheLoai(dataTuJavaScript.getTheLoai());
        spctTrongDB.setChatLieu(dataTuJavaScript.getChatLieu());
        spctTrongDB.setXuatXu(dataTuJavaScript.getXuatXu());
        spctTrongDB.setPhanLoai(dataTuJavaScript.getPhanLoai());

        try {
            // 4. Lưu lại đối tượng đã được cập nhật đầy đủ
            SanPhamChiTiet updatedSpct = sanPhamCTService.save(spctTrongDB);
            return ResponseEntity.ok(updatedSpct);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi khi cập nhật: Biến thể này có thể đã tồn tại.");
        }
    }
    // API này sẽ được JS gọi để lấy dữ liệu cho modal "Sửa" SPCT
    @GetMapping("/{id}")
    @Transactional(readOnly = true) // <-- SỬA: THÊM DÒNG NÀY
    public ResponseEntity<SanPhamChiTiet> getById(@PathVariable Integer id) {
        return sanPhamCTService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    /**
     * SỬA: Bổ sung API để Xóa biến thể
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVariant(@PathVariable Integer id) {
        // Kiểm tra xem ID có tồn tại không trước khi xóa
        if (!sanPhamCTService.getById(id).isPresent()) {
            return new ResponseEntity<>("Không tìm thấy biến thể với ID: " + id, HttpStatus.NOT_FOUND);
        }
        try {
            sanPhamCTService.delete(id);
            return ResponseEntity.noContent().build(); // Trả về 204 No Content là thành công
        } catch (Exception e) {
            // Thường là lỗi khóa ngoại (nếu biến thể đã có trong hóa đơn)
            return ResponseEntity.status(HttpStatus.CONFLICT) // Dùng 409 cho lỗi khóa ngoại
                    .body("Không thể xóa biến thể này vì đang được sử dụng.");
        }
    }
    @GetMapping("/available")
    public List<SanPhamChiTiet> getAvailableProducts() {
        // SỬA LỖI: Gọi Service thay vì Repository
        return sanPhamCTService.getAvailableProducts();
    }
}