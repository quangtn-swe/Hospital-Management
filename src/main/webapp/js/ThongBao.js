/* ==========================================================================
     LOGIC CHO TRANG QUẢN LÝ THÔNG BÁO (thongBao.js)
     **Lưu ý:** File này giả định rằng 2 biến 'roles' và 'accounts'
     đã được định nghĩa sẵn trong file JSP trước khi file này được tải.
 ========================================================================== */

/**
 * Hàm này xử lý việc ẩn/hiện và điền dữ liệu cho ô <select>
 * dựa trên radio button được chọn (Tất cả, Vai trò, Tài khoản).
 */
function toggleTargetValue() {

    // Tìm form 1 lần
    const form = document.getElementById('createForm');
    if (!form)
        return; // Thoát nếu không tìm thấy form

    // Lấy giá trị radio đang được chọn
    const targetType = form.querySelector('input[name="targetType"]:checked').value;

    // Lấy các element liên quan
    const targetValueDiv = document.getElementById('targetValueDiv');
    const targetValueLabel = document.getElementById('targetValueLabel');
    const targetValueInput = document.getElementById('targetValueInput');

    targetValueInput.innerHTML = ''; // Luôn xóa các option cũ khi hàm chạy

    if (targetType === 'ALL') {
        // --- TRƯỜNG HỢP 1: GỬI TẤT CẢ ---

        targetValueDiv.style.display = 'none'; // Ẩn select
        targetValueInput.removeAttribute('required');
        targetValueInput.name = ''; // Vô hiệu hóa <select> để nó không gửi đi

        // Thêm/cập nhật input ẩn để gửi giá trị "ALL"
        let hiddenInput = document.getElementById('hiddenTargetValue');
        if (!hiddenInput) {
            hiddenInput = document.createElement('input');
            hiddenInput.type = 'hidden';
            hiddenInput.id = 'hiddenTargetValue';
            form.appendChild(hiddenInput);
        }
        hiddenInput.name = 'targetValue'; // Kích hoạt input này
        hiddenInput.value = 'ALL';

    } else {
        // --- TRƯỜNG HỢP 2 & 3: GỬI THEO VAI TRÒ / TÀI KHOẢN ---

        // Xóa input ẩn (nếu có)
        let hiddenInput = document.getElementById('hiddenTargetValue');
        if (hiddenInput) {
            hiddenInput.removeAttribute('name'); // Vô hiệu hóa input ẩn
        }

        // Hiển thị và kích hoạt <select>
        targetValueInput.name = 'targetValue';
        targetValueDiv.style.display = 'block';
        targetValueInput.setAttribute('required', 'required');

        if (targetType === 'ROLE') {
            // Điền dữ liệu cho Vai trò (dùng biến 'roles' toàn cục từ JSP)
            targetValueLabel.textContent = 'Chọn vai trò:';
            roles.forEach(role => {
                const option = document.createElement('option');
                option.value = role;
                option.textContent = role;
                targetValueInput.appendChild(option);
            });
        } else if (targetType === 'USER') {
            // Điền dữ liệu cho Tài khoản (dùng biến 'accounts' toàn cục từ JSP)
            targetValueLabel.textContent = 'Chọn tài khoản:';

            // Thêm option mặc định
            const defaultOption = document.createElement('option');
            defaultOption.value = '';
            defaultOption.textContent = '-- Chọn tài khoản --';
            targetValueInput.appendChild(defaultOption);

            accounts.forEach(acc => {
                const option = document.createElement('option');
                option.value = acc.id;
                option.textContent = acc.username + ' (ID: ' + acc.id + ')';
                targetValueInput.appendChild(option);
            });
        }
    }
}

// Chạy code khi toàn bộ cây DOM đã được tải xong
document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('createForm');
    if (!form)
        return; // Không làm gì nếu không có form

    // 1. Gắn event listeners cho tất cả radio button 'targetType'
    const radioButtons = form.querySelectorAll('input[name="targetType"]');
    radioButtons.forEach(radio => {
        radio.addEventListener('change', toggleTargetValue);
    });

    // 2. Chạy hàm lần đầu tiên khi tải trang để cài đặt trạng thái ban đầu
    toggleTargetValue();
});