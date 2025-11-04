//package sd_04.datn_fstore.api;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.io.Resource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
//import sd_04.datn_fstore.model.HinhAnh;
//import sd_04.datn_fstore.model.SanPham;
//import sd_04.datn_fstore.service.FileStorageService;
//import sd_04.datn_fstore.service.HinhAnhService;
//import sd_04.datn_fstore.service.SanPhamService; // Nếu cần gán SP vào HA
//
//import java.util.Date;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/hinh-anh")
//@RequiredArgsConstructor
//@CrossOrigin("*")
//@Slf4j
//public class HinhAnhApiController {
//
//    private final HinhAnhService hinhAnhService;
//    private final FileStorageService fileStorageService;
//    private final SanPhamService sanPhamService; // Giả sử cần dùng để gán HA cho SP
//
//    /**
//     * API 1: Lấy TẤT CẢ Hình ảnh (cho dropdown Khóa phụ)
//     */
//    @GetMapping("/all")
//    public ResponseEntity<List<HinhAnh>> getAllHinhAnh() {
//        try {
//            List<HinhAnh> hinhAnhs = hinhAnhService.getAll();
//            return ResponseEntity.ok(hinhAnhs);
//        } catch (Exception e) {
//            log.error("Lỗi khi lấy danh sách Hình ảnh: ", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    /**
//     * API 2: Xử lý Upload file (lưu file và tạo entity HinhAnh)
//     * Thường dùng khi thêm ảnh vào danh sách ảnh chi tiết của sản phẩm.
//     */
//    @PostMapping("/upload")
//    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
//                                        @RequestParam(name = "idSanPham", required = false) Integer idSanPham,
//                                        @RequestParam(name = "moTa", required = false) String moTa) {
//        try {
//            // 1. Lưu file vật lý, trả về tên file duy nhất
//            String fileName = fileStorageService.storeFile(file);
//
//            // 2. Tạo URL truy cập file (để lưu vào DB)
//            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
//                    .path("/api/hinh-anh/download/") // API tải file (cần thêm logic ở API loadFile)
//                    .path(fileName)
//                    .toUriString();
//
//            // 3. Tạo Entity HinhAnh
//            HinhAnh hinhAnh = new HinhAnh();
//            hinhAnh.setTenHinhAnh(fileName); // Lưu tên file duy nhất
//            hinhAnh.setMoTa(moTa);
//            hinhAnh.setNgayTao(new Date());
//            hinhAnh.setTrangThai(1); // Mặc định đang hoạt động
//
//            // 4. Gán SanPham (nếu idSanPham được cung cấp)
//            if (idSanPham != null) {
//                SanPham sanPham = sanPhamService.getById(idSanPham).orElse(null);
//                hinhAnh.setSanPham(sanPham);
//            }
//
//            // 5. Lưu Entity vào DB
//            HinhAnh savedHinhAnh = hinhAnhService.save(hinhAnh);
//
//            // Trả về thông tin HinhAnh đã lưu, bao gồm ID và tên file
//            return ResponseEntity.ok(savedHinhAnh);
//
//        } catch (Exception e) {
//            log.error("Lỗi khi upload file: ", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Lỗi khi upload file: " + e.getMessage());
//        }
//    }
//
//    /**
//     * API 3: Tải file (dùng để hiển thị ảnh trên web)
//     */
//    @GetMapping("/download/{fileName:.+}")
//    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
//        // Tải file dưới dạng Resource
//        Resource resource = fileStorageService.loadFileAsResource(fileName);
//
//        String contentType = "application/octet-stream"; // Loại mặc định
//        try {
//            // Cố gắng xác định Content Type
//            contentType = file.get.getContentType().get(resource.getFile().getAbsolutePath());
//        } catch (Exception ex) {
//            log.info("Không thể xác định loại file, sử dụng mặc định.");
//        }
//
//        // Trả về file dưới dạng ResponseEntity
//        return ResponseEntity.ok()
//                .contentType(MediaType.parseMediaType(contentType))
//                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"") // inline để hiển thị trực tiếp
//                .body(resource);
//    }
//}