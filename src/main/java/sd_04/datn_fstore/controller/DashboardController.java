package sd_04.datn_fstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import sd_04.datn_fstore.service.DashboardService;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor

public class DashboardController {

    private final DashboardService dashboardService;

    // ============================================================
    // 1. TRẢ VỀ TRANG HTML (View)
    // ============================================================
    @GetMapping("/dashboard")
    public String viewDashboard(Model model) {
        // A. Thống kê 4 Cards
        model.addAttribute("todayRevenue", dashboardService.todayRevenue());
        model.addAttribute("totalOrdersToday", dashboardService.countOrdersToday());
        model.addAttribute("newCustomers", dashboardService.countAllKhachHangNew());
        model.addAttribute("totalSanPhamSapHet", dashboardService.totalSanPhamSapHet());

        // B. Biểu đồ Cột (Mặc định: 7 ngày gần nhất)
        model.addAttribute("dayLabels", dashboardService.getDayLabelsLast7Days());
        model.addAttribute("revenueList", dashboardService.getRevenueLast7Days());

        // C. Biểu đồ Tròn (Mặc định: Tổng quan từ trước đến nay hoặc theo logic mặc định của bạn)
        model.addAttribute("orderStatusValues", dashboardService.getOrderStatusSummary());

        // D. Bảng dữ liệu
        model.addAttribute("recentOrders", dashboardService.getRecentOrders());
        model.addAttribute("topProducts", dashboardService.getTopSellingProducts());

        return "/view/admin/dashboard";
    }

    // ============================================================
    // 2. API TRẢ VỀ JSON (AJAX Update Chart)
    // ============================================================
    @GetMapping("/dashboard/revenue")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRevenueData(
            @RequestParam(defaultValue = "weekly") String filter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Map<String, Object> response = new HashMap<>();

        // Khởi tạo ngày để tính toán cho Pie Chart đồng bộ
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now();
        LocalDate today = LocalDate.now();

        try {
            // --- XỬ LÝ LOGIC THEO TỪNG LOẠI FILTER ---
            switch (filter) {
                case "weekly":
                    // 1. Lấy data cho Bar Chart (Dùng hàm riêng của Service)
                    response.put("labels", dashboardService.getDayLabelsLast7Days());
                    response.put("revenues", dashboardService.getRevenueLast7Days());

                    // 2. Tính ngày cho Pie Chart (Phải khớp với logic Last 7 Days)
                    end = today;
                    start = today.minusDays(6);
                    break;

                case "monthly":
                    // 1. Bar Chart: Theo tuần trong tháng
                    response.put("labels", dashboardService.getWeekLabelsInMonth());
                    response.put("revenues", dashboardService.getRevenueByWeeksInMonth());

                    // 2. Pie Chart: Tính từ đầu tháng đến cuối tháng này
                    start = today.with(TemporalAdjusters.firstDayOfMonth());
                    end = today.with(TemporalAdjusters.lastDayOfMonth());
                    break;

                case "quarterly":
                    // 1. Bar Chart: Theo quý
                    response.put("labels", dashboardService.getQuarterLabels());
                    response.put("revenues", dashboardService.getRevenueByQuarter());

                    // 2. Pie Chart: Tính ngày bắt đầu và kết thúc của Quý hiện tại
                    int currentQuarter = (today.getMonthValue() - 1) / 3 + 1;
                    int firstMonthOfQuarter = (currentQuarter - 1) * 3 + 1;
                    start = LocalDate.of(today.getYear(), firstMonthOfQuarter, 1);
                    end = start.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth());
                    break;

                case "yearly":
                    // 1. Bar Chart: Theo 12 tháng
                    response.put("labels", dashboardService.getYearLabels());
                    response.put("revenues", dashboardService.getRevenueByYear());

                    // 2. Pie Chart: Từ đầu năm đến cuối năm
                    start = LocalDate.of(today.getYear(), 1, 1);
                    end = LocalDate.of(today.getYear(), 12, 31);
                    break;

                case "custom":
                    // 1. Bar Chart: Theo ngày tùy chọn
                    if (startDate != null && endDate != null) {
                        response.put("labels", dashboardService.getCustomDateLabels(startDate, endDate));
                        response.put("revenues", dashboardService.getRevenueByCustomDate(startDate, endDate));
                        start = startDate;
                        end = endDate;
                    }
                    break;
            }

            // --- LẤY DỮ LIỆU PIE CHART ---
            // Gọi hàm getOrderStatusSummaryByFilter với khoảng ngày (start, end) đã tính ở trên
            // Điều này đảm bảo Pie Chart luôn hiển thị đúng dữ liệu của khoảng thời gian đang xem
            response.put("pieData", dashboardService.getOrderStatusSummaryByFilter(filter, start, end));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); // Nên dùng Logger trong thực tế
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}