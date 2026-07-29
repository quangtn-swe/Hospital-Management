<%--
    Document   : changePassword.jsp (Đã sửa lỗi hiển thị)
    Created on : Oct 29, 2025
    Author     : ADMIN
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Đổi mật khẩu</title>
safsfafa

        
        <%-- Nhúng CSS/Font chung --%>
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    </head>
    <body class="login-page-body"> <%-- Tái sử dụng class nền và căn giữa --%>

        <%-- **THÊM CLASS 'single-form' VÀ SỬA STYLE WIDTH** --%>
        <div class.login-container single-form" style="width: 480px;">

            <%-- Nút Home (Đã sửa logic 3 hướng) --%>
            <c:choose>
                <c:when test="${sessionScope.ROLE == 'QUAN_TRI'}">
                    <a href="${pageContext.request.contextPath}/admin/dashboard.jsp" class="home-link" title="Quay về Bảng điều khiển">
                        <i class="fas fa-home"></i>
                    </a>
                </c:when>
                <c:when test="${sessionScope.ROLE == 'BAC_SI' || sessionScope.ROLE == 'LE_TAN'}">
                    <a href="${pageContext.request.contextPath}/staff/dashboard.jsp" class="home-link" title="Quay về Bảng điều khiển">
                        <i class="fas fa-home"></i>
                    </a>
                </c:when>
                <c:when test="${sessionScope.ROLE == 'BENH_NHAN'}">
                    <a href="${pageContext.request.contextPath}/index.jsp" class="home-link" title="Quay về Trang chủ">
                        <i class="fas fa-home"></i>
                    </a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/index.jsp" class="home-link" title="Quay về Trang giới thiệu">
                        <i class="fas fa-home"></i>
                    </a>
                </c:otherwise>
            </c:choose>
            

            <%-- **SỬA LẠI: Dùng <h1> thay vì <h2>.section-title** --%>
            <h1>Đổi mật khẩu</h1>

            <%-- Hiển thị thông báo (Đã sửa logic) --%>
            <c:if test="${not empty sessionScope.FORCE_CHANGE_PASS_MSG}">
                <p class="error-message"> ${sessionScope.FORCE_CHANGE_PASS_MSG} </p>
                <c:remove var="FORCE_CHANGE_PASS_MSG" scope="session" /> 
            </c:if>
            <c:if test="${not empty requestScope.ERROR_MESSAGE}">
                <p class="error-message">${requestScope.ERROR_MESSAGE}</p>
            </c:if>
            <c:if test="${not empty sessionScope.SUCCESS_MESSAGE}">
                <p class="success-message"> ${sessionScope.SUCCESS_MESSAGE} </p>
                <c:remove var="SUCCESS_MESSAGE" scope="session" /> 
            </c:if>

            <%-- **SỬA LẠI: Dùng .data-form với style đã reset** --%>
            <form action="${pageContext.request.contextPath}/MainController" method="post" class="data-form">

                <input type="hidden" name="action" value="changePassword" />

                <div class="form-group">
                    <label for="oldPassword">Mật khẩu cũ:</label>
                    <input type="password" id="oldPassword" name="oldPassword" required="required">
                </div>

                <div class="form-group">
                    <label for="newPassword">Mật khẩu mới:</label>
                    <input type="password" id="newPassword" name="newPassword" required="required">
                </div>

                <div class="form-group">
                    <label for="confirmPassword">Xác nhận mật khẩu mới:</label>
                    <input type="password" id="confirmPassword" name="confirmPassword" required="required">
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn-submit">
                        <i class="fas fa-save"></i> Cập nhật
                    </button>

                    <%-- Nút Hủy (Đã sửa logic 3 hướng) --%>
                    <c:choose>
                        <c:when test="${sessionScope.ROLE == 'QUAN_TRI'}">
<!--                            <a href="${pageContext.request.contextPath}/admin/dashboard.jsp" class="btn-cancel">Hủy</a>-->
                        </c:when>
                        <c:when test="${sessionScope.ROLE == 'BAC_SI' || sessionScope.ROLE == 'LE_TAN'}">
<!--                            <a href="${pageContext.request.contextPath}/staff/dashboard.jsp" class="btn-cancel">Hủy</a>-->
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/home.jsp" class="btn-cancel">Hủy</a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </form>

        </div>

    </body>
</html>