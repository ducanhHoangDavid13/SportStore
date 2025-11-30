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

import java.time.DayOfWeek;
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
    // 1. CONTROLLER TRẢ VỀ GIAO DIỆN HTML (View)
    // URL: /admin/dashboard
    // ============================================================
    @GetMapping("/dashboard")
    public String viewDashboard(Model model) {
        // A. Thống kê 4 Cards trên cùng
        model.addAttribute("todayRevenue", dashboardService.todayRevenue());
        model.addAttribute("totalOrdersToday", dashboardService.countOrdersToday());
        model.addAttribute("newCustomers", dashboardService.countAllKhachHangNew());
        model.addAttribute("totalSanPhamSapHet", dashboardService.totalSanPhamSapHet());

        // B. Dữ liệu ban đầu cho Biểu đồ Cột (Mặc định là Tuần này)
        model.addAttribute("dayLabels", dashboardService.getDayLabelsLast7Days());
        model.addAttribute("revenueList", dashboardService.getRevenueLast7Days());

        // C. Dữ liệu ban đầu cho Biểu đồ Tròn (Trạng thái đơn hàng)
        model.addAttribute("orderStatusValues", dashboardService.getOrderStatusSummary());

        // D. Danh sách Đơn hàng gần đây
        model.addAttribute("recentOrders", dashboardService.getRecentOrders());

        // E. Top sản phẩm bán chạy
        model.addAttribute("topProducts", dashboardService.getTopSellingProducts());

        // Trả về file HTML: templates/admin/dashboard.html
        return "/view/admin/dashboard";
    }

    // ============================================================
    // 2. API TRẢ VỀ DỮ LIỆU JSON (AJAX cho Biểu đồ)
    // URL: /admin/dashboard/revenue?filter=...
    // ============================================================
    @GetMapping("/dashboard/revenue")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRevenueData(
            @RequestParam(defaultValue = "weekly") String filter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Map<String, Object> response = new HashMap<>();
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now();
        LocalDate today = LocalDate.now();

        try {
            // --- XỬ LÝ BIỂU ĐỒ CỘT (DOANH THU) ---
            switch (filter) {
                case "weekly":
                    response.put("labels", dashboardService.getDayLabelsLast7Days());
                    response.put("revenues", dashboardService.getRevenueLast7Days());

                    // Tính ngày cho Pie Chart
                    start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    end = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                    break;

                case "monthly":
                    response.put("labels", dashboardService.getWeekLabelsInMonth());
                    response.put("revenues", dashboardService.getRevenueByWeeksInMonth());

                    start = today.with(TemporalAdjusters.firstDayOfMonth());
                    end = today.with(TemporalAdjusters.lastDayOfMonth());
                    break;

                case "quarterly":
                    response.put("labels", dashboardService.getQuarterLabels());
                    response.put("revenues", dashboardService.getRevenueByQuarter());

                    int currentQuarter = (today.getMonthValue() - 1) / 3 + 1;
                    int firstMonth = (currentQuarter - 1) * 3 + 1;
                    start = LocalDate.of(today.getYear(), firstMonth, 1);
                    end = start.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth());
                    break;

                case "yearly":
                    response.put("labels", dashboardService.getYearLabels());
                    response.put("revenues", dashboardService.getRevenueByYear());

                    start = LocalDate.of(today.getYear(), 1, 1);
                    end = LocalDate.of(today.getYear(), 12, 31);
                    break;

                case "custom":
                    if (startDate != null && endDate != null) {
                        response.put("labels", dashboardService.getCustomDateLabels(startDate, endDate));
                        response.put("revenues", dashboardService.getRevenueByCustomDate(startDate, endDate));
                        start = startDate;
                        end = endDate;
                    }
                    break;
            }

            // --- XỬ LÝ BIỂU ĐỒ TRÒN (TRẠNG THÁI) ---
            // Dùng khoảng thời gian start/end đã tính ở trên để lọc trạng thái
            response.put("pieData", dashboardService.getOrderStatusSummaryByFilter(filter, start, end));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}