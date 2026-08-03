<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
    <head>
        <title>Đặt lịch hẹn</title>

        <%-- SỬA 1: Đảo thứ tự tải CSS --%>
        <%-- Tải file cũ/chung trước --%>
        <link rel="stylesheet" href="<c:url value='/css/index.css'/>">

        <%-- Tải file mới (LichHenQuang) SAU CÙNG để nó ghi đè --%>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/LichHenQuang.css">
    </head>
    <body>

        <jsp:include page="/WEB-INF/headerDat.jsp" />

        <%-- SỬA 2: Đổi "form-container" thành "page-container" --%>
        <div class="page-container">
            <h2>Đặt lịch hẹn mới</h2>

            <%-- Thông báo (Giữ nguyên) --%>
            <c:if test="${not empty ERROR_MESSAGE}">
                <p class="error"><c:out value="${ERROR_MESSAGE}"/></p>
            </c:if>
            <c:if test="${not empty LOAD_FORM_ERROR}">
                <p class="load-error"><c:out value="${LOAD_FORM_ERROR}"/></p>
            </c:if>

            <form action="${pageContext.request.contextPath}/MainController" method="POST">
                <input type="hidden" name="action" value="bookAppointment">

                <%-- Các .form-group này đã đúng và sẽ hiển thị đẹp --%>
                <div class="form-group">
                    <label for="khoaId">Chọn Khoa:</label>
                    <select name="khoaId" id="khoaId" required onchange="loadBacSi(this)">
                        <option value="">-- Vui lòng chọn khoa --</option>
                        <c:forEach var="khoa" items="${khoaList}">
                            <option value="${khoa.id}">
                                <c:out value="${khoa.tenKhoa}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="form-group">
                    <label for="bacSiId">Chọn Bác sĩ:</label>
                    <select name="bacSiId" id="bacSiId" required>
                        <option value="">-- Vui lòng chọn khoa trước --</option>
                    </select>
                    <small id="bacSiLoading" style="color: #5d5dff; display: none;">Đang tải bác sĩ...</small>
                </div>

                <div class="form-group">
                    <label for="thoiGianHen">Thời gian hẹn:</label>
                    <input type="datetime-local" id="thoiGianHen" name="thoiGianHen" required 
                           value="${LICHHEN_DATA.thoiGianHen.toLocalDateTime()}">
                </div>

                <div class="form-group">
                    <label for="lyDoKham">Lý do khám:</label>
                    <textarea id="lyDoKham" name="lyDoKham" rows="4" required><c:out value="${LICHHEN_DATA.lyDoKham}"/></textarea>
                </div>

                <%-- SỬA 3: Thêm class "btn btn-primary" cho nút --%>
                <button type="submit" class="btn btn-primary">Xác nhận Đặt lịch</button>
            </form>

            <%-- SỬA 4: Thêm class "btn-link" cho link --%>
            <p style="margin-top: 15px;">
                <a href="MainController?action=myAppointments" class="btn-link">Xem lịch hẹn của tôi</a>
            </p>
        </div>

        <%-- Tải JavaScript (Giữ nguyên) --%>
        <script src="<c:url value='/js/index.js'/>"></script>
        <script src="${pageContext.request.contextPath}/js/LichHenQuang.js"></script>
        <%-- Footer --%>
        <footer class="main-footer">
            <div class="container">
                <jsp:include page="/WEB-INF/footer.jsp" /> 
            </div>
        </footer>

        <%-- THÊM THƯ VIỆN SWIPER.JS (Bắt buộc) --%>
        <script src="https://cdn.jsdelivr.net/npm/swiper@11/swiper-bundle.min.js"></script>

        <%-- LINK TỚI FILE JS --%>
        <script src="<c:url value='/js/index.js'/>"></script>

    </body>
</html>