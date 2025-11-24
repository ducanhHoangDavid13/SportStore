package sd_04.datn_fstore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Lấy đường dẫn tuyệt đối đến thư mục làm việc hiện tại của ứng dụng (Thư mục gốc của dự án)
        String rootPath = System.getProperty("user.dir");

        // Tạo đường dẫn vật lý đầy đủ tới thư mục 'uploads'
        // Sử dụng File.separator để đảm bảo tương thích với cả Windows (\) và Linux/Mac (/)
        String uploadPath = rootPath + File.separator + "uploads" + File.separator;

        // In ra console để kiểm tra TÍNH CHÍNH XÁC của đường dẫn vật lý
        System.out.println("Cấu hình ảnh: Ánh xạ /image/** tới đường dẫn vật lý: " + uploadPath);

        // Chuyển đường dẫn vật lý thành định dạng URL Resource Location
        // Đảm bảo đường dẫn bắt đầu bằng 'file:///'
        String location = "file:///" + uploadPath.replace('\\', '/');

        registry.addResourceHandler("/image/**")
                .addResourceLocations(location);
    }
}