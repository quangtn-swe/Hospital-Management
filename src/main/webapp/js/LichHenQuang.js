const contextPath = '${pageContext.request.contextPath}';
const bacSiSelect = document.getElementById('bacSiId');
const bacSiLoading = document.getElementById('bacSiLoading');

async function loadBacSi(selectElement) {
    const khoaId = selectElement.value;
    bacSiSelect.innerHTML = ''; // Xóa option cũ

    if (!khoaId) {
        bacSiSelect.innerHTML = '<option value="">-- Vui lòng chọn khoa trước --</option>';
        return;
    }

    // Hiển thị trạng thái đang tải
    bacSiLoading.style.display = 'block';
    bacSiSelect.disabled = true;

    try {
        const url = "MainController?action=getBacSiByKhoa&khoaId=" + encodeURIComponent(khoaId);
        console.log("URL gửi đi:", url);

        const response = await fetch(url);
        const text = await response.text();
        console.log("Kết quả server trả về:", text);

        let bacSiList;
        try {
            bacSiList = JSON.parse(text);
        } catch (err) {
            console.error("Không thể parse JSON:", err);
            bacSiSelect.innerHTML = '<option value="">-- Dữ liệu phản hồi không hợp lệ --</option>';
            return;
        }

        if (!Array.isArray(bacSiList) || bacSiList.length === 0) {
            bacSiSelect.innerHTML = '<option value="">-- Khoa này không có bác sĩ --</option>';
        } else {
            // Hiển thị danh sách bác sĩ - SỬA Ở ĐÂY
            bacSiSelect.innerHTML = '<option value="">-- Chọn bác sĩ --</option>';
            bacSiList.forEach(bacSi => {
                const option = document.createElement('option');
                option.value = bacSi.id;
                const chuyenMonText = bacSi.chuyenMon && bacSi.chuyenMon !== "false"
                        ? bacSi.chuyenMon
                        : "Chưa cập nhật";
                // SỬA: Dùng string concatenation
                option.textContent = bacSi.hoTen + ' (' + chuyenMonText + ')';
                bacSiSelect.appendChild(option);
            });
        }
    } catch (error) {
        console.error('Lỗi AJAX:', error);
        bacSiSelect.innerHTML = '<option value="">-- Lỗi tải danh sách --</option>';
    } finally {
        bacSiLoading.style.display = 'none';
        bacSiSelect.disabled = false;
    }
}
