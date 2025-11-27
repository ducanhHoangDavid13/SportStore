package sd_04.datn_fstore.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sd_04.datn_fstore.model.SanPham;
import sd_04.datn_fstore.repository.SanPhamRepository;
import sd_04.datn_fstore.service.ExcelService;
import sd_04.datn_fstore.service.FileStorageService;
import sd_04.datn_fstore.service.HinhAnhService;
import sd_04.datn_fstore.service.SanPhamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import sd_04.datn_fstore.model.HinhAnh;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private  final ExcelService excelService;

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

    @PostMapping("/create-with-image")
    public ResponseEntity<?> createWithImage(
            @RequestParam("sanPhamData") String sanPhamDataJson,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            SanPham sanPham = objectMapper.readValue(sanPhamDataJson, SanPham.class);

            if (sanPham.getMaSanPham() != null && !sanPham.getMaSanPham().trim().isEmpty()
                    && sanPhamService.existsByMaSanPham(sanPham.getMaSanPham())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Mã sản phẩm đã tồn tại.");
            }

            sanPham.setNgayTao(LocalDateTime.now());

            SanPham savedSanPham = sanPhamService.save(sanPham);

            if (file != null && !file.isEmpty()) {
                String fileName = fileStorageService.storeFile(file);

                HinhAnh hinhAnh = new HinhAnh();
                hinhAnh.setTenHinhAnh(fileName);
                hinhAnh.setTrangThai(1);
                hinhAnh.setSanPham(savedSanPham);
                hinhAnhService.save(hinhAnh);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(savedSanPham);

        } catch (Exception e) {
            log.error("Lỗi khi thêm sản phẩm và ảnh: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi thêm mới: " + e.getMessage());
        }
    }

    @PutMapping(value = "/update-with-image/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateWithImage(
            @PathVariable Integer id,
            @RequestParam("sanPhamData") String sanPhamDataJson,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Optional<SanPham> optionalSanPham = sanPhamService.getById(id);
            if (optionalSanPham.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            SanPham existingSanPham = optionalSanPham.get();

            SanPham sanPhamDetails = objectMapper.readValue(sanPhamDataJson, SanPham.class);

            existingSanPham.setMaSanPham(sanPhamDetails.getMaSanPham());
            existingSanPham.setTenSanPham(sanPhamDetails.getTenSanPham());
            existingSanPham.setGiaTien(sanPhamDetails.getGiaTien());
            existingSanPham.setSoLuong(sanPhamDetails.getSoLuong());
            existingSanPham.setMoTa(sanPhamDetails.getMoTa());
            existingSanPham.setTrangThai(sanPhamDetails.getTrangThai());

            SanPham updatedSanPham = sanPhamService.save(existingSanPham);

            if (file != null && !file.isEmpty()) {
                String fileName = fileStorageService.storeFile(file);

                HinhAnh hinhAnh = new HinhAnh();
                hinhAnh.setTenHinhAnh(fileName);
                hinhAnh.setTrangThai(1);
                hinhAnh.setSanPham(updatedSanPham);
                hinhAnhService.save(hinhAnh);
            }

            return ResponseEntity.ok(updatedSanPham);

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Lỗi dữ liệu (trùng mã...).");
        } catch (Exception e) {
            log.error("Error updating SanPham with Image: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody SanPham sanPhamDetails) {
        try {
            Optional<SanPham> optionalSanPham = sanPhamService.getById(id);
            if (optionalSanPham.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            SanPham existingSanPham = optionalSanPham.get();

            existingSanPham.setTenSanPham(sanPhamDetails.getTenSanPham());
            existingSanPham.setMaSanPham(sanPhamDetails.getMaSanPham());
            existingSanPham.setGiaTien(sanPhamDetails.getGiaTien());
            existingSanPham.setSoLuong(sanPhamDetails.getSoLuong());
            existingSanPham.setMoTa(sanPhamDetails.getMoTa());
            existingSanPham.setTrangThai(sanPhamDetails.getTrangThai());

            SanPham updatedSanPham = sanPhamService.save(existingSanPham);
            return ResponseEntity.ok(updatedSanPham);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi cập nhật.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            if (!sanPhamService.getById(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }
            sanPhamRepository.deleteById(id);
            return ResponseEntity.ok("Xóa thành công");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Không thể xóa sản phẩm này vì đang được sử dụng.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa.");
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<SanPham>> getProducts(
            @RequestParam(value = "xuatXuIds", required = false) List<Integer> xuatXuIds,
            @RequestParam(value = "theLoaiIds", required = false) List<Integer> theLoaiIds,
            @RequestParam(value = "phanLoaiIds", required = false) List<Integer> phanLoaiIds,
            @RequestParam(value = "chatLieuIds", required = false) List<Integer> chatLieuIds,
            @RequestParam(value = "minPrice", required = false, defaultValue = "0") BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false, defaultValue = "999999999") BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort
    ) {
        List<Integer> finalXuatXuIds = Optional.ofNullable(xuatXuIds).filter(list -> !list.isEmpty()).orElse(null);
        List<Integer> finalTheLoaiIds = Optional.ofNullable(theLoaiIds).filter(list -> !list.isEmpty()).orElse(null);
        List<Integer> finalPhanLoaiIds = Optional.ofNullable(phanLoaiIds).filter(list -> !list.isEmpty()).orElse(null);
        List<Integer> finalChatLieuIds = Optional.ofNullable(chatLieuIds).filter(list -> !list.isEmpty()).orElse(null);

        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        Pageable pageable = PageRequest.of(page, size, sortOrder);

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
    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateTrangThai(
            @PathVariable("id") Integer id,
            @RequestParam("trangThai") Integer trangThai) {
        try {
            SanPham updatedSanPham = sanPhamService.updateTrangThai(id, trangThai);
            return ResponseEntity.ok(updatedSanPham);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }
    @GetMapping("/export/excel")
    public ResponseEntity<InputStreamResource> exportExcel() {
        List<SanPham> list = sanPhamService.getAll();
        ByteArrayInputStream in = excelService.exportSanPhamToExcel(list);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=san_pham.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}