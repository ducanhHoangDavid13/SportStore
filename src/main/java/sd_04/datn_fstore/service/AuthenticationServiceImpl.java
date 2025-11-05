package sd_04.datn_fstore.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.enums.RoleEnum;
import sd_04.datn_fstore.model.Account;
import sd_04.datn_fstore.model.KhachHang;
<<<<<<< HEAD
import sd_04.datn_fstore.repository.KhachHangRepo;
=======
import sd_04.datn_fstore.model.NhanVien;
import sd_04.datn_fstore.repo.KhachHangRepo;
>>>>>>> 3fef446b75181f27f81d2fe160195ccdd4d3e6ce
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

        if(RoleEnum.ADMIN == roleEnum){
            Optional<NhanVien> nhanVien = nhanVienRepository.findByEmail(email);
            if (nhanVien.isEmpty()) {
                NhanVien user = new NhanVien();
                user.setTenNhanVien(email);
                user.setEmail(email);
                nhanVienRepository.save(user);
            }
        }

        if(RoleEnum.USDE == roleEnum){
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