package sd_04.datn_fstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.services.HoaDonService;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/hoadon")
@CrossOrigin("*")
@RequiredArgsConstructor
public class HoaDonController {

    private final HoaDonService hoaDonService;

    // ✅ Lấy toàn bộ hóa đơn
    @GetMapping("/list")
    public ResponseEntity<List<HoaDon>> getAll() {
        return ResponseEntity.ok(hoaDonService.getAll());
    }

    // ✅ Lấy hóa đơn theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        Optional<HoaDon> hoaDon = hoaDonService.getById(id);
        return hoaDon.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ Lọc theo trạng thái
    @GetMapping("/trangthai")
    public ResponseEntity<List<HoaDon>> getByTrangThai(@RequestParam Integer trangThai) {
        return ResponseEntity.ok(hoaDonService.getByTrangThai(trangThai));
    }

    // ✅ Lọc theo khoảng thời gian
    @GetMapping("/date")
    public ResponseEntity<List<HoaDon>> getByDate(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date end) {
        return ResponseEntity.ok(hoaDonService.getByDateRange(start, end));
    }

    // ✅ Thêm mới hóa đơn
    @PostMapping("/add")
    public ResponseEntity<HoaDon> add(@RequestBody HoaDon hoaDon) {
        return ResponseEntity.ok(hoaDonService.add(hoaDon));
    }

    // ✅ Cập nhật hóa đơn
    @PutMapping("/update/{id}")
    public ResponseEntity<HoaDon> update(@PathVariable Integer id, @RequestBody HoaDon hoaDon) {
        return ResponseEntity.ok(hoaDonService.update(id, hoaDon));
    }

    // ✅ Xóa hóa đơn
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        hoaDonService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
