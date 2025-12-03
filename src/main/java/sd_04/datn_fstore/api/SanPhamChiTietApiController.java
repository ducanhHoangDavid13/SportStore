package sd_04.datn_fstore.api;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.model.SanPhamChiTiet;
import sd_04.datn_fstore.service.SanPhamCTService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/san-pham-chi-tiet")
@RequiredArgsConstructor
@CrossOrigin("*")
public class SanPhamChiTietApiController {

    private final SanPhamCTService sanPhamCTService;

    // API tìm kiếm / lọc (ĐÃ BỎ minPrice, maxPrice)
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
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) String keyword
    ) {
        Page<SanPhamChiTiet> spctPage = sanPhamCTService.search(
                pageable, idSanPham, idKichThuoc, idChatLieu, idTheLoai,
                idXuatXu, idMauSac, idPhanLoai, trangThai, keyword
        );
        return ResponseEntity.ok(spctPage);
    }

    @PostMapping
    public ResponseEntity<?> addVariant(@RequestBody SanPhamChiTiet sanPhamChiTiet) {
        try {
            sanPhamChiTiet.setId(null);
            SanPhamChiTiet savedSpct = sanPhamCTService.save(sanPhamChiTiet);
            return new ResponseEntity<>(savedSpct, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Thêm mới thất bại: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateTrangThai(
            @PathVariable("id") Integer id,
            @RequestParam("trangThai") Integer trangThai) {
        try {
            return ResponseEntity.ok(sanPhamCTService.updateTrangThai(id, trangThai));
        } catch (RuntimeException e) {
            Map<String, String> err = new HashMap<>();
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVariant(@PathVariable Integer id,
                                           @RequestBody SanPhamChiTiet dataTuJavaScript) {
        Optional<SanPhamChiTiet> optSpct = sanPhamCTService.getById(id);
        if (optSpct.isEmpty()) {
            return new ResponseEntity<>("Không tìm thấy biến thể với ID: " + id, HttpStatus.NOT_FOUND);
        }
        SanPhamChiTiet spctTrongDB = optSpct.get();

        spctTrongDB.setGiaTien(dataTuJavaScript.getGiaTien());
        spctTrongDB.setSoLuong(dataTuJavaScript.getSoLuong());
        spctTrongDB.setMoTa(dataTuJavaScript.getMoTa());
        spctTrongDB.setTrangThai(dataTuJavaScript.getTrangThai());
        spctTrongDB.setSanPham(dataTuJavaScript.getSanPham());
        spctTrongDB.setMauSac(dataTuJavaScript.getMauSac());
        spctTrongDB.setKichThuoc(dataTuJavaScript.getKichThuoc());
        spctTrongDB.setChatLieu(dataTuJavaScript.getChatLieu());
        spctTrongDB.setXuatXu(dataTuJavaScript.getXuatXu());
        spctTrongDB.setTheLoai(dataTuJavaScript.getTheLoai());

        try {
            SanPhamChiTiet updatedSpct = sanPhamCTService.save(spctTrongDB);
            return ResponseEntity.ok(updatedSpct);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<SanPhamChiTiet> getById(@PathVariable Integer id) {
        return sanPhamCTService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVariant(@PathVariable Integer id) {
        if (sanPhamCTService.getById(id).isEmpty()) {
            return new ResponseEntity<>("Không tìm thấy biến thể với ID: " + id, HttpStatus.NOT_FOUND);
        }
        try {
            sanPhamCTService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Không thể xóa biến thể này vì đang được sử dụng.");
        }
    }

    @GetMapping("/search")
    public List<SanPhamChiTiet> searchBySanPhamTen(@RequestParam("tenSp") String tenSp) {
        return sanPhamCTService.searchBySanPhamTen(tenSp);
    }
}