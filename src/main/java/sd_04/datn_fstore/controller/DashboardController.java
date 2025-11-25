package sd_04.datn_fstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/dashboard")
public class DashboardController {

    @GetMapping()
    public String showDashboard(Model model) {
        model.addAttribute("totalUsers", 132);
        model.addAttribute("totalOrders", 36);
        model.addAttribute("todayRevenue", "2,800,000");
        model.addAttribute("newCustomers", 12);

        model.addAttribute("dayLabels", List.of("T2", "T3", "T4", "T5", "T6", "T7", "CN"));
        model.addAttribute("revenueList", List.of(3200000, 4100000, 3800000, 4200000, 5000000, 4700000, 5200000));

        model.addAttribute("orderStatusLabels", List.of("Đang xử lý", "Hoàn tất", "Đã hủy"));
        model.addAttribute("orderStatusValues", List.of(12, 30, 5));

        return "view/admin/dashboard";
    }
}
