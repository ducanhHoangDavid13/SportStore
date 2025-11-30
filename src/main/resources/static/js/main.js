
    let lastCount = 0; // Để check xem có tin mới không

    function fetchNotifications() {
    $.ajax({
        url: '/api/notifications/unread',
        method: 'GET',
        success: function(data) {
            const badge = $('#notif-badge');
            const list = $('#notif-list');

            // 1. Cập nhật số lượng trên Badge
            if (data.length > 0) {
                badge.text(data.length);
                badge.show();

                // Nếu số lượng tăng lên -> Có tin mới -> Hiện Toast & Phát tiếng
                if (data.length > lastCount) {
                    // Toastr thông báo góc màn hình
                    toastr.info(data[0].noiDung, data[0].tieuDe);
                    // Phát tiếng (nếu có file)
                    // document.getElementById('audio-ting').play().catch(e=>{});
                }
                lastCount = data.length;
            } else {
                badge.hide();
                lastCount = 0;
            }

            // 2. Render danh sách xuống Dropdown
            if (data.length === 0) {
                list.html('<li class="text-center text-muted small py-4">Không có thông báo mới</li>');
            } else {
                let html = '';
                data.forEach(item => {
                    // Chọn Icon và Màu sắc theo Loại
                    let icon = 'bi-info-circle text-primary';
                    let bg = 'bg-white';

                    if (item.loaiThongBao === 'STOCK') {
                        icon = 'bi-exclamation-triangle-fill text-danger';
                        bg = 'bg-danger-subtle'; // Hơi đỏ nhẹ để cảnh báo
                    } else if (item.loaiThongBao === 'ORDER') {
                        icon = 'bi-bag-check-fill text-success';
                    }

                    // Tính thời gian (Vừa xong, 5 phút trước...)
                    let timeAgo = getTimeAgo(item.ngayTao);

                    html += `
                        <li>
                            <a class="dropdown-item d-flex align-items-start gap-2 py-2 border-bottom ${item.trangThai===0 ? 'bg-light' : ''}" 
                               href="${item.urlLienKet || '#'}" 
                               onclick="markAsRead(${item.id})">
                                <div class="fs-4">${'<i class="bi '+icon+'"></i>'}</div>
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
    });
}

    // Hàm đánh dấu đã đọc
    function markAsRead(id) {
    $.post(`/api/notifications/read/${id}`, function() {
        fetchNotifications(); // Load lại sau khi đọc
    });
}

    // Hàm tính thời gian
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

    // Chạy ngay khi load trang
    $(document).ready(function() {
    fetchNotifications();
    // Cứ 10 giây gọi API 1 lần (Polling)
    setInterval(fetchNotifications, 10000);
});
