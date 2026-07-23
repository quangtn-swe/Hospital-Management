/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;
//test2
import exception.ValidationException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.dto.DichVuDTO;
import model.dto.ThuocDTO;
import service.DichVuService;
import service.ThuocService;

/**
 *
 * @author SunnyU
 */
@WebServlet(name = "CatalogController", urlPatterns = {"/CatalogController"})
public class CatalogController extends HttpServlet {
    
    private static final String ERROR_PAGE = "error.jsp";
    private static final String CREATE_SERVICE_PAGE = "DichVu.jsp";
    private static final String CREATE_MEDICATION_PAGE = "Thuoc.jsp";
    private static final String THUOC_LIST_PAGE = "DanhSachThuoc.jsp";
    private static final String DICH_VU_LIST_PAGE = "DanhSachDichVu.jsp";
    
    private static final DichVuService dichVuService = new DichVuService();
    private static final ThuocService thuocService = new ThuocService();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        
        String action = request.getParameter("action");
        String url = ERROR_PAGE;
        try {
            if (action == null || action.isEmpty()) {
                // Nếu không có action, có thể chuyển về trang chủ
                // url = "home.jsp";
                throw new Exception("Hành động không được chỉ định.");
            }
            
            switch (action) {
                case "showUpdateServiceForm":
                    url = showUpdateForm(request);
                    break;
                case "listAndSearchServices":
                    url = listAndSearchServices(request);
                    break;
                //tạo 1 dịch vụ mới
                case "showCreateServiceForm":
                    url = showServiceForm(request);
                    break;
                //tạo đơn thuốc
                case "showMedicationForm":
                    url = showMedicationForm(request);
                    break;
                //update đơn thuốc
                case "showUpdateForm":
                    url = showUpdateMedicationForm(request);
                    break;
                //show và search thuóc
                case "listMedications":
                    url = listAndSearchMedications(request);
                    break;
                default:
                    request.setAttribute("ERROR_MESSAGE", "Hành động '" + action + "' không hợp lệ cho phương thức GET.");
            }
        } catch (Exception e) {
            log("Lỗi tại EMRCoreController (doGet): " + e.getMessage(), e);
            request.setAttribute("ERROR_MESSAGE", "Đã có lỗi xảy ra: " + e.getMessage());
        } finally {
            request.getRequestDispatcher(url).forward(request, response);
        }
        
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        
        String action = request.getParameter("action");
        String url = ERROR_PAGE;
        
        try {
            if (action == null || action.isEmpty()) {
                throw new Exception("Hành động không được chỉ định.");
            }
            
            switch (action) {
                case "deactivateService":
                    url = updateServiceStatus(request, "NGUNG_SU_DUNG");
                    break;
                case "activateService":
                    url = updateServiceStatus(request, "SU_DUNG");
                    break;
                case "deactivateMedication":
                    url = updateThuocStatus(request, "NGUNG_SU_DUNG");
                    break;
                case "activateMedication":
                    url = updateThuocStatus(request, "SU_DUNG");
                    break;
                case "updateService":
                    url = updateService(request);
                    break;
                case "createService":
                    url = createService(request);
                    break;
                case "createMedication":
                    url = createMedication(request);
                    break;
                case "updateMedicationInfo":
                    url = updateMedicationInfo(request);
                    break;
                case "updateStock":
                    url = updateMedicationStock(request);
                    break;
                case "deleteMedication":
                    url = deleteMedication(request);
                    break;
                case "deleteService":
                    url = deleteService(request);
                    break;
                default:
                    request.setAttribute("ERROR_MESSAGE", "Hành động '" + action + "' không hợp lệ cho phương thức POST.");
            }
            
            if (url.startsWith("redirect:")) {
                String redirectUrl = url.substring("redirect:".length());
                response.sendRedirect(request.getContextPath() + redirectUrl);
            } else {
                request.getRequestDispatcher(url).forward(request, response);
            }
        } catch (Exception e) {
            log("Lỗi tại EMRCoreController (doPost): " + e.getMessage(), e);
            request.setAttribute("ERROR_MESSAGE", "Đã có lỗi nghiêm trọng xảy ra: " + e.getMessage());
        }
    }

    // chuyển hướng đến trang jsp    
    private String showServiceForm(HttpServletRequest request) {
        return CREATE_SERVICE_PAGE;
    }
    
    private String showMedicationForm(HttpServletRequest request) {
        return CREATE_MEDICATION_PAGE;
    }
    
    private String showUpdateMedicationForm(HttpServletRequest request) throws Exception {
        int thuocId = Integer.parseInt(request.getParameter("id"));
        ThuocDTO thuoc = thuocService.getMedicationById(thuocId);
        if (thuoc == null) {
            throw new Exception("Không tìm thấy thuốc để cập nhật.");
        }
        request.setAttribute("MEDICATION_DATA", thuoc);
        return CREATE_MEDICATION_PAGE;
    }
    
    private String listAndSearchMedications(HttpServletRequest request) {
        String keyword = request.getParameter("keyword"); // Lấy từ khóa tìm kiếm
        List<ThuocDTO> danhSachThuoc;
        
        try {
            if (keyword != null && !keyword.trim().isEmpty()) {
                // Nếu có từ khóa -> gọi hàm tìm kiếm
                danhSachThuoc = thuocService.searchMedicationsByName(keyword);
                request.setAttribute("searchKeyword", keyword); // Gửi lại từ khóa để hiển thị
            } else {
                // Nếu không có từ khóa -> gọi hàm lấy tất cả
                danhSachThuoc = thuocService.getAllMedications();
            }
            request.setAttribute("danhSachThuoc", danhSachThuoc);
        } catch (Exception e) {
            log("Lỗi khi lấy danh sách thuốc: ", e);
            request.setAttribute("ERROR_MESSAGE", "Không thể tải danh sách thuốc: " + e.getMessage());
        }
        return THUOC_LIST_PAGE;
    }

    //tạo 
    private String createService(HttpServletRequest request) {
        // Tạo một DTO để chứa dữ liệu từ form
        DichVuDTO newServiceDTO = new DichVuDTO();
        
        try {

            // 1. LẤY DỮ LIỆU THÔ TỪ REQUEST
            String tenDichVu = request.getParameter("tenDichVu");
            String moTa = request.getParameter("moTa");
            String donGiaStr = request.getParameter("donGia");

            // 2. ĐÓNG GÓI DỮ LIỆU VÀO DTO (VALIDATE & CONVERT)
            // Gán các giá trị đã lấy vào DTO để có thể gửi lại cho form nếu có lỗi
            newServiceDTO.setTenDichVu(tenDichVu);
            newServiceDTO.setMoTa(moTa);
            newServiceDTO.setTrangThai("SU_DUNG");
            // Chuyển đổi đơn giá từ String sang BigDecimal
            if (donGiaStr != null && !donGiaStr.isEmpty()) {
                newServiceDTO.setDonGia(new BigDecimal(donGiaStr));
            }

            // 3. GỌI TẦNG SERVICE ĐỂ XỬ LÝ NGHIỆP VỤ
            DichVuDTO result = dichVuService.createService(newServiceDTO);
            String url = "/MainController?action=listAndSearchServices&keyword=" + result.getId();
            // 4. XỬ LÝ KẾT QUẢ THÀNH CÔNG
            request.getSession().setAttribute("SUCCESS_MESSAGE", "Đã tạo dịch vụ '" + result.getTenDichVu() + "' thành công!");
            // Có thể chuyển hướng về trang danh sách dịch vụ
            return "redirect:" + url;
            
        } catch (ValidationException e) {
            // Bắt lỗi nghiệp vụ (ví dụ: tên trùng) từ Service
            log("Lỗi nghiệp vụ khi tạo dịch vụ: " + e.getMessage());
            request.setAttribute("ERROR_MESSAGE", e.getMessage());
            request.setAttribute("SERVICE_DATA", newServiceDTO); // Gửi lại dữ liệu đã nhập
            return CREATE_SERVICE_PAGE; // Quay lại trang tạo để sửa

        } catch (NumberFormatException e) {
            // Bắt lỗi định dạng số (ví dụ: nhập chữ vào ô đơn giá)
            log("Lỗi định dạng đơn giá: " + e.getMessage());
            request.setAttribute("ERROR_MESSAGE", "Đơn giá phải là một con số hợp lệ.");
            request.setAttribute("SERVICE_DATA", newServiceDTO); // Gửi lại dữ liệu đã nhập
            return CREATE_SERVICE_PAGE;
            
        } catch (Exception e) {
            // Bắt các lỗi hệ thống không lường trước được
            log("Lỗi hệ thống khi tạo dịch vụ: " + e.getMessage(), e);
            request.setAttribute("ERROR_MESSAGE", "Đã xảy ra lỗi hệ thống nghiêm trọng.");
            return "error.jsp";
        }
    }
    
    private String createMedication(HttpServletRequest request) {
        ThuocDTO newThuocDTO = new ThuocDTO();
        
        try {
            // 1. LẤY DỮ LIỆU THÔ TỪ REQUEST
            String tenThuoc = request.getParameter("tenThuoc");
            String hoatChat = request.getParameter("hoatChat");
            String donViTinh = request.getParameter("donViTinh");
            String donGiaStr = request.getParameter("donGia");
            String soLuongStr = request.getParameter("soLuongTonKho");

            // 2. ĐÓNG GÓI DỮ LIỆU VÀO DTO (VALIDATE & CONVERT)
            // Gán các giá trị đã lấy vào DTO để có thể gửi lại cho form nếu có lỗi
            newThuocDTO.setTenThuoc(tenThuoc);
            newThuocDTO.setHoatChat(hoatChat);
            newThuocDTO.setDonViTinh(donViTinh);
            newThuocDTO.setTrangThai("SU_DUNG");
            // Chuyển đổi các giá trị số
            if (donGiaStr != null && !donGiaStr.isEmpty()) {
                newThuocDTO.setDonGia(new BigDecimal(donGiaStr));
            }
            if (soLuongStr != null && !soLuongStr.isEmpty()) {
                newThuocDTO.setSoLuongTonKho(Integer.parseInt(soLuongStr));
            }

            // 3. GỌI TẦNG SERVICE ĐỂ XỬ LÝ NGHIỆP VỤ
            // Khởi tạo service (trong thực tế, bạn nên khởi tạo 1 lần ở cấp lớp)
            ThuocDTO result = thuocService.createMedication(newThuocDTO);

            // 4. XỬ LÝ KẾT QUẢ THÀNH CÔNG
            request.setAttribute("SUCCESS_MESSAGE", "Đã tạo thuốc '" + result.getTenThuoc() + "' thành công!");
            // Có thể chuyển hướng về trang danh sách thuốc
            return "redirect:/MainController?action=listMedications&keyword=" + result.getTenThuoc();
            
        } catch (ValidationException e) {
            log("Lỗi nghiệp vụ khi tạo thuốc: " + e.getMessage());
            request.setAttribute("ERROR_MESSAGE", e.getMessage());
            request.setAttribute("MEDICATION_DATA", newThuocDTO); // Gửi lại dữ liệu đã nhập
            return CREATE_MEDICATION_PAGE; // Quay lại trang tạo để sửa

        } catch (NumberFormatException e) {
            log("Lỗi định dạng số: " + e.getMessage());
            request.setAttribute("ERROR_MESSAGE", "Đơn giá hoặc số lượng phải là một con số hợp lệ.");
            request.setAttribute("MEDICATION_DATA", newThuocDTO); // Gửi lại dữ liệu đã nhập
            return CREATE_MEDICATION_PAGE;
            
        } catch (Exception e) {
            // Bắt các lỗi hệ thống không lường trước được
            log("Lỗi hệ thống khi tạo thuốc: " + e.getMessage(), e);
            request.setAttribute("ERROR_MESSAGE", "Đã xảy ra lỗi hệ thống nghiêm trọng.");
            return "error.jsp";
        }
    }
    
    private String updateMedicationInfo(HttpServletRequest request) {
        int thuocId = Integer.parseInt(request.getParameter("id"));
        ThuocDTO dto = new ThuocDTO();
        try {
            // Lấy dữ liệu từ form
            dto.setId(thuocId);
            dto.setTenThuoc(request.getParameter("tenThuoc"));
            dto.setHoatChat(request.getParameter("hoatChat"));
            dto.setDonViTinh(request.getParameter("donViTinh"));
            dto.setDonGia(new BigDecimal(request.getParameter("donGia")));
            
            thuocService.updateMedicationInfo(thuocId, dto);

            // Đặt thông báo thành công vào session để hiển thị sau khi redirect
            request.getSession().setAttribute("SUCCESS_MESSAGE", "Cập nhật thông tin thuốc thành công!");
            return "redirect:/MainController?action=listMedications&keyword=" + dto.getTenThuoc();
            
        } catch (ValidationException e) {
            request.setAttribute("ERROR_MESSAGE", e.getMessage());
            request.setAttribute("MEDICATION_DATA", dto); // Gửi lại dữ liệu đã nhập
            return CREATE_MEDICATION_PAGE;
        } catch (Exception e) {
            request.setAttribute("ERROR_MESSAGE", "Lỗi hệ thống khi cập nhật thuốc.");
            return ERROR_PAGE;
        }
    }

    /**
     * Xử lý cập nhật số lượng tồn kho. chưa làm xong cần thêm ...
     */
    private String updateMedicationStock(HttpServletRequest request) {
        int thuocId = Integer.parseInt(request.getParameter("thuocId"));
        int soLuongThayDoi = Integer.parseInt(request.getParameter("soLuongThayDoi"));
        try {
            thuocService.updateStockQuantity(thuocId, soLuongThayDoi);
            request.getSession().setAttribute("SUCCESS_MESSAGE", "Cập nhật tồn kho thành công!");
            
        } catch (ValidationException e) {
            request.getSession().setAttribute("ERROR_MESSAGE", e.getMessage());
        } catch (Exception e) {
            request.getSession().setAttribute("ERROR_MESSAGE", "Lỗi hệ thống khi cập nhật tồn kho.");
        }
        return "redirect:/MainController?action=listMedications&keyword=" + thuocId; // Luôn quay về trang danh sách
    }
    
    private String deleteMedication(HttpServletRequest request) throws Exception {
        try {
            int thuocId = Integer.parseInt(request.getParameter("id"));

            // Gọi Service để thực hiện nghiệp vụ xóa
            thuocService.deleteMedication(thuocId);

            // Đặt thông báo thành công
            request.getSession().setAttribute("SUCCESS_MESSAGE", "Đã xóa thuốc thành công!");
            
        } catch (ValidationException e) {
            request.getSession().setAttribute("ERROR_MESSAGE", e.getMessage());
            return "redirect:/MainController?action=listMedications";
        } catch (NumberFormatException e) {
            throw new Exception("ID thuốc không hợp lệ.");
        }
        return "redirect:/MainController?action=listMedications";
    }

    // Thêm phương thức này và gọi nó trong doGet
    private String listAndSearchServices(HttpServletRequest request) {
        String keyword = request.getParameter("keyword");
        List<DichVuDTO> danhSachDichVu;
        
        try {
            if (keyword != null && !keyword.trim().isEmpty()) {
                danhSachDichVu = dichVuService.searchServicesByName(keyword);
                request.setAttribute("searchKeyword", keyword);
            } else {
                danhSachDichVu = dichVuService.getAllServices();
            }
            request.setAttribute("danhSachDichVu", danhSachDichVu);
        } catch (Exception e) {
            log("Lỗi khi lấy danh sách dịch vụ: ", e);
            request.setAttribute("ERROR_MESSAGE", "Không thể tải danh sách dịch vụ.");
        }
        return DICH_VU_LIST_PAGE;
    }
    
    private String showUpdateForm(HttpServletRequest request) throws ValidationException, Exception {
        try {
            int serviceId = Integer.parseInt(request.getParameter("id"));
            DichVuDTO service = dichVuService.getServiceById(serviceId);
            if (service == null) {
                throw new ValidationException("Không tìm thấy dịch vụ để cập nhật.");
            }
            request.setAttribute("SERVICE_DATA", service);
            return CREATE_SERVICE_PAGE;
        } catch (NumberFormatException e) {
            throw new Exception("ID dịch vụ không hợp lệ.");
        }
        
    }
    
    private String updateService(HttpServletRequest request) throws ValidationException {
        int serviceId = Integer.parseInt(request.getParameter("id"));
        String redirectUrl = "/MainController?action=listAndSearchServices&keyword=" + serviceId;
        try {
            DichVuDTO dto = new DichVuDTO();
            dto.setId(serviceId);
            dto.setTenDichVu(request.getParameter("tenDichVu"));
            dto.setDonGia(new BigDecimal(request.getParameter("donGia")));
            dto.setMoTa(request.getParameter("moTa"));
            
            dichVuService.updateService(serviceId, dto);
            
            request.getSession()
                    .setAttribute("SUCCESS_MESSAGE", "Cập nhật dịch vụ thành công!");
            // Gửi lại dữ liệu form trong trường hợp có lỗi validation
            request.setAttribute("SERVICE_DATA", dto);
        } catch (ValidationException e) {
            request.getSession().setAttribute("ERROR_MESSAGE", e.getMessage());
        }
        
        return "redirect:" + redirectUrl;
    }
    
    private String deleteService(HttpServletRequest request) {
        try {
            int id = Integer.parseInt(request.getParameter("id"));

            // Gọi Service để thực hiện nghiệp vụ xóa
            dichVuService.deleteService(id);

            // Đặt thông báo thành công vào session
            request.getSession().setAttribute("SUCCESS_MESSAGE", "Đã xóa dịch vụ thành công!");
            
        } catch (ValidationException e) {
            request.getSession().setAttribute("ERROR_MESSAGE", e.getMessage());
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("ERROR_MESSAGE", "ID dịch vụ không hợp lệ.");
        }
        return "redirect:/MainController?action=listAndSearchServices&keyword";
    }

    /**
     * ✨ HÀM MỚI ✨ Phương thức chung để xử lý việc cập nhật trạng thái của một
     * dịch vụ.
     *
     * @param request HttpServletRequest
     * @param newStatus Trạng thái mới cần đặt ("SU_DUNG" hoặc "NGUNG_SU_DUNG")
     * @return URL để redirect về lại trang danh sách.
     */
    private String updateServiceStatus(HttpServletRequest request, String newStatus) {
        String redirectUrl = "/MainController?action=listAndSearchServices"; // URL trang danh sách
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            
            dichVuService.updateServiceStatus(id, newStatus);
            
            String message = "NGUNG_SU_DUNG".equals(newStatus) ? "ngừng sử dụng" : "kích hoạt lại";
            request.getSession().setAttribute("SUCCESS_MESSAGE", "Đã " + message + " dịch vụ thành công!");
            
        } catch (ValidationException e) {
            request.getSession().setAttribute("ERROR_MESSAGE", e.getMessage());
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("ERROR_MESSAGE", "ID dịch vụ không hợp lệ.");
        } catch (Exception e) {
            log("Lỗi khi cập nhật trạng thái dịch vụ: ", e);
            request.getSession().setAttribute("ERROR_MESSAGE", "Lỗi hệ thống: " + e.getMessage());
        }

        // Luôn redirect về trang danh sách
        return "redirect:" + redirectUrl;
    }

    /**
     * Phương thức chung để xử lý việc cập nhật trạng thái của một Thuốc.
     */
    private String updateThuocStatus(HttpServletRequest request, String newStatus) {
        String redirectUrl = "/MainController?action=listMedications"; // URL trang danh sách thuốc
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            thuocService.updateThuocStatus(id, newStatus);
            
            String message = "NGUNG_SU_DUNG".equals(newStatus) ? "ngừng sử dụng" : "kích hoạt lại";
            request.getSession().setAttribute("SUCCESS_MESSAGE", "Đã " + message + " thuốc thành công!");
            
        } catch (ValidationException e) {
            request.getSession().setAttribute("ERROR_MESSAGE", e.getMessage());
        } catch (Exception e) {
            log("Lỗi khi cập nhật trạng thái thuốc: ", e);
            request.getSession().setAttribute("ERROR_MESSAGE", "Lỗi hệ thống: " + e.getMessage());
        }
        return "redirect:" + redirectUrl;
    }
    
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
