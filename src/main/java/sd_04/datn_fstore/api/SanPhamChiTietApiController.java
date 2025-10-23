package sd_04.datn_fstore.api;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.model.SanPhamChiTiet;
import sd_04.datn_fstore.service.SanPhamCTService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/san-pham-chi-tiet")
@RequiredArgsConstructor
@CrossOrigin("*")
public class SanPhamChiTietApiController {

    private final SanPhamCTService sanPhamCTService;

    // API này sẽ được JS gọi để lấy dữ liệu cho modal "Sửa" SPCT
    @GetMapping("/{id}")
    public ResponseEntity<SanPhamChiTiet> getById(@PathVariable Integer id) {
        return sanPhamCTService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

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
            @RequestParam(required = false) BigDecimal giaMin,
            @RequestParam(required = false) BigDecimal giaMax,
            @RequestParam(required = false) Integer trangThai) {

        Page<SanPhamChiTiet> spctPage = sanPhamCTService.search(
                pageable, idSanPham, idKichThuoc, idPhanLoai, idXuatXu,
                idChatLieu, idMauSac, idTheLoai, giaMin, giaMax, trangThai
        );
        return ResponseEntity.ok(spctPage);
    }

    // (Các API khác giữ nguyên)
}