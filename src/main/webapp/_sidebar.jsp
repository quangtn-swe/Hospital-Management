<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<head>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.13.1/font/bootstrap-icons.min.css">
</head>
<nav class="sidebar-nav" id="sidebar-nav">
    <div class="sidebar-header">

        <c:if test="${sessionScope.USER.vaiTro == 'BAC_SI'}">
            <a href="<c:url value='/MainController?action=listAllEncounters'/>" class="sidebar-logo">
                <img src="images/logo.png" alt="alt"/>
                <span>Hospital MIS</span>
            </a>
        </c:if>

        <c:if test="${sessionScope.USER.vaiTro  == 'LE_TAN'}">
            <a href="<c:url value='/MainController?action=listInvoices'/>" class="sidebar-logo">
                <img src="images/logo.png" alt="alt"/>
                <span>Hospital MIS</span>
            </a>
        </c:if>

        <button id="sidebar-toggle-btn" class="sidebar-toggle-btn" title="Thu gọn/Mở rộng">
            <i class="fas fa-bars"></i>
        </button>
    </div>

    <ul class="sidebar-menu">

        <%-- ===== CHỨC NĂNG CỦA BÁC SĨ (TẠO MỚI) ===== --%>
        <c:if test="${sessionScope.USER.vaiTro  == 'BAC_SI'}">
            <li>
                <a href="<c:url value='/MainController?action=showCreateEncounterForm'/>" class="nav-link" data-link="showCreateEncounterForm">
                    <i class="fas fa-plus-circle"></i>
                    <span class="nav-text">Tạo Phiếu Khám</span>
                </a>
            </li>

        </c:if>

        <li class="menu-divider"><span class="nav-text">Quản lý</span></li>

        <%-- ===== CHỨC NĂNG CỦA BÁC SĨ (QUẢN LÝ) ===== --%>
        <c:if test="${sessionScope.USER.vaiTro  == 'BAC_SI'}">
            <li>
                <a href="<c:url value='/MainController?action=listAllEncounters'/>" class="nav-link" data-link="listAllEncounters">
                    <i class="fas fa-notes-medical"></i>
                    <span class="nav-text">DS. Phiếu Khám</span>
                </a>
            </li>
            <li>
                <a href="<c:url value='/MainController?action=listAll'/>" class="nav-link" data-link="listAll">
                    <i class="bi bi-file-medical"></i>
                    <span class="nav-text">DS. Đơn Thuốc</span>
                </a>
            </li>
            <li>
                <a href="<c:url value='/MainController?action=listLichHenNurse'/>" class="nav-link" data-link="listLichHenNurse">
                    <i class="fas fa-calendar-alt"></i>
                    <span class="nav-text">Lịch Hẹn</span>
                </a>
            </li>
            <li>
                <a href="<c:url value='/MainController?action=listMedications'/>" class="nav-link" data-link="listMedications">
                    <i class="fas fa-pills"></i>
                    <span class="nav-text">Thuốc</span>
                </a>
            </li>
            <li>
                <a href="<c:url value='/MainController?action=listAndSearchServices'/>" class="nav-link" data-link="listAndSearchServices">
                    <i class="fas fa-vials"></i>
                    <span class="nav-text">Dịch Vụ</span>
                </a>
            </li>    

        </c:if>

        <%-- ===== CHỨC NĂNG CỦA LỄ TÂN (QUẢN LÝ) ===== --%>
        <c:if test="${sessionScope.USER.vaiTro  == 'LE_TAN'}">
            <li>
                <a href="<c:url value='/MainController?action=listLichHenNurse'/>" class="nav-link" data-link="listLichHenNurse">
                    <i class="fas fa-calendar-alt"></i>
                    <span class="nav-text">Lịch Hẹn</span>
                </a>
            </li>
            <li>
                <a href="<c:url value='/MainController?action=listInvoices'/>" class="nav-link" data-link="listInvoices">
                    <i class="fas fa-file-invoice-dollar"></i>
                    <span class="nav-text">Hóa đơn</span>
                </a>
            </li>
            <li>
                <a href="<c:url value='/MainController?action=listRooms'/>" class="nav-link" data-link="listRooms">
                    <i class="fas fa-hospital"></i>
                    <span class="nav-text">Phòng bệnh</span>
                </a>
            </li>
            <li>
                <a href="<c:url value='/MainController?action=listBeds'/>" class="nav-link" data-link="listBeds">
                    <i class="fas fa-bed"></i>
                    <span class="nav-text">Giường bệnh</span>
                </a>
            </li>

        </c:if>

        <%-- ===== CHỨC NĂNG CHUNG (CHO CẢ HAI) ===== --%>
        <li>
            <a href="<c:url value='/MainController?action=viewMyNotifications'/>" class="nav-link" data-link="viewMyNotifications">
                <i class="fas fa-bell"></i>
                <span class="nav-text">Thông báo</span>
            </a>
        </li>
    </ul> <div class="sidebar-footer">
        <a href="<c:url value='/MainController?action=logout'/>" class="nav-link logout-link">
            <i class="fas fa-sign-out-alt"></i>
            <span class="nav-text">Đăng xuất</span>
        </a>
    </div>
</nav>