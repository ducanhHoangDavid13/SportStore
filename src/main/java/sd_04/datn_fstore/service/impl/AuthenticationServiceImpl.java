package sd_04.datn_fstore.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.enums.RoleEnum;
import sd_04.datn_fstore.model.Account;
import sd_04.datn_fstore.model.KhachHang;
import sd_04.datn_fstore.model.NhanVien; // Kept from second commit
// The package for KhachHangRepo is the main point of conflict.
// Assuming 'sd_04.datn_fstore.repository.KhachHangRepo' (HEAD) is the correct full package path,
// but merging to 'sd_04.datn_fstore.repo.KhachHangRepo' (second commit) for resolution clarity.
// You must verify which package path is correct in your project structure.
import sd_04.datn_fstore.repository.KhachHangRepo; // Keeping this one, assuming 'repo' directory is correct
import sd_04.datn_fstore.repository.AccountRepository;
import sd_04.datn_fstore.repository.NhanVienRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl {

    private final KhachHangRepo khachHangRepo;
    private final NhanVienRepository nhanVienRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void postConstruct() {
        createAccount("admin@gmail.com", "admin", RoleEnum.ADMIN);
        createAccount("user@gmail.com", "user", RoleEnum.USDE);
    }


    private void createAccount(String email, String password, RoleEnum roleEnum) {
        Optional<Account> account = accountRepository.findByEmail(email);
        if (account.isEmpty()) {
            accountRepository.save(Account.builder()
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .role(roleEnum)
                    .build());
        }

        if (RoleEnum.ADMIN == roleEnum) {
            Optional<NhanVien> nhanVien = nhanVienRepository.findByEmail(email);
            if (nhanVien.isEmpty()) {
                NhanVien user = new NhanVien();
                user.setTenNhanVien(email);
                user.setEmail(email);
                nhanVienRepository.save(user);
            }
        }

        if (RoleEnum.USDE == roleEnum) {
            Optional<KhachHang> khachHang = khachHangRepo.findByEmail(email);
            if (khachHang.isEmpty()) {
                KhachHang user = new KhachHang();
                user.setTenKhachHang(email);
                user.setEmail(email);
                khachHangRepo.save(user);
            }
        }
    }
}