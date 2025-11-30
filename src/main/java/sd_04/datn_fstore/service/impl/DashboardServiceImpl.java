package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.repository.HoaDonRepository;
import sd_04.datn_fstore.repository.KhachHangRepo;
import sd_04.datn_fstore.repository.SanPhamRepository;
import sd_04.datn_fstore.service.DashboardService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final HoaDonRepository hoaDonRepo;
    private final SanPhamRepository sanPhamRepo;
    private final KhachHangRepo khachHangRepo;


    @Override
    public int countAllKhachHangNew() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        return khachHangRepo.countByNgayTaoBetween(startOfDay, endOfDay);
    }

    @Override
    public int countOrdersToday() {
        return 0;
    }

    @Override
    public BigDecimal todayRevenue() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        return hoaDonRepo.sumTongTienByNgay(startOfDay, endOfDay);
    }


    @Override
    public int totalSanPhamSapHet() {
        return sanPhamRepo.countBySoLuongLessThan(5);
    }

    @Override
    public List<String> getDayLabelsLast7Days() {
        List<String> labels = new ArrayList<>();
        String[] dayNames = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            int dayOfWeek = date.getDayOfWeek().getValue() % 7;
            labels.add(dayNames[dayOfWeek]);
        }
        return labels;
    }


    @Override
    public List<Long> getRevenueLast7Days() {
        List<Long> revenues = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDateTime startDate = LocalDate.now().minusDays(i).atStartOfDay();
            LocalDateTime endDate = startDate.plusDays(1);

            Long revenue = hoaDonRepo.sumRevenueByDateRange(startDate, endDate);
            revenues.add(revenue != null ? revenue : 0L);
        }
        return revenues;
    }

    @Override
    public List<String> getHourLabels() {
        return List.of();
    }

    @Override
    public List<Long> getRevenueByHours() {
        return List.of();
    }

    @Override
    public List<String> getWeekLabelsInMonth() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        List<String> labels = new ArrayList<>();
        int weekNumber = 1;
        LocalDate currentWeekStart = firstDayOfMonth;

        while (currentWeekStart.isBefore(lastDayOfMonth) || currentWeekStart.isEqual(lastDayOfMonth)) {
            labels.add("Tuần " + weekNumber);
            currentWeekStart = currentWeekStart.plusWeeks(1);
            weekNumber++;
        }

        return labels;
    }

    @Override
    public List<Long> getRevenueByWeeksInMonth() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        List<Long> revenues = new ArrayList<>();
        LocalDate currentWeekStart = firstDayOfMonth;

        while (currentWeekStart.isBefore(lastDayOfMonth) || currentWeekStart.isEqual(lastDayOfMonth)) {
            LocalDate weekEnd = currentWeekStart.plusWeeks(1);
            if (weekEnd.isAfter(lastDayOfMonth)) {
                weekEnd = lastDayOfMonth.plusDays(1);
            }

            LocalDateTime startDateTime = currentWeekStart.atStartOfDay();
            LocalDateTime endDateTime = weekEnd.atStartOfDay();

            Long revenue = hoaDonRepo.sumRevenueByDateRange(startDateTime, endDateTime);
            revenues.add(revenue != null ? revenue : 0L);

            currentWeekStart = weekEnd;
        }

        return revenues;
    }

    @Override
    public List<Integer> getOrderStatusSummary() {
        int processing = hoaDonRepo.countByTrangThai(0);
        int completed = hoaDonRepo.countByTrangThai(1);
        int cancelled = hoaDonRepo.countByTrangThai(2);
        return List.of(processing, completed, cancelled);
    }
}

