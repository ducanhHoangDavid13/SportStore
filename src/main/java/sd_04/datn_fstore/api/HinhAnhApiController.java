package sd_04.datn_fstore.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import sd_04.datn_fstore.model.HinhAnh;
import sd_04.datn_fstore.model.SanPham;
import sd_04.datn_fstore.service.FileStorageService;
import sd_04.datn_fstore.service.HinhAnhService;
import sd_04.datn_fstore.service.SanPhamService;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime; // <-- Đã thay đổi import
import java.util.List;

@RestController
@RequestMapping("/api/hinh-anh")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class HinhAnhApiController {

    private final HinhAnhService hinhAnhService;
    private final FileStorageService fileStorageService;
    private final SanPhamService sanPhamService;

    // ... (API 1 và API 2 giữ nguyên) ...

    @GetMapping("/all")
    public ResponseEntity<List<HinhAnh>> getAllHinhAnh() {
        try {
            List<HinhAnh> hinhAnhs = hinhAnhService.getAll();
            return ResponseEntity.ok(hinhAnhs);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách Hình ảnh: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/san-pham/{idSanPham}")
    public ResponseEntity<List<HinhAnh>> getImagesBySanPhamId(@PathVariable Integer idSanPham) {
        try {
            List<HinhAnh> hinhAnhs = hinhAnhService.getBySanPhamId(idSanPham);
            return ResponseEntity.ok(hinhAnhs);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách Hình ảnh theo SP ID {}: ", idSanPham, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API 3: Xử lý Upload file
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam(name = "idSanPham", required = false) Integer idSanPham,
                                        @RequestParam(name = "moTa", required = false) String moTa) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File không được để trống.");
        }
        try {
            // 1. Lưu file vật lý
            String fileName = fileStorageService.storeFile(file);

            // 2. Tạo Entity HinhAnh
            HinhAnh hinhAnh = new HinhAnh();
            hinhAnh.setTenHinhAnh(fileName);
            hinhAnh.setMoTa(moTa);
            hinhAnh.setTrangThai(1);

            // --- SỬA ĐỔI TẠI ĐÂY ---
            hinhAnh.setNgayTao(LocalDateTime.now()); // Sử dụng LocalDateTime.now()
            // -----------------------

            // 3. Gán SanPham
            if (idSanPham != null) {
                SanPham sanPham = sanPhamService.getById(idSanPham)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Sản phẩm"));
                hinhAnh.setSanPham(sanPham);
            }

            // 4. Lưu Entity vào DB
            HinhAnh savedHinhAnh = hinhAnhService.save(hinhAnh);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedHinhAnh);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            log.error("Lỗi khi upload file: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi upload file: " + e.getMessage());
        }
    }

    // ... (API 4 và API 5 giữ nguyên) ...

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Thêm kiểm tra null để tránh lỗi RuntimeException nếu file không tồn tại
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        String contentType = "application/octet-stream";
        try {
            contentType = Files.probeContentType(resource.getFile().toPath());
        } catch (IOException ex) {
            log.warn("Không thể xác định loại file cho: " + fileName);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHinhAnh(@PathVariable Integer id) {
        try {
            hinhAnhService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Lỗi khi xóa Hình ảnh ID " + id + ": ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}