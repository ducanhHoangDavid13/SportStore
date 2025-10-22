package sd_04.datn_fstore.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.KhachHang;
import sd_04.datn_fstore.repo.KhachHangRepo;
import sd_04.datn_fstore.service.AuthenticationService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final KhachHangRepo khachHangRepo;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void postConstruct() {
        KhachHang user = new KhachHang();
        user.setEmail("user@gmail.com");
        user.setPassword(passwordEncoder.encode("user"));
        khachHangRepo.save(user);
    }


    @Override
    public void register(KhachHang account) {
        Optional<KhachHang> khachHang = khachHangRepo.findByEmail(account.getEmail());
        if (khachHang.isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        String encodedPassword = passwordEncoder.encode(account.getPassword());
        account.setPassword(encodedPassword);
        khachHangRepo.save(account);
    }
}
