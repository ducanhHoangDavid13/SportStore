package sd_04.datn_fstore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sd_04.datn_fstore.model.PhieuGiamGia;
import sd_04.datn_fstore.repo.PhieuGiamGiaRepo;
import sd_04.datn_fstore.service.PhieuGiamgiaService;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/phieu-giam-gia")
public class PhieuGiamGiaController {
    @GetMapping("/hien-thi")
    public String hienThi() {
        return "view/PhieuGiamGia";
    }
<<<<<<< HEAD
<<<<<<< HEAD
}
=======
}
>>>>>>> main
=======
}

>>>>>>> main
