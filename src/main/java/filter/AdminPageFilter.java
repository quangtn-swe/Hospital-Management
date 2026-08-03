package filter; // Hoặc package của bạn

import java.io.IOException;
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
import model.dto.TaiKhoanDTO; // Đảm bảo import DTO của bạn

/**
 * Filter này bảo vệ các trang JSP trong thư mục /admin/ Chỉ cho phép ADMIN đã
 * đăng nhập truy cập.
 */
// QUAN TRỌNG: urlPatterns trỏ đến thư mục cần bảo vệ
@WebFilter(filterName = "AdminPageFilter", urlPatterns = {"/admin/*"})
public class AdminPageFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Không cần init gì đặc biệt
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false); // Lấy session, không tạo mới

        TaiKhoanDTO user = null;
        if (session != null) {
            user = (TaiKhoanDTO) session.getAttribute("USER");
        }

        // Kiểm tra điều kiện:
        // 1. Chưa đăng nhập (user == null)
        // 2. Đã đăng nhập NHƯNG vai trò KHÔNG PHẢI "QUAN_TRI"
        // (Sử dụng chính xác chuỗi vai trò "QUAN_TRI" như trong AuthFilter)
        if (user == null || !"QUAN_TRI".equals(user.getVaiTro())) {

            // Ghi log (tùy chọn nhưng nên làm)
            String username = (user == null) ? "Guest (Chưa đăng nhập)" : user.getTenDangNhap();
            System.out.println("CẢNH BÁO TRUY CẬP TRÁI PHÉP (AdminPageFilter): User '" + username
                    + "' đã cố gắng truy cập: " + httpRequest.getRequestURI());

            if (user == null) {
                // Nếu chưa đăng nhập, đá về trang login
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp");
            } else {
                // Nếu đăng nhập rồi nhưng sai vai trò, đá về trang lỗi
                request.setAttribute("errorMessage", "Bạn không có quyền truy cập trang quản trị.");
                httpRequest.getRequestDispatcher("/error.jsp").forward(httpRequest, httpResponse);
            }
            return; // Dừng xử lý yêu cầu
        }

        // Nếu user LÀ admin (vượt qua vòng if ở trên)
        // Cho phép đi tiếp
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Không cần dọn dẹp
    }
}
