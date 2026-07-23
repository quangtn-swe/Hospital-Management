package controller;

import exception.ValidationException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.dto.BenhNhanDTO;
import model.dto.PhieuKhamBenhDTO;
import model.dto.TaiKhoanDTO;
import service.BenhNhanService;
import service.PhieuKhamBenhService;
import service.TaiKhoanService;
//1
/**
 * Controller xử lý các nghiệp vụ liên quan đến Bệnh Nhân (Patient). (ĐÃ NÂNG
 * CẤP: Thêm Tìm kiếm, Sửa lỗi Xóa Mềm, Sửa lỗi Ngày sinh, Thêm Mã dự kiến)
 */
@WebServlet(name = "BenhNhanController", urlPatterns = {"/BenhNhanController"})
public class BenhNhanController extends HttpServlet {

    private static final String BENHNHAN_LIST_PAGE = "admin/danhSachBenhNhan.jsp";
    private static final String BENHNHAN_FORM_PAGE = "admin/formBenhNhan.jsp";
    private static final String ERROR_PAGE = "error.jsp";
    private static final String EDIT_PROFILE_PAGE = "user/editProfile.jsp";
    private static final String VIEW_PROFILE_PAGE = "user/viewProfile.jsp";
    private static final String HOME_PAGE = "index.jsp";
    private static final String CONFIRM_PROFILE_PAGE = "user/confirmProfile.jsp";
    private static final int PAGE_SIZE = 10;
    private final BenhNhanService benhNhanService = new BenhNhanService();
    private final TaiKhoanService taiKhoanService = new TaiKhoanService();
    private static final String HISTORY_PAGE = "LichSuKhamBenh.jsp";

    private final PhieuKhamBenhService phieuKhamService = new PhieuKhamBenhService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        String url = ERROR_PAGE;
        boolean isRedirect = false;

        try {
            if (action == null || action.isEmpty()) {
                action = "listBenhNhan"; // Mặc định cho Admin
            }

            switch (action) {
                // --- Logic của Admin ---
                case "listBenhNhan":
                    url = listBenhNhan(request); // <-- ĐÃ SỬA DÙNG PHÂN TRANG & TÌM KIẾM
                    break;
                case "showBenhNhanCreateForm":
                    url = showBenhNhanCreateForm(request); // <-- ĐÃ SỬA (Thêm Mã dự kiến)
                    break;
                case "showBenhNhanEditForm":
                    url = showBenhNhanEditForm(request);
                    break;

                // --- Logic Bệnh nhân ---
                //=====================Dat===============
                case "viewMyHistory":
                    url = viewMyHistory(request);
                    break;
                //=-===========================
                case "showProfile":
                    url = showViewProfile(request);
                    if (url.startsWith("BenhNhanController")) {
                        isRedirect = true;
                    }
                    break;
                case "showEditProfile":
                    url = showEditProfile(request, null);
                    break;
                case "showEditProfileWithExisting":
                    url = showEditProfileWithExisting(request);
                    break;
                default:
                    request.setAttribute("ERROR_MESSAGE", "Hành động '" + action + "' không hợp lệ cho GET.");
            }
        } catch (ValidationException e) {
            log("Lỗi Validation tại BenhNhanController (doGet): " + e.getMessage(), e);
            request.setAttribute("ERROR_MESSAGE", "Lỗi nghiệp vụ: " + e.getMessage());
            url = ERROR_PAGE;
        } catch (Exception e) {
            log("Lỗi Hệ thống tại BenhNhanController (doGet): " + e.getMessage(), e);
            request.setAttribute("ERROR_MESSAGE", "Đã xảy ra lỗi hệ thống: " + e.getMessage());
            url = ERROR_PAGE;
        } finally {
            if (isRedirect) {
                response.sendRedirect(request.getContextPath() + "/" + url);
            } else {
                request.getRequestDispatcher(url).forward(request, response);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        String url = ERROR_PAGE;
        String formErrorPage = ERROR_PAGE;

        try {
            if (action == null || action.isEmpty()) {
                throw new ValidationException("Hành động không được chỉ định.");
            }

            if ("createBenhNhan".equals(action) || "updateBenhNhan".equals(action)) {
                formErrorPage = BENHNHAN_FORM_PAGE;
            } else if ("softDeleteBenhNhan".equals(action)) {
                formErrorPage = BENHNHAN_LIST_PAGE;
            } else if ("saveProfile".equals(action)) {
                formErrorPage = EDIT_PROFILE_PAGE;
            } else if ("confirmAndLink".equals(action)) {
                formErrorPage = CONFIRM_PROFILE_PAGE;
            }

            switch (action) {
                case "createBenhNhan":
                    url = createBenhNhan(request);
                    break;
                case "updateBenhNhan":
                    url = updateBenhNhan(request);
                    break;
                case "softDeleteBenhNhan":
                    url = softDeleteBenhNhan(request);
                    break;
                case "saveProfile":
                    url = saveProfile(request);
                    if (url.equals(HOME_PAGE)) {
                        url = "redirect:" + HOME_PAGE;
                    }
                    break;
                case "confirmAndLink":
                    url = confirmAndLink(request);
                    if (url.equals(HOME_PAGE)) {
                        url = "redirect:" + HOME_PAGE;
                    }
                    break;
                default:
                    request.setAttribute("ERROR_MESSAGE", "Hành động '" + action + "' không hợp lệ cho POST.");
            }
        } catch (ValidationException e) {
            log("Lỗi Validation tại BenhNhanController (doPost): " + e.getMessage(), e);
            if ("saveProfile".equals(action)) {
                request.setAttribute("ERROR_MESSAGE", e.getMessage());
                request.setAttribute("BENHNHAN_DATA", createDTOFromRequest(request));
                request.setAttribute("formAction", "saveProfile");
            } else if ("confirmAndLink".equals(action)) {
                request.setAttribute("ERROR_MESSAGE", e.getMessage());
                try {
                    int patientId = Integer.parseInt(request.getParameter("patientId"));
                    request.setAttribute("EXISTING_PATIENT", benhNhanService.getBenhNhanByIdEvenIfInactive(patientId));
                } catch (Exception ex) {
                    /* Bỏ qua */ }
            } else {
                handleServiceException(request, e, action);
            }
            url = formErrorPage;
        } catch (Exception e) {
            log("Lỗi Hệ thống tại BenhNhanController (doPost): " + e.getMessage(), e);
            request.setAttribute("ERROR_MESSAGE", "Đã xảy ra lỗi hệ thống: " + e.getMessage());
            url = ERROR_PAGE;
        } finally {
            if (url.startsWith("redirect:")) {
                String redirectUrl = url.substring("redirect:".length());
                if (redirectUrl.startsWith(request.getContextPath()) || redirectUrl.startsWith("BenhNhanController")) {
                    response.sendRedirect(redirectUrl);
                } else {
                    response.sendRedirect(request.getContextPath() + "/" + redirectUrl);
                }
            } else {
                request.getRequestDispatcher(url).forward(request, response);
            }
        }
    }

    // =========================================================================
    // === CÁC HÀM ADMIN (ĐÃ NÂNG CẤP) ===
    // =========================================================================
    /**
     * === NÂNG CẤP (PHÂN TRANG + TÌM KIẾM) ===
     */
    private String listBenhNhan(HttpServletRequest request) throws Exception {
        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                page = Integer.parseInt(pageStr);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }
        String keyword = request.getParameter("keyword");
        List<BenhNhanDTO> list;
        long totalBenhNhan;

        if (keyword != null && !keyword.trim().isEmpty()) {
            String trimmedKeyword = keyword.trim();
            list = benhNhanService.searchBenhNhanPaginated(trimmedKeyword, page, PAGE_SIZE);
            totalBenhNhan = benhNhanService.getBenhNhanSearchCount(trimmedKeyword);
            request.setAttribute("searchKeyword", keyword);
        } else {
            list = benhNhanService.getAllBenhNhanPaginated(page, PAGE_SIZE);
            totalBenhNhan = benhNhanService.getBenhNhanCount();
        }

        long totalPages = (long) Math.ceil((double) totalBenhNhan / PAGE_SIZE);
        request.setAttribute("LIST_BENHNHAN", list);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        return BENHNHAN_LIST_PAGE;
    }

    /**
     * === NÂNG CẤP (LẤY MÃ DỰ KIẾN) ===
     */
    private String showBenhNhanCreateForm(HttpServletRequest request) {
        loadFormDependencies(request, "createBenhNhan"); // Không cần load gì
        request.setAttribute("formAction", "createBenhNhan");

        try {
            // Gọi hàm "nhìn trộm"
            String nextMa = benhNhanService.getNextMaBenhNhan();
            request.setAttribute("NEXT_MA_BENH_NHAN", nextMa);
        } catch (Exception e) {
            log("Lỗi khi tạo mã bệnh nhân mới: " + e.getMessage(), e);
        }

        return BENHNHAN_FORM_PAGE;
    }

    private String showBenhNhanEditForm(HttpServletRequest request) throws Exception {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            BenhNhanDTO benhNhan = benhNhanService.getBenhNhanById(id); // Service đã lọc
            request.setAttribute("BENHNHAN_DATA", benhNhan);
            loadFormDependencies(request, "updateBenhNhan");
            request.setAttribute("formAction", "updateBenhNhan");
            return BENHNHAN_FORM_PAGE;
        } catch (NumberFormatException e) {
            throw new ValidationException("ID Bệnh nhân không hợp lệ.");
        }
    }

    private String createBenhNhan(HttpServletRequest request) throws ValidationException, Exception {
        BenhNhanDTO newBenhNhanDTO = createDTOFromRequest(request);
        BenhNhanDTO result = benhNhanService.createBenhNhan(newBenhNhanDTO);
        request.getSession().setAttribute("SUCCESS_MESSAGE", "Tạo bệnh nhân '" + result.getHoTen() + "' thành công!");
        return "redirect:MainController?action=listBenhNhan";
    }

    private String updateBenhNhan(HttpServletRequest request) throws ValidationException, Exception {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            BenhNhanDTO benhNhanDTO = createDTOFromRequest(request);
            BenhNhanDTO result = benhNhanService.updateBenhNhan(id, benhNhanDTO);
            request.getSession().setAttribute("SUCCESS_MESSAGE", "Cập nhật bệnh nhân '" + result.getHoTen() + "' thành công!");
            return "redirect:MainController?action=listBenhNhan";
        } catch (NumberFormatException e) {
            throw new ValidationException("ID Bệnh nhân không hợp lệ khi cập nhật.");
        }
    }

    /**
     * === NÂNG CẤP (LOGIC XÓA MỀM) === Sửa lại để gọi hàm Xóa Mềm mới trong
     * Service
     */
    private String softDeleteBenhNhan(HttpServletRequest request) throws ValidationException, Exception {
        try {
            int id = Integer.parseInt(request.getParameter("id"));

            // Sửa: Gọi hàm Xóa Mềm mới (hàm này sẽ xử lý cả Bệnh Nhân và Tài Khoản)
            benhNhanService.softDeleteBenhNhan(id);

            request.getSession().setAttribute("SUCCESS_MESSAGE", "Đã vô hiệu hóa bệnh nhân và tài khoản liên kết (nếu có) thành công!");
            return "redirect:MainController?action=listBenhNhan";

        } catch (NumberFormatException e) {
            throw new ValidationException("ID Bệnh nhân không hợp lệ.");
        }
    }

    /**
     * SỬA: Bỏ logic tải "Tài khoản"
     */
    private void loadFormDependencies(HttpServletRequest request, String formAction) {
        // (Không cần tải gì nữa vì form chỉ cần nhập tay)
    }

    /**
     * SỬA: Bỏ logic tải "Tài khoản"
     */
    private void handleServiceException(HttpServletRequest request, Exception e, String formAction) {
        request.setAttribute("ERROR_MESSAGE", e.getMessage());
        request.setAttribute("BENHNHAN_DATA", createDTOFromRequest(request));
        request.setAttribute("formAction", formAction);
        // loadFormDependencies(request, formAction); // (Không cần tải lại)
    }

    /**
     * === NÂNG CẤP (THÊM AVATAR BASE64) ===
     */
    private BenhNhanDTO createDTOFromRequest(HttpServletRequest request) {
        BenhNhanDTO dto = new BenhNhanDTO();
        String idStr = request.getParameter("id");
        if (idStr != null && !idStr.isEmpty()) {
            try {
                dto.setId(Integer.parseInt(idStr));
            } catch (NumberFormatException e) {
                /* ignore */ }
        }
        java.util.function.Function<String, String> safeTrim = (s) -> (s != null) ? s.trim() : null;
        dto.setMaBenhNhan(safeTrim.apply(request.getParameter("maBenhNhan")));
        dto.setHoTen(safeTrim.apply(request.getParameter("hoTen")));
        dto.setGioiTinh(safeTrim.apply(request.getParameter("gioiTinh")));
        dto.setDiaChi(safeTrim.apply(request.getParameter("diaChi")));
        dto.setSoDienThoai(safeTrim.apply(request.getParameter("soDienThoai")));
        dto.setNhomMau(safeTrim.apply(request.getParameter("nhomMau")));
        dto.setTienSuBenh(safeTrim.apply(request.getParameter("tienSuBenh")));
        dto.setCccd(safeTrim.apply(request.getParameter("cccd")));

        // === SỬA LỖI (PARSE NGÀY SINH) ===
        String ngaySinhStr = request.getParameter("ngaySinh");
        if (ngaySinhStr != null && !ngaySinhStr.isEmpty()) {
            try {
                dto.setNgaySinh(LocalDate.parse(ngaySinhStr));
            } catch (DateTimeParseException e) {
                log("Lỗi parse ngày sinh: " + ngaySinhStr + ". Yêu cầu định dạng yyyy-MM-dd.");
            }
        }

        // ================================================================
        // === ✨ BẮT ĐẦU THÊM MỚI (UPLOAD AVATAR) ✨ ===
        // ================================================================
        // Giả sử input ẩn của bạn tên là "avatarBenhNhanBase64"
        String avatarString = request.getParameter("avatarBenhNhanBase64");
        if (avatarString != null) {
            // Không cần trim() vì chuỗi base64 không có khoảng trắng thừa
            dto.setAvatarBase64(avatarString);
        }
        // ================================================================
        // === ✨ KẾT THÚC THÊM MỚI ✨ ===
        // ================================================================

        dto.setTaiKhoanId(null);

        return dto;
    }

    // =========================================================================
    // === CÁC HÀM CỦA BỆNH NHÂN (GIỮ NGUYÊN) ===
    // =========================================================================
    private String showViewProfile(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("USER") == null) {
            throw new ValidationException("Vui lòng đăng nhập để xem hồ sơ.");
        }
        TaiKhoanDTO currentUser = (TaiKhoanDTO) session.getAttribute("USER");
        BenhNhanDTO benhNhan = benhNhanService.getBenhNhanByTaiKhoanId(currentUser.getId());
        if (benhNhan != null) {
            request.setAttribute("BENHNHAN_DATA", benhNhan);
            return VIEW_PROFILE_PAGE;
        } else {
            return "BenhNhanController?action=showEditProfile";
        }
    }

    private String showEditProfile(HttpServletRequest request, BenhNhanDTO dataToLoad) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("USER") == null) {
            throw new ValidationException("Vui lòng đăng nhập để xem hồ sơ.");
        }
        TaiKhoanDTO currentUser = (TaiKhoanDTO) session.getAttribute("USER");
        BenhNhanDTO benhNhan = dataToLoad;
        if (benhNhan == null) {
            benhNhan = benhNhanService.getBenhNhanByTaiKhoanId(currentUser.getId());
        }
        if (benhNhan != null) {
            request.setAttribute("BENHNHAN_DATA", benhNhan);
        } else {
            BenhNhanDTO emptyProfile = new BenhNhanDTO();
            emptyProfile.setHoTen(currentUser.getTenDangNhap());
            request.setAttribute("BENHNHAN_DATA", emptyProfile);
        }
        request.setAttribute("formAction", "saveProfile");
        return EDIT_PROFILE_PAGE;
    }

    private String showEditProfileWithExisting(HttpServletRequest request) throws Exception {
        try {
            int patientId = Integer.parseInt(request.getParameter("patientId"));
            BenhNhanDTO existingPatient = benhNhanService.getBenhNhanByIdEvenIfInactive(patientId);
            if (existingPatient == null) {
                throw new ValidationException("Không tìm thấy hồ sơ.");
            }
            return showEditProfile(request, existingPatient);
        } catch (NumberFormatException e) {
            throw new ValidationException("ID không hợp lệ.");
        }
    }

    private String confirmAndLink(HttpServletRequest request) throws ValidationException, Exception {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("USER") == null) {
            throw new ValidationException("Phiên đăng nhập hết hạn.");
        }
        TaiKhoanDTO currentUser = (TaiKhoanDTO) session.getAttribute("USER");
        try {
            int patientId = Integer.parseInt(request.getParameter("patientId"));
            BenhNhanDTO existingPatient = benhNhanService.getBenhNhanByIdEvenIfInactive(patientId);
            if (existingPatient == null) {
                throw new ValidationException("Không tìm thấy hồ sơ.");
            }
            if (existingPatient.getTaiKhoanId() != null && existingPatient.getTaiKhoanId() != currentUser.getId()) {
                throw new ValidationException("Hồ sơ này đã được liên kết với tài khoản khác.");
            }
            if (existingPatient.getTaiKhoanId() == null) {
                benhNhanService.linkAccountToPatient(patientId, currentUser.getId());
            }
            session.setAttribute("SUCCESS_MESSAGE", "Chào mừng trở lại! Hồ sơ y tế của bạn đã được liên kết thành công.");
            return HOME_PAGE;
        } catch (NumberFormatException e) {
            throw new ValidationException("ID không hợp lệ.");
        }
    }

    private String saveProfile(HttpServletRequest request) throws ValidationException, Exception {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("USER") == null) {
            throw new ValidationException("Phiên đăng nhập hết hạn.");
        }
        TaiKhoanDTO currentUser = (TaiKhoanDTO) session.getAttribute("USER");
        BenhNhanDTO dtoFromForm = createDTOFromRequest(request);
        Integer benhNhanId = dtoFromForm.getId();
        if (benhNhanId != null && benhNhanId > 0) {
            BenhNhanDTO existingPatient = benhNhanService.getBenhNhanById(benhNhanId);
            if (existingPatient == null) {
                throw new ValidationException("Hồ sơ bạn đang cố sửa không còn tồn tại.");
            }
            if (existingPatient.getTaiKhoanId() == null || existingPatient.getTaiKhoanId() != currentUser.getId()) {
                if (existingPatient.getTaiKhoanId() != null) {
                    throw new ValidationException("Lỗi bảo mật: Bạn không có quyền sửa hồ sơ này.");
                }
            }
            if (existingPatient.getTaiKhoanId() == null) {
                benhNhanService.linkAccountToPatient(benhNhanId, currentUser.getId());
                session.setAttribute("SUCCESS_MESSAGE", "Hồ sơ được cập nhật và liên kết thành công!");
            } else {
                session.setAttribute("SUCCESS_MESSAGE", "Cập nhật hồ sơ thành công!");
            }
            dtoFromForm.setTaiKhoanId(currentUser.getId());
            benhNhanService.updateBenhNhan(benhNhanId, dtoFromForm);
            return HOME_PAGE;
        } else {
            String cccd = dtoFromForm.getCccd();
            if (cccd == null || cccd.isEmpty()) {
                throw new ValidationException("CCCD là thông tin bắt buộc.");
            }
            BenhNhanDTO existingPatientByCccd = benhNhanService.findByCccd(cccd);
            if (existingPatientByCccd != null) {
                if (existingPatientByCccd.getTaiKhoanId() != null && existingPatientByCccd.getTaiKhoanId() != currentUser.getId()) {
                    throw new ValidationException("CCCD này đã được liên kết với một tài khoản khác.");
                }
                request.setAttribute("EXISTING_PATIENT", existingPatientByCccd);
                return CONFIRM_PROFILE_PAGE;
            } else {
                dtoFromForm.setTaiKhoanId(currentUser.getId());
                benhNhanService.createBenhNhan(dtoFromForm);
                session.setAttribute("SUCCESS_MESSAGE", "Tạo và liên kết hồ sơ thành công!");
                return HOME_PAGE;
            }
        }
    }

    //======================Dat===========================
    /**
     * Lấy lịch sử khám bệnh của bệnh nhân đang đăng nhập.
     */
    private String viewMyHistory(HttpServletRequest request) throws ValidationException {
        HttpSession session = request.getSession(false);
        try {
            // 1. Lấy tài khoản từ session
            TaiKhoanDTO taiKhoan = (TaiKhoanDTO) session.getAttribute("USER");
            if (taiKhoan == null) {
                throw new ValidationException("Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại.");
            }

            // 2. Lấy thông tin Bệnh nhân từ Tài khoản
            BenhNhanDTO benhNhan = benhNhanService.getBenhNhanByTaiKhoanId(taiKhoan.getId());
            if (benhNhan == null) {
                throw new ValidationException("Không tìm thấy hồ sơ bệnh nhân cho tài khoản này.");
            }

            // 3. Lấy lịch sử khám từ ID Bệnh nhân
            List<PhieuKhamBenhDTO> lichSuKham = phieuKhamService.getHistoryForPatient(benhNhan.getId());

            // 4. Gửi danh sách đến JSP
            request.setAttribute("danhSachLichSuKham", lichSuKham);
            request.setAttribute("benhNhan", benhNhan); 

        } catch (ValidationException e) {
            request.getSession().setAttribute("ERROR_MESSAGE", e.getMessage());
        } catch (Exception e) {
            request.getSession().setAttribute("ERROR_MESSAGE","Vui lòng thử lại sau");
        }

        return HISTORY_PAGE;
    }

    @Override
    public String getServletInfo() {
        return "Controller quản lý các nghiệp vụ liên quan đến Bệnh Nhân.";
    }
}
