package filter; // Hoặc package của bạn

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Entity.TaiKhoan;
import model.dto.TaiKhoanDTO;

/**
 * Filter này kiểm soát việc xác thực (Authentication) và phân quyền
 * (Authorization) cho MainController.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/MainController"})
public class AuthFilter implements Filter {

    // --- Các Set (tập hợp) để chứa các action cho từng vai trò ---
    private Set<String> publicActions = new HashSet<>();
    private Set<String> commonActions = new HashSet<>();
    private Set<String> adminActions = new HashSet<>();
    private Set<String> doctorActions = new HashSet<>();
    private Set<String> receptionistActions = new HashSet<>();
    private Set<String> patientActions = new HashSet<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // --- Định nghĩa các nhóm Action (Copy từ MainController) ---

        // --- 1. Nhóm PUBLIC (Không cần đăng nhập) ---
        String[] userActions_Public = {"login", "register", "resendVerification"};
        String[] verifyActions = {"verify"};
        String[] resetActions = {"requestReset", "performReset"};
        String[] publicOnlyActions = {"viewDoctors"};
        publicActions.addAll(Arrays.asList(userActions_Public));
        publicActions.addAll(Arrays.asList(verifyActions));
        publicActions.addAll(Arrays.asList(resetActions));
        publicActions.addAll(Arrays.asList(publicOnlyActions));

        // --- 2. Nhóm COMMON (Chung cho mọi vai trò đã đăng nhập) ---
        String[] userThongBaoActions = {"viewMyNotifications", "markNotificationAsRead", "deleteMyNotification"};
        String[] thongBaoActions = {"createThongBao", "listNotifications"};
        String[] securityActions = {"showConfirmPassword", "confirmPassword", "showEditPhone", "savePhone", "showEditCCCD", "saveCCCD", "showEditName", "saveName", "showEditDOB", "saveDOB"};
        commonActions.add("logout");
        commonActions.addAll(Arrays.asList(securityActions)); // Tự quản lý tài khoản
        commonActions.addAll(Arrays.asList(userThongBaoActions)); // Xem thông báo cá nhân
        commonActions.addAll(Arrays.asList(thongBaoActions)); // "thông báo là chung"

        // --- 3. Nhóm BỆNH NHÂN (PATIENT) ---
        String[] patientLichHenActions = {"myAppointments", "showPatientBookingForm", "myAppointments", "bookAppointment", "getBacSiByKhoa", "cancelAppointment","printEncounter"};
        patientActions.addAll(Arrays.asList(patientLichHenActions)); // Tự đặt lịch hẹn
        // Các action tự xem/sửa hồ sơ cá nhân
        patientActions.addAll(Arrays.asList("showProfile", "showEditProfile", "saveProfile", "confirmAndLink", "showEditProfileWithExisting", "updateAndLink", "viewMyHistory"));

        // --- 4. Nhóm LỄ TÂN (RECEPTIONIST) ---
        String[] hoaDon_GiaoDichThanhToanActions = {"viewInvoice", "payInvoice", "listInvoices", "generateInvoice", "printInvoice"};
        String[] phongBenhActions = {"createRoom", "listRooms", "updateRoom", "getRoomForUpdate", "deleteRoom", "showCreateRoomForm"};
        String[] giuongBenhActions = {"assignBed", "releaseBed", "listBeds", "createBed", "deleteBed", "updateBed", "getBedForUpdate", "showCreateBedForm"};
        String[] NurseLichHenActions = {"showCreateAppointmentForm", "createAppointment", "getDoctorsByKhoa", "listLichHenNurse", "updateAppointmentStatus"};
        // Lễ tân quản lý thông tin bệnh nhân (thêm mới, sửa)
        String[] benhNhanActions_Reception = {"listBenhNhan", "showBenhNhanCreateForm", "createBenhNhan", "showBenhNhanEditForm", "updateBenhNhan", "softDeleteBenhNhan"};

        receptionistActions.addAll(Arrays.asList(hoaDon_GiaoDichThanhToanActions)); // Yêu cầu: "hóa đơn"
        receptionistActions.addAll(Arrays.asList(phongBenhActions)); // Yêu cầu: "phòng bệnh"
        receptionistActions.addAll(Arrays.asList(giuongBenhActions)); // Yêu cầu: "giường bệnh"
        receptionistActions.addAll(Arrays.asList(NurseLichHenActions)); // Lễ tân (hoặc Y tá) tạo lịch hẹn
        receptionistActions.addAll(Arrays.asList(benhNhanActions_Reception)); // Lễ tân quản lý bệnh nhân

        // --- 5. Nhóm BÁC SĨ (DOCTOR) ---
        String[] EMRCoreActions = {"printEncounter", "completeEncounter", "createEncounter", "updateEncounterDetails", "getEncounterDetails", "showCreateEncounterForm", "listAllEncounters", "viewEncounterDetails", "addServiceRequest", "updateServiceResult", "showUpdateEncounterForm", "updateEncounter", "getAppointmentsByDate", "searchPatients"};
        String[] DonThuocActions = {"addDetail", "updateDetail", "deleteDetail", "viewDetails", "listAll", "showCreateDonThuocForm", "createPrescription"};
        String[] CatalogActions = {"createService", "showCreateServiceForm", "createMedication", "showMedicationForm", "showUpdateForm", "updateMedicationInfo", "updateStock", "listMedications", "listAndSearchServices", "updateService", "showUpdateServiceForm", "deactivateService", "activateService", "activateMedication", "deactivateMedication"};
        String[] lichHenActions = {"listLichHen", "showLichHenCreateForm", "createLichHen", "updateLichHenStatus"};
        String[] benhNhanActions_Doctor = {"listBenhNhan", "viewMyHistory", "showProfile"}; // Bác sĩ xem DS và lịch sử

        doctorActions.addAll(Arrays.asList(EMRCoreActions)); // Yêu cầu: "còn lại là của bác sĩ"
        doctorActions.addAll(Arrays.asList(DonThuocActions));
        doctorActions.addAll(Arrays.asList(CatalogActions));
        doctorActions.addAll(Arrays.asList(lichHenActions));
        doctorActions.addAll(Arrays.asList(benhNhanActions_Doctor));

        // --- 6. Nhóm ADMIN ---
        String[] userActions_Admin = {"listUsers", "showUserCreateForm", "createUser", "showUserEditForm", "updateUserStatus", "showChangePasswordForm", "changePassword"};
        String[] khoaActions = {"listKhoa", "showKhoaCreateForm", "createKhoa", "showKhoaEditForm", "updateKhoa", "softDeleteKhoa"};
        String[] nhanVienActions = {"listNhanVien", "showNhanVienCreateForm", "createNhanVien", "showNhanVienEditForm", "updateNhanVien", "softDeleteNhanVien"};

        adminActions.addAll(Arrays.asList(userActions_Admin)); // Quản lý tài khoản
        adminActions.addAll(Arrays.asList(khoaActions)); // Quản lý khoa
        adminActions.addAll(Arrays.asList(nhanVienActions)); // Quản lý nhân viên
        adminActions.addAll(Arrays.asList(thongBaoActions)); // Admin tạo thông báo
        adminActions.addAll(Arrays.asList(benhNhanActions_Reception)); // Admin quản lý bệnh nhân

        // <<< THÊM CÁC DÒNG NÀY: Cho phép Admin quản lý lịch hẹn (giống Lễ tân/Bác sĩ)
        adminActions.addAll(Arrays.asList(NurseLichHenActions));
        adminActions.addAll(Arrays.asList(lichHenActions));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        String action = httpRequest.getParameter("action");

        // 1. Nếu action là null hoặc là action công khai, cho qua.
        if (action == null || publicActions.contains(action)) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Kiểm tra xem đã đăng nhập chưa
        TaiKhoanDTO user = null;
        if (session != null) {
            // Lấy "USER" từ session
            user = (TaiKhoanDTO) session.getAttribute("USER");
        }

        if (user == null) {
            // 3. Chưa đăng nhập -> Về trang login
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp");
            return;
        }

        // 4. Đã đăng nhập. Kiểm tra các action chung
        if (commonActions.contains(action)) {
            chain.doFilter(request, response);
            return;
        }

        // 5. Kiểm tra phân quyền (Authorization) dựa trên vai trò
        String role = user.getVaiTro();
        boolean authorized = false;

        // Đảm bảo String trả về từ user.getVaiTro() khớp CHÍNH XÁC
        // với các case dưới đây (ví dụ: "QUAN_TRI", "BAC_SI", "LE_TAN", "BENH_NHAN")
        switch (role) {
            case "QUAN_TRI":
                if (adminActions.contains(action)) {
                    authorized = true;
                }
                break;
            case "BAC_SI":
                if (doctorActions.contains(action)) {
                    authorized = true;
                }
                break;
            case "LE_TAN":
                if (receptionistActions.contains(action)) {
                    authorized = true;
                }
                break;
            case "BENH_NHAN":
                if (patientActions.contains(action)) {
                    authorized = true;
                }
                break;
            default:
                authorized = false;
        }

        // 6. Xử lý kết quả phân quyền
        if (authorized) {
            // Được phép -> Cho đi tiếp
            chain.doFilter(request, response);
        } else {
            // 7. Không được phép -> Chuyển đến trang lỗi
            System.out.println("CẢNH BÁO: User '" + user.getTenDangNhap() + "' (Vai trò: " + role + ") "
                    + "đã cố gắng truy cập action bị cấm: " + action);

            request.setAttribute("errorMessage", "Bạn không có quyền truy cập chức năng này.");
            httpRequest.getRequestDispatcher("error.jsp").forward(httpRequest, httpResponse);
        }
    }

    @Override
    public void destroy() {
        // Dọn dẹp tài nguyên (nếu có)
    }
}

