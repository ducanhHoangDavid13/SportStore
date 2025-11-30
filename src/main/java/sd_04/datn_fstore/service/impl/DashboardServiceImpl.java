package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.dto.RecentOrderDTO;
import sd_04.datn_fstore.dto.TopProductDTO;
import sd_04.datn_fstore.repository.HoaDonRepository;
import sd_04.datn_fstore.repository.KhachHangRepo;
import sd_04.datn_fstore.repository.SanPhamCTRepository;
import sd_04.datn_fstore.service.DashboardService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final HoaDonRepository hoaDonRepository;
    private final KhachHangRepo khachHangRepository;
    private final SanPhamCTRepository sanPhamChiTietRepository;

    // --- CÁC HÀM THỐNG KÊ CƠ BẢN (CARDS) ---

    @Override
    public int countAllKhachHangNew() {
        // Đếm khách hàng tạo trong ngày hôm nay
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        return khachHangRepository.countByNgayTaoBetween(start, end);
    }

    @Override
    public int countOrdersToday() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        return hoaDonRepository.countByNgayTaoBetween(start, end);
    }

    @Override
    public BigDecimal todayRevenue() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        BigDecimal revenue = hoaDonRepository.sumTotalAmountByDateRange(start, end);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    @Override
    public int totalSanPhamSapHet() {
        // Giả sử số lượng < 10 là sắp hết
        return sanPhamChiTietRepository.countBySoLuongLessThanEqual(10);
    }

    // --- CÁC HÀM BIỂU ĐỒ (CHARTS) ---

    // 1. Biểu đồ Tuần (7 ngày gần nhất)
    @Override
    public List<String> getDayLabelsLast7Days() {
        List<String> labels = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = 6; i >= 0; i--) {
            labels.add(today.minusDays(i).format(formatter));
        }
        return labels;
    }

    @Override
    public List<Long> getRevenueLast7Days() {
        List<Long> revenues = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            BigDecimal total = hoaDonRepository.sumTotalAmountByDateRange(
                    date.atStartOfDay(), date.atTime(LocalTime.MAX));
            revenues.add(total != null ? total.longValue() : 0L);
        }
        return revenues;
    }

    // 2. Biểu đồ Tháng (Chia 4 tuần tượng trưng)
    @Override
    public List<String> getWeekLabelsInMonth() {
        return Arrays.asList("Tuần 1", "Tuần 2", "Tuần 3", "Tuần 4");
    }

    @Override
    public List<Long> getRevenueByWeeksInMonth() {
        List<Long> revenues = new ArrayList<>();
        LocalDate startMonth = LocalDate.now().withDayOfMonth(1);

        // Chia tháng thành 4 khoảng (mỗi khoảng 7 ngày, tuần cuối lấy hết phần còn lại)
        for (int i = 0; i < 4; i++) {
            LocalDateTime start = startMonth.plusDays(i * 7).atStartOfDay();
            LocalDateTime end = (i == 3)
                    ? startMonth.plusMonths(1).minusDays(1).atTime(LocalTime.MAX) // Cuối tháng
                    : start.plusDays(6).with(LocalTime.MAX); // Cuối tuần

            BigDecimal total = hoaDonRepository.sumTotalAmountByDateRange(start, end);
            revenues.add(total != null ? total.longValue() : 0L);
        }
        return revenues;
    }

    // 3. Biểu đồ Quý
    @Override
    public List<Long> getRevenueByQuarter() {
        List<Long> revenues = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();

        // 4 Quý: Q1(1-3), Q2(4-6), Q3(7-9), Q4(10-12)
        for (int quarter = 1; quarter <= 4; quarter++) {
            int startMonth = (quarter - 1) * 3 + 1;
            int endMonth = startMonth + 2;

            LocalDateTime start = LocalDateTime.of(currentYear, startMonth, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(currentYear, endMonth, 1, 23, 59, 59)
                    .withDayOfMonth(LocalDateTime.of(currentYear, endMonth, 1, 0, 0).toLocalDate().lengthOfMonth());

            BigDecimal total = hoaDonRepository.sumTotalAmountByDateRange(start, end);
            revenues.add(total != null ? total.longValue() : 0L);
        }
        return revenues;
    }

    // 4. Biểu đồ Năm
    @Override
    public List<Long> getRevenueByYear() {
        List<Long> revenues = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();

        for (int month = 1; month <= 12; month++) {
            LocalDateTime start = LocalDateTime.of(currentYear, month, 1, 0, 0);
            LocalDateTime end = start.withDayOfMonth(start.toLocalDate().lengthOfMonth()).with(LocalTime.MAX);

            BigDecimal total = hoaDonRepository.sumTotalAmountByDateRange(start, end);
            revenues.add(total != null ? total.longValue() : 0L);
        }
        return revenues;
    }

    // 5. Biểu đồ Tùy chỉnh
    @Override
    public List<String> getCustomDateLabels(LocalDate startDate, LocalDate endDate) {
        List<String> labels = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        // Lưu ý: Nếu khoảng cách ngày quá lớn, nên group theo tuần/tháng. Ở đây làm simple loop từng ngày.
        startDate.datesUntil(endDate.plusDays(1)).forEach(date -> {
            labels.add(date.format(formatter));
        });
        return labels;
    }

    @Override
    public List<Long> getRevenueByCustomDate(LocalDate startDate, LocalDate endDate) {
        List<Long> revenues = new ArrayList<>();
        startDate.datesUntil(endDate.plusDays(1)).forEach(date -> {
            BigDecimal total = hoaDonRepository.sumTotalAmountByDateRange(
                    date.atStartOfDay(), date.atTime(LocalTime.MAX));
            revenues.add(total != null ? total.longValue() : 0L);
        });
        return revenues;
    }

    // 6. Biểu đồ Tròn (Mặc định lấy toàn bộ hoặc theo tháng này)
    @Override
    public List<Integer> getOrderStatusSummary() {
        // Mặc định lấy tháng hiện tại
        return getOrderStatusSummaryByFilter("monthly", null, null);
    }

    // Hàm hỗ trợ lọc cho Pie Chart
    @Override
    public List<Integer> getOrderStatusSummaryByFilter(String filter, LocalDate startDate, LocalDate endDate) {
        LocalDateTime fromDate;
        LocalDateTime toDate = LocalDateTime.now();

        switch (filter) {
            case "weekly": fromDate = toDate.minusDays(7); break;
            case "monthly": fromDate = LocalDate.now().withDayOfMonth(1).atStartOfDay(); break;
            case "quarterly": fromDate = toDate.minusMonths(3); break;
            case "yearly": fromDate = LocalDate.now().withDayOfYear(1).atStartOfDay(); break;
            case "custom":
                fromDate = (startDate != null) ? startDate.atStartOfDay() : toDate.minusDays(7);
                if (endDate != null) toDate = endDate.atTime(LocalTime.MAX);
                break;
            default: fromDate = toDate.minusDays(7);
        }

        // Query DB: 1=Hoàn thành, 3=Đang giao, 0=Hủy (Bạn sửa lại ID trạng thái theo DB thật của bạn)
        Integer hoanThanh = hoaDonRepository.countByStatusAndDateRange(1, fromDate, toDate);
        Integer dangGiao = hoaDonRepository.countByStatusAndDateRange(3, fromDate, toDate);
        Integer daHuy = hoaDonRepository.countByStatusAndDateRange(0, fromDate, toDate);

        return Arrays.asList(
                hoanThanh != null ? hoanThanh : 0,
                dangGiao != null ? dangGiao : 0,
                daHuy != null ? daHuy : 0
        );
    }

    // --- CÁC HÀM DANH SÁCH (TABLES) ---

    @Override
    public List<RecentOrderDTO> getRecentOrders() {
        // Lấy 5 đơn mới nhất
        return hoaDonRepository.findRecentOrders(PageRequest.of(0, 5));
    }

    @Override
    public List<TopProductDTO> getTopSellingProducts() {
        // Lấy 5 sản phẩm bán chạy nhất
        return sanPhamChiTietRepository.findTopSellingProducts(PageRequest.of(0, 5));
    }

    // --- CÁC HÀM CŨ (GIỮ LẠI TRÁNH LỖI) ---
    @Override public List<String> getHourLabels() { return new ArrayList<>(); }
    @Override public List<Long> getRevenueByHours() { return new ArrayList<>(); }
}