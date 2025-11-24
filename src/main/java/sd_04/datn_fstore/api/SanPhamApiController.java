package sd_04.datn_fstore.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sd_04.datn_fstore.model.SanPham;
import sd_04.datn_fstore.repository.SanPhamRepository;
import sd_04.datn_fstore.service.FileStorageService;
import sd_04.datn_fstore.service.HinhAnhService;
import sd_04.datn_fstore.service.SanPhamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import sd_04.datn_fstore.model.HinhAnh;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime; // <-- SỬA 1: Đổi import
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class SanPhamApiController {

    private final SanPhamService sanPhamService;
    private  final SanPhamRepository sanPhamRepository;
    private final FileStorageService fileStorageService;
    private final HinhAnhService hinhAnhService;

    /**
     * API: Lấy danh sách sản phẩm (phân trang, tìm kiếm, lọc)
     */
    @GetMapping
    public ResponseEntity<?> search(
            Pageable pageable,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "trangThai", required = false) Integer trangThai) {
        try {
            Page<SanPham> sanPhamPage = sanPhamService.searchAndPaginate(pageable, keyword, trangThai);
            return ResponseEntity.ok(sanPhamPage);
        } catch (Exception e) {
            log.error("Error searching SanPham: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tìm kiếm sản phẩm.");
        }
    }

    /**
     * API: Lấy TẤT CẢ sản phẩm (dùng cho dropdown)
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        try {
            List<SanPham> sanPhams = sanPhamRepository.findAll();
            return ResponseEntity.ok(sanPhams);
        } catch (Exception e) {
            log.error("Error getting all SanPham: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách sản phẩm.");
        }
    }



    /**
     * API: Lấy chi tiết 1 sản phẩm
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        try {
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

    /**
     * API: Thêm mới 1 sản phẩm
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody SanPham sanPham) {
        try {
            // (Validate đầu vào...)
            if (sanPham.getTenSanPham() == null || sanPham.getTenSanPham().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Tên sản phẩm không được để trống.");
            }
            // (Kiểm tra trùng mã...)
            if (sanPham.getMaSanPham() != null && !sanPham.getMaSanPham().trim().isEmpty()
                    && sanPhamService.existsByMaSanPham(sanPham.getMaSanPham())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Mã sản phẩm đã tồn tại.");
            }

            // ----- SỬA 2: Dùng LocalDateTime.now() -----
            sanPham.setNgayTao(LocalDateTime.now());
            // ------------------------------------------

            SanPham savedSanPham = sanPhamService.save(sanPham);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedSanPham);

        } catch (DataIntegrityViolationException e) {
            log.error("Database integrity violation while creating SanPham: ", e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Lỗi ràng buộc dữ liệu, có thể mã sản phẩm đã tồn tại.");
        } catch (Exception e) {
            log.error("Error creating SanPham: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi thêm mới sản phẩm.");
        }
    }

    /**
     * API: Cập nhật 1 sản phẩm
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody SanPham sanPhamDetails) {
        try {
            // (Validate và kiểm tra trùng mã...)

            Optional<SanPham> optionalSanPham = sanPhamService.getById(id);
            if (optionalSanPham.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            SanPham existingSanPham = optionalSanPham.get();

            // (Cập nhật các trường...)
            existingSanPham.setTenSanPham(sanPhamDetails.getTenSanPham());
            existingSanPham.setMaSanPham(sanPhamDetails.getMaSanPham());
            // ... (các trường khác)

            SanPham updatedSanPham = sanPhamService.save(existingSanPham);
            return ResponseEntity.ok(updatedSanPham);

        } catch (DataIntegrityViolationException e) {
            log.error("Database integrity violation while updating SanPham ID {}: ", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Lỗi ràng buộc dữ liệu khi cập nhật.");
        } catch (Exception e) {
            log.error("Error updating SanPham ID {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật sản phẩm.");
        }
    }

    @PostMapping("/create-with-image")
    public ResponseEntity<?> createWithImage(
            @RequestParam("sanPhamData") String sanPhamDataJson,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // 1. Chuyển đổi JSON string thành đối tượng SanPham Entity
            SanPham sanPham = objectMapper.readValue(sanPhamDataJson, SanPham.class);

            // ----- SỬA 3: Dùng LocalDateTime.now() -----
            sanPham.setNgayTao(LocalDateTime.now());
            // ------------------------------------------

            SanPham savedSanPham = sanPhamService.save(sanPham);

            // 3. Nếu có file, lưu file và tạo Entity HinhAnh
            if (file != null && !file.isEmpty()) {
                String fileName = fileStorageService.storeFile(file);

                HinhAnh hinhAnh = new HinhAnh();
                hinhAnh.setTenHinhAnh(fileName);
                hinhAnh.setTrangThai(1); // Ảnh đại diện ban đầu
                hinhAnh.setSanPham(savedSanPham); // Gán khóa ngoại SP
                hinhAnhService.save(hinhAnh);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(savedSanPham);

        } catch (Exception e) {
            log.error("Lỗi khi thêm sản phẩm và ảnh: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi thêm mới sản phẩm và hình ảnh: " + e.getMessage());
        }
    }

    /**
     * API: Xóa 1 sản phẩm
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        // ... (Logic xóa)
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<SanPham>> getProducts(
            @RequestParam(value = "xuatXuIds", required = false) List<Integer> xuatXuIds, // Sửa Long -> Integer
            @RequestParam(value = "theLoaiIds", required = false) List<Integer> theLoaiIds, // Sửa Long -> Integer
            @RequestParam(value = "phanLoaiIds", required = false) List<Integer> phanLoaiIds, // Sửa Long -> Integer
            @RequestParam(value = "chatLieuIds", required = false) List<Integer> chatLieuIds, // Sửa Long -> Integer
            @RequestParam(value = "minPrice", required = false, defaultValue = "0") BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false, defaultValue = "999999999") BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort
    ) {
        // 1. Xử lý logic List rỗng thành NULL (Sử dụng Integer List)
        List<Integer> finalXuatXuIds = Optional.ofNullable(xuatXuIds).filter(list -> !list.isEmpty()).orElse(null);
        List<Integer> finalTheLoaiIds = Optional.ofNullable(theLoaiIds).filter(list -> !list.isEmpty()).orElse(null);
        List<Integer> finalPhanLoaiIds = Optional.ofNullable(phanLoaiIds).filter(list -> !list.isEmpty()).orElse(null);
        List<Integer> finalChatLieuIds = Optional.ofNullable(chatLieuIds).filter(list -> !list.isEmpty()).orElse(null);

        // 2. Xử lý tham số sort và Pageable (Giữ nguyên)
        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        // 3. Gọi Repository trực tiếp với các tham số ĐÃ XỬ LÝ
        Page<SanPham> productsPage = sanPhamRepository.findFilteredProducts(
                finalXuatXuIds,
                finalTheLoaiIds,
                finalPhanLoaiIds,
                finalChatLieuIds,
                minPrice,
                maxPrice,
                pageable);

        return ResponseEntity.ok(productsPage);
    }
}