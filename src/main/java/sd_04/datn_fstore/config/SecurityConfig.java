package sd_04.datn_fstore.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // FIX 1: TẮT CSRF để cho phép tất cả các lệnh POST/PUT từ JavaScript
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(authorize -> authorize

                        // FIX 2: MỞ QUYỀN TRUY CẬP CHO CSS/JS (Lỗi MIME TYPE)
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/vendor/**",
                                "/uploads/**",
                                "/admin/css/**", // THÊM: Cho phép CSS/JS ngay cả khi có prefix /admin/
                                "/admin/js/**"
                        ).permitAll()

                        .requestMatchers("/login", "/register").permitAll()

                        // FIX 3: Cho phép người dùng đã đăng nhập truy cập API và trang admin
                        .requestMatchers("/api/**", "/admin/**").authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/admin/ban-hang", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(e -> e
                        // (Các exception handlers giữ nguyên)
                        .authenticationEntryPoint((req, res, ex) -> {
                            if (req.getRequestURI().startsWith("/api/")) {
                                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                res.setContentType("application/json;charset=UTF-8");
                                res.getWriter().write("{\"error\":\"Unauthorized: Vui lòng đăng nhập.\"}") ;
                            } else {
                                res.sendRedirect("/login");
                            }
                        })
                        .accessDeniedHandler((req, res, ex) -> {
                            if (req.getRequestURI().startsWith("/api/")) {
                                res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                res.setContentType("application/json;charset=UTF-8");
                                res.getWriter().write("{\"error\":\"Access Denied: Không đủ quyền truy cập.\"}") ;
                            } else {
                                res.sendRedirect("/access-denied");
                            }
                        })
                );

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }
}