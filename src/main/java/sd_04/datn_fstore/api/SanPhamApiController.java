package sd_04.datn_fstore.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sd_04.datn_fstore.model.HinhAnh;
import sd_04.datn_fstore.model.SanPham;
import sd_04.datn_fstore.repository.SanPhamRepository;
import sd_04.datn_fstore.service.ExcelService;
import sd_04.datn_fstore.service.FileStorageService;
import sd_04.datn_fstore.service.HinhAnhService;
import sd_04.datn_fstore.service.SanPhamService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class SanPhamApiController {

    private final SanPhamService sanPhamService;
    private final SanPhamRepository sanPhamRepository;
    private final FileStorageService fileStorageService;
    private final HinhAnhService hinhAnhService;
    private final ExcelService excelService;

    // =========================================================
    // 🔍 HÀM LỌC NÂNG CAO
    // =========================================================
    @GetMapping("/filter")
    public ResponseEntity<Page<SanPham>> filterProducts(
            @RequestParam(value = "xuatXuIds", required = false) List<Integer> xuatXuIds,
            @RequestParam(value = "theLoaiIds", required = false) List<Integer> theLoaiIds,
            @RequestParam(value = "phanLoaiIds", required = false) List<Integer> phanLoaiIds,
            @RequestParam(value = "chatLieuIds", required = false) List<Integer> chatLieuIds,
            @RequestParam(value = "minPrice", required = false, defaultValue = "0") BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false, defaultValue = "999999999") BigDecimal maxPrice,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort
    ) {
        // ... (Logic xử lý tham số lọc và Pageable)

        List<Integer> finalXuatXuIds = Optional.ofNullable(xuatXuIds).filter(list -> !list.isEmpty()).orElse(null);
        List<Integer> finalTheLoaiIds = Optional.ofNullable(theLoaiIds).filter(list -> !list.isEmpty()).orElse(null);
        List<Integer> finalPhanLoaiIds = Optional.ofNullable(phanLoaiIds).filter(list -> !list.isEmpty()).orElse(null);
        List<Integer> finalChatLieuIds = Optional.ofNullable(chatLieuIds).filter(list -> !list.isEmpty()).orElse(null);

        String finalKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        // Service/Repository phải đảm bảo SanPham trả về đã được cập nhật soLuong.
        Page<SanPham> productsPage = sanPhamRepository.findFilteredProducts(
                finalXuatXuIds,
                finalTheLoaiIds,
                finalPhanLoaiIds,
                finalChatLieuIds,
                minPrice,
                maxPrice,
                finalKeyword,
                pageable);

        return ResponseEntity.ok(productsPage);
    }

    // =========================================================
    // 🔎 HÀM TÌM KIẾM & LỌC CƠ BẢN
    // =========================================================
    @GetMapping
    public ResponseEntity<?> search(
            Pageable pageable,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "trangThai", required = false) Integer trangThai) {
        try {
            // Service phải đảm bảo SanPham trả về đã được cập nhật soLuong.
            Page<SanPham> sanPhamPage = sanPhamService.searchAndPaginate(pageable, keyword, trangThai);
            return ResponseEntity.ok(sanPhamPage);
        } catch (Exception e) {
            log.error("Error searching SanPham: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tìm kiếm sản phẩm.");
        }
    }
// Trong SanPhamApiController.java

    @GetMapping("/export/excel")
    public ResponseEntity<InputStreamResource> exportExcel() {
        // 1. Lấy TẤT CẢ sản phẩm (sanPhamService.getAll())
        List<SanPham> list = sanPhamService.getAll();

        // 2. Gọi ExcelService tạo file
        ByteArrayInputStream in = excelService.exportSanPhamToExcel(list);

        // 3. Cấu hình Headers và trả về file
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=san_pham.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                // Đảm bảo kiểu dữ liệu đúng cho file Excel
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    // =========================================================
    // 👁️ HÀM LẤY CHI TIẾT THEO ID
    // =========================================================
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        try {
            // Service phải đảm bảo SanPham trả về đã được cập nhật soLuong.
            Optional<SanPham> sanPhamOptional = sanPhamService.getById(id);
            if (sanPhamOptional.isPresent()) {
                return ResponseEntity.ok(sanPhamOptional.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting SanPham by ID {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy chi tiết sản phẩm.");
        }
    }

    // =========================================================
    // 🚀 HÀM THÊM MỚI SẢN PHẨM KÈM ẢNH (CREATE)
    // =========================================================
    @PostMapping("/create-with-image")
    public ResponseEntity<?> createWithImage(
            @RequestPart("sanPhamData") String sanPhamDataJson,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            SanPham sanPham = objectMapper.readValue(sanPhamDataJson, SanPham.class);

            if (sanPham.getMaSanPham() != null && !sanPham.getMaSanPham().trim().isEmpty()
                    && sanPhamService.existsByMaSanPham(sanPham.getMaSanPham())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Mã sản phẩm đã tồn tại.");
            }

            sanPham.setNgayTao(LocalDateTime.now());
            // **Quan trọng:** Khi tạo sản phẩm, soLuong phải được thiết lập là 0
            // vì chưa có biến thể nào được thêm vào.
            sanPham.setSoLuong(0);

            // 1. Lưu sản phẩm trước để có ID
            SanPham savedSanPham = sanPhamService.save(sanPham);

            // 2. Xử lý File và Lưu HinhAnh nếu có
            if (file != null && !file.isEmpty()) {
                String fileName = fileStorageService.storeFile(file);

                HinhAnh hinhAnh = new HinhAnh();
                hinhAnh.setTenHinhAnh(fileName);
                hinhAnh.setSanPham(savedSanPham);
                hinhAnh.setNgayTao(LocalDateTime.now());

                hinhAnhService.save(hinhAnh);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(savedSanPham);

        } catch (IOException e) {
            log.error("Lỗi khi chuyển đổi JSON thành đối tượng SanPham: ", e);
            return ResponseEntity.badRequest().body("Dữ liệu sản phẩm không hợp lệ.");
        } catch (Exception e) {
            log.error("Lỗi khi thêm sản phẩm và ảnh: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi thêm mới: " + e.getMessage());
        }
    }


    // =========================================================
    // 📝 HÀM CẬP NHẬT SẢN PHẨM KÈM ẢNH (UPDATE)
    // =========================================================
    @PutMapping(value = "/update-with-image/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateWithImage(
            @PathVariable Integer id,
            @RequestPart("sanPhamData") String sanPhamDataJson,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Optional<SanPham> optionalSanPham = sanPhamService.getById(id);
            if (optionalSanPham.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            SanPham existingSanPham = optionalSanPham.get();
            SanPham sanPhamDetails = objectMapper.readValue(sanPhamDataJson, SanPham.class);

            // 1. LOGIC CHECK TRÙNG MÃ SẢN PHẨM KHI UPDATE
            String newMaSanPham = sanPhamDetails.getMaSanPham();
            if (newMaSanPham != null && !newMaSanPham.trim().isEmpty() &&
                    !newMaSanPham.equalsIgnoreCase(existingSanPham.getMaSanPham())) {
                if (sanPhamService.existsByMaSanPham(newMaSanPham)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Mã sản phẩm đã tồn tại với sản phẩm khác.");
                }
            }

            // 2. Cập nhật các trường chính (BỎ QUA soLuong từ Request Body)
            existingSanPham.setMaSanPham(newMaSanPham);
            existingSanPham.setTenSanPham(sanPhamDetails.getTenSanPham());
            existingSanPham.setGiaTien(sanPhamDetails.getGiaTien());
            existingSanPham.setMoTa(sanPhamDetails.getMoTa());
            existingSanPham.setTrangThai(sanPhamDetails.getTrangThai());

            // 3. Lưu sản phẩm đã cập nhật (SoLuong sẽ được giữ nguyên hoặc được Service tính toán)
            SanPham updatedSanPham = sanPhamService.save(existingSanPham);

            // 4. Xử lý File
            if (file != null && !file.isEmpty()) {
                // Xóa avatar cũ trước khi thêm mới
                hinhAnhService.deleteAvatarBySanPhamId(updatedSanPham.getId());
                String fileName = fileStorageService.storeFile(file);

                HinhAnh hinhAnh = new HinhAnh();
                hinhAnh.setTenHinhAnh(fileName);
                hinhAnh.setSanPham(updatedSanPham);
                hinhAnh.setNgayTao(LocalDateTime.now());

                hinhAnhService.save(hinhAnh);
            }

            return sanPhamService.getById(updatedSanPham.getId())
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.ok(updatedSanPham));


        } catch (IOException e) {
            log.error("Lỗi khi chuyển đổi JSON thành đối tượng SanPham: ", e);
            return ResponseEntity.badRequest().body("Dữ liệu cập nhật sản phẩm không hợp lệ.");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Lỗi dữ liệu (trùng mã...)");
        } catch (Exception e) {
            log.error("Error updating SanPham with Image: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    // =========================================================
    // ✏️ HÀM CẬP NHẬT KHÔNG KÈM ẢNH
    // =========================================================
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody SanPham sanPhamDetails) {
        try {
            Optional<SanPham> optionalSanPham = sanPhamService.getById(id);
            if (optionalSanPham.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            SanPham existingSanPham = optionalSanPham.get();

            String newMaSanPham = sanPhamDetails.getMaSanPham();
            if (newMaSanPham != null && !newMaSanPham.trim().isEmpty() &&
                    !newMaSanPham.equalsIgnoreCase(existingSanPham.getMaSanPham())) {
                if (sanPhamService.existsByMaSanPham(newMaSanPham)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Mã sản phẩm đã tồn tại với sản phẩm khác.");
                }
            }

            existingSanPham.setTenSanPham(sanPhamDetails.getTenSanPham());
            existingSanPham.setMaSanPham(sanPhamDetails.getMaSanPham());
            existingSanPham.setGiaTien(sanPhamDetails.getGiaTien());
            existingSanPham.setMoTa(sanPhamDetails.getMoTa());
            existingSanPham.setTrangThai(sanPhamDetails.getTrangThai());

            SanPham updatedSanPham = sanPhamService.save(existingSanPham);

            return sanPhamService.getById(updatedSanPham.getId())
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.ok(updatedSanPham));

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Lỗi dữ liệu (trùng mã...)");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi cập nhật.");
        }
    }

    // =========================================================
    // 🗑️ HÀM XÓA SẢN PHẨM
    // =========================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            if (!sanPhamService.getById(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }
            sanPhamService.delete(id);
            return ResponseEntity.ok("Xóa thành công");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Không thể xóa sản phẩm này vì đang được sử dụng.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa.");
        }
    }

    // =========================================================
    // 🔄 HÀM CẬP NHẬT TRẠNG THÁI RIÊNG
    // =========================================================
    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateTrangThai(
            @PathVariable("id") Integer id,
            @RequestParam("trangThai") Integer trangThai) {
        try {
            // Logic cập nhật trạng thái
            SanPham updatedSanPham = sanPhamService.updateTrangThai(id, trangThai);

            // Thường thì việc cập nhật trạng thái không cần đồng bộ số lượng, nhưng
            // nếu cần thiết, service có thể tự xử lý hoặc gọi lại sync-quantity.

            return ResponseEntity.ok(updatedSanPham);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // =========================================================
    // 🔄 HÀM ĐỒNG BỘ TỔNG SỐ LƯỢNG (SYNC TOTAL QUANTITY)
    // =========================================================
    /**
     * Endpoint API cho phép kích hoạt việc đồng bộ lại tổng số lượng tồn kho
     * của một SanPham cụ thể dựa trên tổng số lượng của tất cả SanPhamChiTiet liên quan.
     * Sử dụng cho mục đích thủ công hoặc kiểm tra/khắc phục sự cố dữ liệu.
     *
     * @param sanPhamId ID của SanPham cần đồng bộ
     * @return ResponseEntity thông báo kết quả
     */
    @PutMapping("/{sanPhamId}/sync-quantity")
    public ResponseEntity<String> syncTotalQuantity(@PathVariable Integer sanPhamId) {
        try {
            if (!sanPhamService.getById(sanPhamId).isPresent()) {
                return ResponseEntity.notFound().build();
            }

            // Gọi phương thức từ Service để tính toán và cập nhật số lượng
            sanPhamService.updateTotalQuantity(sanPhamId);

            return ResponseEntity.ok("Đã đồng bộ thành công tổng số lượng tồn kho cho SanPham ID: " + sanPhamId);
        } catch (Exception e) {
            log.error("Lỗi khi đồng bộ số lượng tồn kho cho SanPham ID {}: ", sanPhamId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi đồng bộ số lượng: " + e.getMessage());
        }
    }


    // =========================================================
    // 📊 HÀM EXPORT EXCEL
    // =========================================================

}