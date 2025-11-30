package sd_04.datn_fstore.service;

import java.math.BigDecimal;
import java.util.List;

public interface DashboardService {

    int countAllKhachHangNew();

    int countOrdersToday();

    BigDecimal todayRevenue();

    int totalSanPhamSapHet();

    List<String> getDayLabelsLast7Days();

    List<Long> getRevenueLast7Days();

    List<String> getHourLabels();

    List<Long> getRevenueByHours();

    List<String> getWeekLabelsInMonth();

    List<Long> getRevenueByWeeksInMonth();

    List<Integer> getOrderStatusSummary();
}
