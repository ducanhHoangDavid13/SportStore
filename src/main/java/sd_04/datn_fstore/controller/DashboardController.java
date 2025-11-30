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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping()
    public String showDashboard(Model model) {
        // 1. Thống kê 4 thẻ Cards (Tổng quan)
        model.addAttribute("newCustomers", dashboardService.countAllKhachHangNew());
        model.addAttribute("totalOrdersToday", dashboardService.countOrdersToday());
        model.addAttribute("todayRevenue", dashboardService.todayRevenue());
        model.addAttribute("totalSanPhamSapHet", dashboardService.totalSanPhamSapHet());

        // 2. Dữ liệu biểu đồ mặc định (Tuần này)
        model.addAttribute("dayLabels", dashboardService.getDayLabelsLast7Days());
        model.addAttribute("revenueList", dashboardService.getRevenueLast7Days());

        // 3. Dữ liệu biểu đồ tròn (Trạng thái đơn hàng)
        // Lưu ý: Thứ tự labels phải khớp với thứ tự data trả về từ service
        model.addAttribute("orderStatusLabels", List.of("Hoàn thành", "Đang giao", "Đã hủy"));
        model.addAttribute("orderStatusValues", dashboardService.getOrderStatusSummary());

        // 4. [MỚI] Dữ liệu Bảng "Đơn hàng gần đây" & "Top sản phẩm"
        model.addAttribute("recentOrders", dashboardService.getRecentOrders());
        model.addAttribute("topProducts", dashboardService.getTopSellingProducts());

        return "view/admin/dashboard";
    }

    // API endpoint lấy dữ liệu biểu đồ doanh thu theo bộ lọc (AJAX)
    // ... imports

    @GetMapping("/revenue")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRevenueData(
            @RequestParam String filter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. Xử lý dữ liệu Biểu đồ đường (Line Chart) - GIỮ NGUYÊN CODE CŨ CỦA BẠN
            switch (filter) {
                case "weekly":
                    response.put("labels", dashboardService.getDayLabelsLast7Days());
                    response.put("revenues", dashboardService.getRevenueLast7Days());
                    // [MỚI] Thêm dữ liệu Pie Chart theo tuần
                    response.put("pieData", dashboardService.getOrderStatusSummaryByFilter("weekly", null, null));
                    break;

                case "monthly":
                    response.put("labels", dashboardService.getWeekLabelsInMonth());
                    response.put("revenues", dashboardService.getRevenueByWeeksInMonth());
                    // [MỚI] Thêm dữ liệu Pie Chart theo tháng
                    response.put("pieData", dashboardService.getOrderStatusSummaryByFilter("monthly", null, null));
                    break;

                // ... các case quarterly, yearly tương tự ...

                case "custom":
                    if (startDate != null && endDate != null) {
                        response.put("labels", dashboardService.getCustomDateLabels(startDate, endDate));
                        response.put("revenues", dashboardService.getRevenueByCustomDate(startDate, endDate));
                        // [MỚI] Thêm dữ liệu Pie Chart theo ngày tùy chỉnh
                        response.put("pieData", dashboardService.getOrderStatusSummaryByFilter("custom", startDate, endDate));
                    }
                    break;
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}