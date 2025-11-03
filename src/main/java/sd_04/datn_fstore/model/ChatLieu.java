package sd_04.datn_fstore.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ChatLieu")
public class ChatLieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "loaiChatLieu", length = 500)
    private String loaiChatLieu;

    @Column(name = "moTa")
    private String moTa;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ngayTao")
    private Date ngayTao;

    @Column(name = "trangThai")
    private Integer trangThai;

    @JsonIgnore
    @OneToMany(mappedBy = "chatLieu", fetch = FetchType.LAZY)
    private List<SanPhamChiTiet> sanPhamChiTiets;
}
