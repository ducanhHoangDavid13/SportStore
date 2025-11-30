package sd_04.datn_fstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import sd_04.datn_fstore.service.DashboardService;

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
        model.addAttribute("newCustomers", dashboardService.countAllKhachHangNew());
        model.addAttribute("totalOrdersToday", dashboardService.countOrdersToday());
        model.addAttribute("todayRevenue", dashboardService.todayRevenue());
        model.addAttribute("totalSanPhamSapHet", dashboardService.totalSanPhamSapHet());
        model.addAttribute("dayLabels", dashboardService.getDayLabelsLast7Days());
        model.addAttribute("revenueList", dashboardService.getRevenueLast7Days());
        model.addAttribute("orderStatusLabels", List.of("Đang xử lý", "Hoàn tất", "Đã hủy"));
        model.addAttribute("orderStatusValues", dashboardService.getOrderStatusSummary());
        return "view/admin/dashboard";
    }

    // API endpoint mới để lấy dữ liệu doanh thu theo filter
    @GetMapping("/revenue")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRevenueData(@RequestParam String filter) {
        Map<String, Object> response = new HashMap<>();

        try {
            switch (filter) {
                case "daily":
                    // Doanh thu theo giờ trong ngày hôm nay (24 giờ)
                    response.put("labels", dashboardService.getHourLabels());
                    response.put("revenues", dashboardService.getRevenueByHours());
                    break;

                case "weekly":
                    // Doanh thu 7 ngày gần nhất
                    response.put("labels", dashboardService.getDayLabelsLast7Days());
                    response.put("revenues", dashboardService.getRevenueLast7Days());
                    break;

                case "monthly":
                    // Doanh thu theo tuần trong tháng này
                    response.put("labels", dashboardService.getWeekLabelsInMonth());
                    response.put("revenues", dashboardService.getRevenueByWeeksInMonth());
                    break;

                default:
                    response.put("labels", dashboardService.getDayLabelsLast7Days());
                    response.put("revenues", dashboardService.getRevenueLast7Days());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Không thể tải dữ liệu doanh thu"));
        }
    }
}
