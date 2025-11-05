package sd_04.datn_fstore.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.enums.RoleEnum;
import sd_04.datn_fstore.model.Account;
import sd_04.datn_fstore.model.KhachHang;
import sd_04.datn_fstore.repository.KhachHangRepo;
import sd_04.datn_fstore.repository.AccountRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl {

    private final KhachHangRepo khachHangRepo;
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

        Optional<KhachHang> khachHang = khachHangRepo.findByEmail(email);
        if (khachHang.isEmpty()) {
            KhachHang user = new KhachHang();
            user.setEmail(email);
            khachHangRepo.save(user);
        }
    }
}
