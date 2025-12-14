package sd_04.datn_fstore.api;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    /**
     * GET: API tìm kiếm / lọc các Biến thể (SanPhamChiTiet)
     */
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
        // Gọi Service để thực hiện tìm kiếm/lọc
        Page<SanPhamChiTiet> spctPage = sanPhamCTService.search(
                pageable, idSanPham, idKichThuoc, idChatLieu, idTheLoai,
                idXuatXu, idMauSac, idPhanLoai, trangThai, keyword
        );
        return ResponseEntity.ok(spctPage);
    }

    /**
     * POST: Thêm mới một Biến thể (SanPhamChiTiet)
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> addVariant(@RequestBody SanPhamChiTiet sanPhamChiTiet) {
        try {
            // Đảm bảo ID được thiết lập là null để Spring Data JPA tự động sinh
            sanPhamChiTiet.setId(null);
            SanPhamChiTiet savedSpct = sanPhamCTService.save(sanPhamChiTiet);
            return new ResponseEntity<>(savedSpct, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            // Trả về message chi tiết từ Service (Lỗi tham chiếu bắt buộc)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Thêm mới thất bại: " + e.getMessage());
        }
    }

    /**
     * PUT: Cập nhật trường trạng thái của một Biến thể theo ID
     */
    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateTrangThai(
            @PathVariable("id") Integer id,
            @RequestParam("trangThai") Integer trangThai) {
        try {
            // Gọi Service để cập nhật trạng thái
            return ResponseEntity.ok(sanPhamCTService.updateTrangThai(id, trangThai));
        } catch (RuntimeException e) {
            // Xử lý lỗi nghiệp vụ (ví dụ: không tìm thấy ID)
            Map<String, String> err = new HashMap<>();
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        } catch (Exception e) {
            // Xử lý lỗi hệ thống
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }

    /**
     * PUT: Cập nhật toàn bộ thông tin của một Biến thể theo ID
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateVariant(@PathVariable Integer id,
                                           @RequestBody SanPhamChiTiet dataTuJavaScript) {
        // 1. Kiểm tra sự tồn tại của biến thể
        Optional<SanPhamChiTiet> optSpct = sanPhamCTService.getById(id);
        if (optSpct.isEmpty()) {
            return new ResponseEntity<>("Không tìm thấy biến thể với ID: " + id, HttpStatus.NOT_FOUND);
        }

        SanPhamChiTiet spctTrongDB = optSpct.get();

        // 2. Cập nhật các trường từ dữ liệu nhận được (Chỉ cập nhật nếu giá trị không phải là null)

        // Cập nhật trường giá trị đơn thuần
        if (dataTuJavaScript.getGiaTien() != null) {
            spctTrongDB.setGiaTien(dataTuJavaScript.getGiaTien());
        }
        if (dataTuJavaScript.getSoLuong() != null) {
            spctTrongDB.setSoLuong(dataTuJavaScript.getSoLuong());
        }
        if (dataTuJavaScript.getMoTa() != null) {
            spctTrongDB.setMoTa(dataTuJavaScript.getMoTa());
        }
        if (dataTuJavaScript.getTrangThai() != null) {
            spctTrongDB.setTrangThai(dataTuJavaScript.getTrangThai());
        }

        // Cập nhật các mối quan hệ (Chỉ gán nếu tồn tại trong payload)
        if (dataTuJavaScript.getSanPham() != null) {
            spctTrongDB.setSanPham(dataTuJavaScript.getSanPham());
        }
        if (dataTuJavaScript.getMauSac() != null) {
            spctTrongDB.setMauSac(dataTuJavaScript.getMauSac());
        }
        if (dataTuJavaScript.getKichThuoc() != null) {
            spctTrongDB.setKichThuoc(dataTuJavaScript.getKichThuoc());
        }
        if (dataTuJavaScript.getChatLieu() != null) {
            spctTrongDB.setChatLieu(dataTuJavaScript.getChatLieu());
        }
        if (dataTuJavaScript.getXuatXu() != null) {
            spctTrongDB.setXuatXu(dataTuJavaScript.getXuatXu());
        }
        if (dataTuJavaScript.getTheLoai() != null) {
            spctTrongDB.setTheLoai(dataTuJavaScript.getTheLoai());
        }
        if (dataTuJavaScript.getPhanLoai() != null) {
            spctTrongDB.setPhanLoai(dataTuJavaScript.getPhanLoai());
        }

        try {
            // 3. Lưu vào DB
            SanPhamChiTiet updatedSpct = sanPhamCTService.save(spctTrongDB);
            return ResponseEntity.ok(updatedSpct);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    /**
     * GET: Lấy thông tin chi tiết của một Biến thể theo ID
     */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<SanPhamChiTiet> getById(@PathVariable Integer id) {
        return sanPhamCTService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * DELETE: Xóa một Biến thể theo ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVariant(@PathVariable Integer id) {
        // 1. Kiểm tra sự tồn tại
        if (sanPhamCTService.getById(id).isEmpty()) {
            return new ResponseEntity<>("Không tìm thấy biến thể với ID: " + id, HttpStatus.NOT_FOUND);
        }
        try {
            // 2. Xóa
            sanPhamCTService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // Xử lý lỗi nếu biến thể đang được tham chiếu (ví dụ: trong Hóa đơn)
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Không thể xóa biến thể này vì đang được sử dụng.");
        }
    }

    /**
     * GET: Tìm kiếm danh sách Biến thể dựa trên Tên Sản phẩm cha
     */
    @GetMapping("/search")
    public List<SanPhamChiTiet> searchBySanPhamTen(@RequestParam("tenSp") String tenSp) {
        return sanPhamCTService.searchBySanPhamTen(tenSp);
    }
}