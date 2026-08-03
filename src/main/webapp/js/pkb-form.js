document.addEventListener('DOMContentLoaded', function () {

    const formGrid = document.querySelector('.form-grid');
    if (!formGrid) {
        console.error("Không tìm thấy .form-grid.");
        return;
    }
    const APPOINTMENT_URL = formGrid.dataset.lichhenUrl;
    const PATIENT_SEARCH_URL = formGrid.dataset.patientUrl;

    // === PHẦN 1: LOGIC LỌC LỊCH HẸN ===
    const appointmentDateInput = document.getElementById('appointmentDate');
    const lichHenSelect = document.getElementById('lichHenId');
    const benhNhanDisplay = document.getElementById('benhNhanDisplay');
    const benhNhanHidden = document.getElementById('benhNhanId_hidden');
    const bacSiHiddenInput = document.querySelector('input[name="bacSiId"]');
    const bacSiSelect = document.getElementById('bacSiId');

    if (appointmentDateInput && lichHenSelect) {

        async function fetchAppointments() {
            const selectedDate = appointmentDateInput.value;
            if (!selectedDate) {
                lichHenSelect.innerHTML = '<option value="">-- Chọn ngày để lọc lịch hẹn --</option>';
                return;
            }

            // Lấy ID bác sĩ (ưu tiên bác sĩ đã đăng nhập, nếu không thì lấy từ dropdown)
            let bacSiId = null;
            if (bacSiHiddenInput) {
                bacSiId = bacSiHiddenInput.value;
            } else if (bacSiSelect) {
                bacSiId = bacSiSelect.value;
            }

            if (!bacSiId) {
                lichHenSelect.innerHTML = '<option value="">-- Vui lòng chọn bác sĩ trước --</option>';
                return;
            }

            lichHenSelect.innerHTML = '<option value="">-- Đang tải... --</option>';

            let url = `${APPOINTMENT_URL}${selectedDate}&bacSiId=${bacSiId}`;

            try {
                const response = await fetch(url);
                if (!response.ok)
                    throw new Error('Lỗi khi tải lịch hẹn');

                const appointments = await response.json();

                lichHenSelect.innerHTML = '<option value="">-- Chọn từ lịch hẹn --</option>';
                if (appointments.length === 0) {
                    lichHenSelect.innerHTML = '<option value="">-- Bác sĩ không có lịch hẹn ngày này --</option>';
                }

                appointments.forEach(app => {
                    const option = document.createElement('option');
                    option.value = app.id;
                    option.textContent = `STT ${app.stt} - ${app.tenBenhNhan} (${app.lyDoKham})`;
                    option.dataset.patientId = app.benhNhanId;
                    option.dataset.patientName = app.tenBenhNhan;
                    lichHenSelect.appendChild(option);
                });
            } catch (error) {
                console.error(error);
                lichHenSelect.innerHTML = '<option value="">-- Lỗi tải dữ liệu --</option>';
            }
        }

        // Gán sự kiện cho ô chọn ngày
        appointmentDateInput.addEventListener('change', fetchAppointments);
        // Nếu là Y tá, cũng lọc lại khi đổi bác sĩ
        if (bacSiSelect) {
            bacSiSelect.addEventListener('change', fetchAppointments);
        }

        // Gán sự kiện cho dropdown lịch hẹn
        lichHenSelect.addEventListener('change', function () {
            const selectedOption = this.options[this.selectedIndex];
            const patientId = selectedOption.dataset.patientId;
            const patientName = selectedOption.dataset.patientName;
            const openModalBtn = document.getElementById('openPatientSearchModal');

            if (patientId && benhNhanDisplay) {
                benhNhanDisplay.value = patientName;
                benhNhanHidden.value = patientId;
                if (openModalBtn)
                    openModalBtn.disabled = true;
            } else if (benhNhanDisplay) {
                benhNhanDisplay.value = "";
                benhNhanHidden.value = "";
                if (openModalBtn)
                    openModalBtn.disabled = false;
            }
        });
    }

    // === PHẦN 2: LOGIC POPUP TÌM BỆNH NHÂN ===
    const modalOverlay = document.getElementById('patient-modal-overlay');
    const openModalBtn = document.getElementById('openPatientSearchModal');

    if (modalOverlay && openModalBtn) {
        const closeModalBtn = document.getElementById('patient-close-button');
        const cancelModalBtn = document.getElementById('patient-cancel-button');
        const searchInput = document.getElementById('patientSearchInput');
        const searchResults = document.getElementById('patient-search-results');
        let searchTimer;

        const openModal = () => modalOverlay.classList.add('active');
        const closeModal = () => modalOverlay.classList.remove('active');

        openModalBtn.addEventListener('click', openModal);
        closeModalBtn.addEventListener('click', closeModal);
        cancelModalBtn.addEventListener('click', closeModal);
        modalOverlay.addEventListener('click', (e) => {
            if (e.target === modalOverlay)
                closeModal();
        });

        searchInput.addEventListener('keyup', () => {
            clearTimeout(searchTimer);
            const keyword = searchInput.value.trim();

            if (keyword.length < 2) {
                searchResults.innerHTML = '<p class="no-patient-results">Nhập ít nhất 2 ký tự để tìm...</p>';
                return;
            }
            
            //Debounce
            searchTimer = setTimeout(async () => {
                try {
                    const response = await fetch(`${PATIENT_SEARCH_URL}${keyword}`);
                    if (!response.ok)
                        throw new Error('Lỗi mạng');

                    const patients = await response.json();
                    displayPatientResults(patients);
                } catch (error) {
                    searchResults.innerHTML = '<p class="no-patient-results">Lỗi khi tìm kiếm.</p>';
                }
            }, 300);
        });

        function displayPatientResults(patients) {
            if (!patients || patients.length === 0) {
                searchResults.innerHTML = '<p class="no-patient-results">Không tìm thấy bệnh nhân nào.</p>';
                return;
            }

            const table = document.createElement('table');
            table.className = 'patient-result-table';
            table.innerHTML = `<thead><tr><th>Mã BN</th><th>Họ Tên</th><th>CCCD</th></tr></thead>`;
            const tbody = document.createElement('tbody');

            patients.forEach(p => {
                const tr = document.createElement('tr');
                tr.dataset.patientId = p.id;
                tr.dataset.patientName = p.hoTen;
                tr.dataset.patientMa = p.maBenhNhan;
                tr.innerHTML = `
                    <td>${p.maBenhNhan}</td>
                    <td>${p.hoTen}</td>
                    <td>${p.cccd}</td> 
                `;
                tr.addEventListener('click', () => selectPatient(p));
                tbody.appendChild(tr);
            });

            table.appendChild(tbody);
            searchResults.innerHTML = '';
            searchResults.appendChild(table);
        }

        function selectPatient(patient) {
            benhNhanDisplay.value = `${patient.hoTen} (Mã: ${patient.maBenhNhan})`;
            benhNhanHidden.value = patient.id;

            if (lichHenSelect)
                lichHenSelect.disabled = true;

            closeModal();
        }
    }
});