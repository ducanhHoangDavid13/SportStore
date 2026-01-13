// Cấu hình Toastr (Thời gian hiển thị và nút đóng)
toastr.options = {
    "closeButton": true,        // Hiện nút X để đóng
    "debug": false,
    "newestOnTop": true,
    "progressBar": true,        // Hiện thanh thời gian chạy
    "positionClass": "toast-top-right", // Vị trí
    "preventDuplicates": true,  // Chặn thông báo trùng lặp liên tục
    "onclick": null,
    "showDuration": "300",
    "hideDuration": "1000",
    "timeOut": "5000",          // 5 giây tự tắt (Sửa số này nếu muốn lâu hơn)
    "extendedTimeOut": "1000",  // Khi rê chuột vào thì chờ thêm 1s mới tắt
    "showEasing": "swing",
    "hideEasing": "linear",
    "showMethod": "fadeIn",
    "hideMethod": "fadeOut"
};

// Biến lưu ID của thông báo mới nhất đã từng hiển thị
let lastSeenId = 0;
let isFirstLoad = true; // Cờ kiểm tra lần load đầu tiên

function fetchNotifications() {
    $.ajax({
        url: '/api/notifications/unread',
        method: 'GET',
        success: function(data) {
            const badge = $('#notif-badge');
            const list = $('#notif-list');

            // 1. Cập nhật số lượng Badge
            if (data.length > 0) {
                badge.text(data.length);
                badge.show();

                // Lấy thông báo mới nhất (Giả sử API trả về mảng sắp xếp giảm dần theo ngày tạo)
                const newestNotif = data[0];

                // LOGIC QUAN TRỌNG:
                // Chỉ hiện Toast khi ID của tin mới > ID tin cũ đã xem
                // VÀ không phải là lần tải trang đầu tiên (để tránh F5 bị hiện lại tin cũ)
                if (newestNotif.id > lastSeenId) {

                    if (!isFirstLoad) {
                        // Toastr thông báo
                        toastr.info(newestNotif.noiDung, newestNotif.tieuDe);

                        // Phát tiếng (nếu cần)
                        // document.getElementById('audio-ting').play().catch(e=>{});
                    }

                    // Cập nhật lại ID mới nhất để lần sau không hiện lại tin này nữa
                    lastSeenId = newestNotif.id;
                }

            } else {
                badge.hide();
            }

            // Sau khi xử lý xong lần đầu, tắt cờ first load
            if(isFirstLoad && data.length > 0) {
                lastSeenId = data[0].id; // Gán luôn ID cao nhất hiện tại để không báo lại
            }
            isFirstLoad = false;

            // 2. Render danh sách (Giữ nguyên logic render của bạn)
            renderNotificationList(list, data);
        },
        error: function(err) {
            console.error("Lỗi tải thông báo", err);
        }
    });
}

// Tách hàm render ra cho gọn
function renderNotificationList(list, data) {
    if (data.length === 0) {
        list.html('<li class="text-center text-muted small py-4">Không có thông báo mới</li>');
    } else {
        let html = '';
        data.forEach(item => {
            let icon = 'bi-info-circle text-primary';
            let bgClass = item.trangThai === 0 ? 'bg-light' : ''; // Chưa đọc thì nền sáng

            if (item.loaiThongBao === 'STOCK') {
                icon = 'bi-exclamation-triangle-fill text-danger';
            } else if (item.loaiThongBao === 'ORDER') {
                icon = 'bi-bag-check-fill text-success';
            }

            let timeAgo = getTimeAgo(item.ngayTao);

            html += `
                <li>
                    <a class="dropdown-item d-flex align-items-start gap-2 py-2 border-bottom ${bgClass}" 
                       href="${item.urlLienKet || '#'}" 
                       onclick="markAsRead(${item.id})">
                        <div class="fs-4"><i class="bi ${icon}"></i></div>
                        <div class="w-100">
                            <div class="d-flex justify-content-between">
                                <strong class="small text-dark">${item.tieuDe}</strong>
                                <small class="text-muted" style="font-size:10px">${timeAgo}</small>
                            </div>
                            <div class="text-muted small text-truncate" style="max-width: 220px;">${item.noiDung}</div>
                        </div>
                    </a>
                </li>`;
        });
        list.html(html);
    }
}

// Các hàm phụ trợ giữ nguyên
function markAsRead(id) {
    $.post(`/api/notifications/read/${id}`, function() {
        // Không cần gọi fetch ngay lập tức nếu polling đang chạy nhanh, 
        // hoặc gọi fetchNotifications() nếu muốn cập nhật ngay lập tức.
        fetchNotifications();
    });
}

function getTimeAgo(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const seconds = Math.floor((now - date) / 1000);
    if (seconds < 60) return "Vừa xong";
    const minutes = Math.floor(seconds / 60);
    if (minutes < 60) return minutes + " phút trước";
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return hours + " giờ trước";
    return date.toLocaleDateString('vi-VN');
}

$(document).ready(function() {
    fetchNotifications();
    setInterval(fetchNotifications, 10000);
});