package sd_04.datn_fstore.service;

import sd_04.datn_fstore.dto.RecentOrderDTO;
import sd_04.datn_fstore.dto.TopProductDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface DashboardService {

    // --- CÁC HÀM THỐNG KÊ CƠ BẢN (CARDS) ---
    int countAllKhachHangNew();
    int countOrdersToday();
    BigDecimal todayRevenue();
    int totalSanPhamSapHet();

    // --- CÁC HÀM BIỂU ĐỒ (CHARTS) ---

    // 1. Biểu đồ Tuần (7 ngày gần nhất)
    List<String> getDayLabelsLast7Days();
    List<Long> getRevenueLast7Days();

    // 2. Biểu đồ Tháng (Chia 4 tuần)
    List<String> getWeekLabelsInMonth();
    List<Long> getRevenueByWeeksInMonth();

    // 3. [MỚI] Biểu đồ Quý (4 Quý trong năm)
    List<Long> getRevenueByQuarter();

    // 4. [MỚI] Biểu đồ Năm (12 Tháng)
    List<Long> getRevenueByYear();

    // 5. [MỚI] Biểu đồ tùy chỉnh ngày (Từ ngày - Đến ngày)
    List<String> getCustomDateLabels(LocalDate startDate, LocalDate endDate);
    List<Long> getRevenueByCustomDate(LocalDate startDate, LocalDate endDate);

    // 6. Biểu đồ tròn (Trạng thái đơn hàng)
    List<Integer> getOrderStatusSummary();

    // --- CÁC HÀM DANH SÁCH (TABLES) ---

    // 7. [MỚI] Top 5 đơn hàng mới nhất
    List<RecentOrderDTO> getRecentOrders();

    // 8. [MỚI] Top 5 sản phẩm bán chạy
    List<TopProductDTO> getTopSellingProducts();

    // (Các hàm cũ không dùng nữa nhưng giữ lại để tránh lỗi override nếu cần)
    List<String> getHourLabels();
    List<Long> getRevenueByHours();
    List<Integer> getOrderStatusSummaryByFilter(String filter, LocalDate startDate, LocalDate endDate);

}