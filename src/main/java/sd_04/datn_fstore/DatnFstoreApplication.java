package sd_04.datn_fstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DatnFstoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatnFstoreApplication.class, args);
        System.out.println("✅ Ứng dụng đang chạy tại: http://localhost:8080/nhanvien/view");
    }

}
