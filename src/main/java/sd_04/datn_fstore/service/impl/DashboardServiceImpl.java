package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.repository.AccountRepository;
import sd_04.datn_fstore.repository.HoaDonRepository;
import sd_04.datn_fstore.service.DashboardService;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final AccountRepository accountRepository;
    private final HoaDonRepository hoaDonRepository;

    @Override
    public long totalUsers() {
        return accountRepository.count();
    }

    @Override
    public long newCustomers() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(23, 59, 59);
        return accountRepository.findByCreatedAtBetween(start, end).size();
    }

    @Override
    public long totalOrders() {
        return hoaDonRepository.count();
    }

    @Override
    public long todayRevenue() {
        return 0;
    }
}
