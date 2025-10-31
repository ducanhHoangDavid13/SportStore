package sd_04.datn_fstore.service;

import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.Account;
import sd_04.datn_fstore.repository.AccountRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Resource
    private AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account byLogin = accountRepository.findByEmail(username).orElseThrow(() ->
                new UsernameNotFoundException("User not found with username: " + username)
        );

        return User.builder()
                .username(byLogin.getEmail())
                .password(byLogin.getPassword())
                .roles(byLogin.getRole().name())
                .build();
    }
}
