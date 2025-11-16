
package sd_04.datn_fstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.dto.NhanVienRequest;
import sd_04.datn_fstore.enums.RoleEnum;
import sd_04.datn_fstore.model.Account;
import sd_04.datn_fstore.model.NhanVien;
import sd_04.datn_fstore.repository.AccountRepository;
import sd_04.datn_fstore.repository.NhanVienRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/nhanvien")
@CrossOrigin(origins = "*") // Cho phép frontend truy cập
@RequiredArgsConstructor
public class NhanVienController {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    // ✅ 1. Lấy toàn bộ danh sách nhân viên
    @GetMapping("/list")
    public ResponseEntity<List<NhanVien>> getAll() {
        List<NhanVien> list = nhanVienRepository.findAll();
        return ResponseEntity.ok(list);
    }

    // ✅ 2. Tìm kiếm (theo tên, mã, vai trò, trạng thái)
    @GetMapping("/search")
    public ResponseEntity<List<NhanVien>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String vaiTro,
            @RequestParam(required = false) Integer trangThai) {

        List<NhanVien> list = nhanVienRepository.findAll();

        // Lọc bằng Java Stream
        List<NhanVien> result = list.stream()
                .filter(nv -> {
                    boolean match = true;

                    if (keyword != null && !keyword.trim().isEmpty()) {
                        match = nv.getTenNhanVien().toLowerCase().contains(keyword.toLowerCase())
                                || nv.getMaNhanVien().toLowerCase().contains(keyword.toLowerCase());
                    }
                    if (vaiTro != null && !vaiTro.trim().isEmpty()) {
                        match = match && nv.getVaiTro().equalsIgnoreCase(vaiTro);
                    }
                    if (trangThai != null) {
                        match = match && nv.getTrangThai() != null && nv.getTrangThai().equals(trangThai);
                    }
                    return match;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ✅ 3. Lấy nhân viên theo ID
    @GetMapping("/{id}")
    public ResponseEntity<NhanVien> getById(@PathVariable Integer id) {
        Optional<NhanVien> nv = nhanVienRepository.findById(id);
        return nv.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ 4. Thêm mới nhân viên
    @PostMapping("/add")
    public ResponseEntity<NhanVien> add(@RequestBody NhanVienRequest nhanVien) {
        if (nhanVien.getMaNhanVien() == null || nhanVien.getTenNhanVien() == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Account> account = accountRepository.findByEmail(nhanVien.getEmail());
        if (account.isEmpty()) {
            accountRepository.save(Account.builder()
                    .email(nhanVien.getEmail())
                    .password(passwordEncoder.encode(nhanVien.getPassword()))
                    .role(RoleEnum.EMPLOYEE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }

        NhanVien save = new NhanVien();
        save.setMaNhanVien(nhanVien.getMaNhanVien());
        save.setTenNhanVien(nhanVien.getTenNhanVien());
        save.setEmail(nhanVien.getEmail());
        save.setSoDienThoai(nhanVien.getSoDienThoai());
        save.setDiaChi(nhanVien.getDiaChi());
        save.setVaiTro(nhanVien.getVaiTro());
        save.setTrangThai(nhanVien.getTrangThai());
        save.setGioiTinh(nhanVien.getGioiTinh());
        save.setCccd(nhanVien.getCccd());
        save.setHinhAnh(nhanVien.getHinhAnh());
        NhanVien response = nhanVienRepository.save(save);
        return ResponseEntity.ok(response);
    }

    // ✅ 5. Cập nhật nhân viên
    @PutMapping("/update/{id}")
    public ResponseEntity<NhanVien> update(@PathVariable Integer id, @RequestBody NhanVien nhanVien) {
        Optional<NhanVien> optionalNV = nhanVienRepository.findById(id);
        if (optionalNV.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        NhanVien existing = optionalNV.get();

        existing.setMaNhanVien(nhanVien.getMaNhanVien());
        existing.setTenNhanVien(nhanVien.getTenNhanVien());
        existing.setEmail(nhanVien.getEmail());
        existing.setSoDienThoai(nhanVien.getSoDienThoai());
        existing.setDiaChi(nhanVien.getDiaChi());
        existing.setVaiTro(nhanVien.getVaiTro());
        existing.setTrangThai(nhanVien.getTrangThai());
        existing.setGioiTinh(nhanVien.getGioiTinh());
        existing.setCccd(nhanVien.getCccd());
        existing.setHinhAnh(nhanVien.getHinhAnh());

        NhanVien updated = nhanVienRepository.save(existing);
        return ResponseEntity.ok(updated);
    }

    // ✅ 6. Xóa nhân viên
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!nhanVienRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        nhanVienRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}