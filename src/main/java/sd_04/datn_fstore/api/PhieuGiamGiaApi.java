package sd_04.datn_fstore.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.model.PhieuGiamGia;
import sd_04.datn_fstore.service.PhieuGiamgiaService;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/phieu-giam-gia")
public class PhieuGiamGiaApi {
    @Autowired
    private PhieuGiamgiaService phieuGiamGiaService;

    @GetMapping
    public Page<PhieuGiamGia> getPromotions(
            // SỬA: Bỏ required=true (mặc định) để cho phép trangThai là NULL
            @RequestParam(name = "trangThai", required = false) Integer trangThai,
            @RequestParam(name = "keyword", required = false) Optional<String> keyword,
            @RequestParam(name = "ngayBatDau", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime ngayBatDau,
            @RequestParam(name = "ngayKetThuc", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime ngayKetThuc,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "sortField", defaultValue = "id") String sortField,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir) {

        String searchKeyword = keyword.orElse(null);

        // Vẫn truyền trangThai (có thể là NULL) vào service
        return phieuGiamGiaService.searchAndFilter(
                trangThai, // <--- trangThai có thể là NULL
                searchKeyword,
                ngayBatDau,
                ngayKetThuc,
                page,
                size,
                sortField,
                sortDir);
    }

    @PostMapping
    public ResponseEntity<PhieuGiamGia> createPromotion(@RequestBody PhieuGiamGia phieuGiamGia) {
        // Service sẽ tính toán và gán trạng thái trước khi lưu
        PhieuGiamGia savedPgg = phieuGiamGiaService.saveWithStatusCheck(phieuGiamGia);
        return new ResponseEntity<>(savedPgg, HttpStatus.CREATED);
    }
}
