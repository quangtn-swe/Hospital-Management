package filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

/**
 * Filter này đảm bảo mọi request và response đều được xử lý bằng bộ mã UTF-8.
 * Nó giải quyết triệt để vấn đề lỗi font tiếng Việt.
 */
@WebFilter(filterName = "CharacterEncodingFilter", urlPatterns = {"/*"})
public class CharacterEncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // 1. Ép buộc request phải được đọc bằng UTF-8 (để đọc form)
        request.setCharacterEncoding("UTF-8");

        // 2. Ép buộc response phải được gửi đi bằng UTF-8 (để hiển thị)
        // ✨ PHẢI ĐẶT TRƯỚC chain.doFilter ✨
        response.setCharacterEncoding("UTF-8");

        // 3. Tiếp tục chuỗi xử lý (đến Servlet)
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Không cần làm gì ở đây
    }

    @Override
    public void destroy() {
        // Không cần làm gì ở đây
    }
}