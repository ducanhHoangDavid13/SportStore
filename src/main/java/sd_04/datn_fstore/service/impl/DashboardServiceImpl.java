package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.dto.RecentOrderDTO;
import sd_04.datn_fstore.dto.TopProductDTO;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.repository.HoaDonRepository;
import sd_04.datn_fstore.repository.KhachHangRepo; // Kiểm tra tên Repo của bạn
import sd_04.datn_fstore.repository.SanPhamCTRepository;
import sd_04.datn_fstore.service.DashboardService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final HoaDonRepository hoaDonRepository;
    private final KhachHangRepo khachHangRepository;
    private final SanPhamCTRepository sanPhamCTRepository;

    // 1. CARDS
    @Override
    public int countAllKhachHangNew() {
        return khachHangRepository.countByNgayTaoBetween(
                LocalDate.now().atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX));
    }

    @Override
    public int countOrdersToday() {
        return hoaDonRepository.countByNgayTaoBetween(
                LocalDate.now().atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX));
    }

    @Override
    public BigDecimal todayRevenue() {
        BigDecimal revenue = hoaDonRepository.sumTotalAmountByDateAndStatus(
                LocalDate.now().atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX), 4);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    @Override
    public int totalSanPhamSapHet() {
        return sanPhamCTRepository.countBySoLuongLessThanEqual(10);
    }

    // 2. CHARTS - WEEKLY
    @Override
    public List<String> getDayLabelsLast7Days() {
        List<String> labels = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = 6; i >= 0; i--) labels.add(today.minusDays(i).format(fmt));
        return labels;
    }

    @Override
    public List<Long> getRevenueLast7Days() {
        List<Long> data = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            BigDecimal total = hoaDonRepository.sumTotalAmountByDateAndStatus(
                    date.atStartOfDay(), date.atTime(LocalTime.MAX), 4);
            data.add(total != null ? total.longValue() : 0L);
        }
        return data;
    }

    // 3. CHARTS - MONTHLY
    @Override
    public List<String> getWeekLabelsInMonth() {
        return List.of("Tuần 1", "Tuần 2", "Tuần 3", "Tuần 4", "Còn lại");
    }

    @Override
    public List<Long> getRevenueByWeeksInMonth() {
        List<Long> data = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int lengthOfMonth = today.lengthOfMonth();
        for (int i = 0; i < 5; i++) {
            int startDay = i * 7 + 1;
            if (startDay > lengthOfMonth) break;
            int endDay = Math.min((i + 1) * 7, lengthOfMonth);
            LocalDate s = LocalDate.of(today.getYear(), today.getMonth(), startDay);
            LocalDate e = LocalDate.of(today.getYear(), today.getMonth(), endDay);
            BigDecimal total = hoaDonRepository.sumTotalAmountByDateAndStatus(s.atStartOfDay(), e.atTime(LocalTime.MAX), 4);
            data.add(total != null ? total.longValue() : 0L);
        }
        while (data.size() < 5) data.add(0L);
        return data;
    }

    // 4. CHARTS - QUARTERLY (Gom nhóm theo 3 tháng)
    @Override
    public List<String> getQuarterLabels() {
        int currentQuarter = (LocalDate.now().getMonthValue() - 1) / 3 + 1;
        int startMonth = (currentQuarter - 1) * 3 + 1;
        return List.of("Tháng " + startMonth, "Tháng " + (startMonth + 1), "Tháng " + (startMonth + 2));
    }

    @Override
    public List<Long> getRevenueByQuarter() {
        List<Long> data = new ArrayList<>();
        int year = LocalDate.now().getYear();
        int currentQuarter = (LocalDate.now().getMonthValue() - 1) / 3 + 1;
        int startMonth = (currentQuarter - 1) * 3 + 1;

        for (int i = 0; i < 3; i++) {
            int month = startMonth + i;
            LocalDate s = LocalDate.of(year, month, 1);
            LocalDate e = s.with(TemporalAdjusters.lastDayOfMonth());
            BigDecimal total = hoaDonRepository.sumTotalAmountByDateAndStatus(s.atStartOfDay(), e.atTime(LocalTime.MAX), 4);
            data.add(total != null ? total.longValue() : 0L);
        }
        return data;
    }

    // 5. CHARTS - YEARLY (Gom nhóm theo 12 tháng)
    @Override
    public List<String> getYearLabels() {
        List<String> labels = new ArrayList<>();
        for (int i = 1; i <= 12; i++) labels.add("Tháng " + i);
        return labels;
    }

    @Override
    public List<Long> getRevenueByYear() {
        List<Long> data = new ArrayList<>();
        int year = LocalDate.now().getYear();
        for (int i = 1; i <= 12; i++) {
            LocalDate s = LocalDate.of(year, i, 1);
            LocalDate e = s.with(TemporalAdjusters.lastDayOfMonth());
            BigDecimal total = hoaDonRepository.sumTotalAmountByDateAndStatus(s.atStartOfDay(), e.atTime(LocalTime.MAX), 4);
            data.add(total != null ? total.longValue() : 0L);
        }
        return data;
    }

    // 6. CUSTOM DATE
    @Override
    public List<String> getCustomDateLabels(LocalDate s, LocalDate e) {
        List<String> labels = new ArrayList<>();
        if (s.until(e).getDays() <= 31) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
            s.datesUntil(e.plusDays(1)).forEach(d -> labels.add(d.format(fmt)));
        } else {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/yyyy");
            LocalDate curr = s.withDayOfMonth(1);
            while (!curr.isAfter(e)) { labels.add(curr.format(fmt)); curr = curr.plusMonths(1); }
        }
        return labels;
    }

    @Override
    public List<Long> getRevenueByCustomDate(LocalDate s, LocalDate e) {
        List<Long> data = new ArrayList<>();
        if (s.until(e).getDays() <= 31) {
            s.datesUntil(e.plusDays(1)).forEach(d -> {
                BigDecimal t = hoaDonRepository.sumTotalAmountByDateAndStatus(d.atStartOfDay(), d.atTime(LocalTime.MAX), 4);
                data.add(t != null ? t.longValue() : 0L);
            });
        } else {
            LocalDate curr = s.withDayOfMonth(1);
            while (!curr.isAfter(e)) {
                LocalDateTime start = (curr.isBefore(s) ? s : curr).atStartOfDay();
                LocalDateTime end = (curr.with(TemporalAdjusters.lastDayOfMonth()).isAfter(e) ? e : curr.with(TemporalAdjusters.lastDayOfMonth())).atTime(LocalTime.MAX);
                BigDecimal t = hoaDonRepository.sumTotalAmountByDateAndStatus(start, end, 4);
                data.add(t != null ? t.longValue() : 0L);
                curr = curr.plusMonths(1);
            }
        }
        return data;
    }

    // 7. PIE CHART
    @Override
    public List<Integer> getOrderStatusSummaryByFilter(String filter, LocalDate startDate, LocalDate endDate) {
        // (Logic tính ngày đã được Controller xử lý và truyền vào startDate/endDate)
        // Nếu startDate null (khi load lần đầu), fallback về tháng hiện tại
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        LocalDateTime end = (endDate != null) ? endDate.atTime(LocalTime.MAX) : LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);

        List<Object[]> results = hoaDonRepository.countOrdersByStatusBetween(start, end);
        List<Integer> counts = new ArrayList<>(Collections.nCopies(7, 0));
        if (results != null && !results.isEmpty()) {
            Object[] row = results.get(0);
            for (int i = 0; i < 7; i++) if (row[i] != null) counts.set(i, ((Number) row[i]).intValue());
        }
        return counts;
    }

    // 8. LIST DATA
    @Override
    public List<RecentOrderDTO> getRecentOrders() {
        List<HoaDon> list = hoaDonRepository.findTop5ByOrderByNgayTaoDesc();
        List<RecentOrderDTO> dtos = new ArrayList<>();
        for (HoaDon hd : list) {
            String name = (hd.getKhachHang() != null) ? hd.getKhachHang().getTenKhachHang() : "Khách lẻ";
            BigDecimal total = (hd.getTongTienSauGiam() != null) ? hd.getTongTienSauGiam() : hd.getTongTien();
            dtos.add(new RecentOrderDTO(hd.getMaHoaDon(), name, hd.getNgayTao(), total, hd.getTrangThai()));
        }
        return dtos;
    }

    @Override
    public List<TopProductDTO> getTopSellingProducts() {
        return sanPhamCTRepository.findTopSellingProducts(PageRequest.of(0, 5));
    }
    @Override
    public List<Integer> getOrderStatusSummary() {
        // Mặc định lấy dữ liệu của THÁNG NAY
        LocalDate today = LocalDate.now();
        return getOrderStatusSummaryByFilter("monthly",
                today.with(TemporalAdjusters.firstDayOfMonth()),
                today.with(TemporalAdjusters.lastDayOfMonth()));
    }
}