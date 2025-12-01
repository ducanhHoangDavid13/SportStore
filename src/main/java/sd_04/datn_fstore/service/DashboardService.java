package sd_04.datn_fstore.service;

import sd_04.datn_fstore.dto.RecentOrderDTO;
import sd_04.datn_fstore.dto.TopProductDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface DashboardService {

    // Cards
    int countAllKhachHangNew();
    int countOrdersToday();
    BigDecimal todayRevenue();
    int totalSanPhamSapHet();

    // Charts
    List<String> getDayLabelsLast7Days();
    List<Long> getRevenueLast7Days();

    List<String> getWeekLabelsInMonth();
    List<Long> getRevenueByWeeksInMonth();

    // Mới: Hàm cho Quý/Năm
    List<String> getQuarterLabels();
    List<Long> getRevenueByQuarter();

    List<String> getYearLabels();
    List<Long> getRevenueByYear();

    List<String> getCustomDateLabels(LocalDate startDate, LocalDate endDate);
    List<Long> getRevenueByCustomDate(LocalDate startDate, LocalDate endDate);

    // Pie Chart
    List<Integer> getOrderStatusSummaryByFilter(String filter, LocalDate startDate, LocalDate endDate);

    // Tables
    List<RecentOrderDTO> getRecentOrders();
    List<TopProductDTO> getTopSellingProducts();

    List<Integer> getOrderStatusSummary();

}