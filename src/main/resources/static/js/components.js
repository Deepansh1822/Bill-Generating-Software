/**
 * SFP Billing - Dynamic Component Loader
 * This script handles loading shared UI components (Sidebar) without Thymeleaf.
 */
class UIComponents {
    static async loadSidebar(activePage = 'dashboard') {
        const sidebarContainer = document.getElementById('sidebar-container');
        if (!sidebarContainer) return;

        // Fetch user context for role-based rendering
        const userContext = await this.getUserContext();
        const role = userContext.userRole || 'CLIENT';

        const sidebarHTML = `
            <aside id="appSidebar">
                <div class="sidebar-content-wrapper">
                    <div class="sidebar-header">
                        <a href="/billing-app/api/Dashboard" class="sidebar-brand">
                            <div class="sidebar-logo">
                                <img src="/images/SFP-Billing-Dark.png" alt="SFP Billing Logo">
                            </div>
                        </a>
                    </div>
                    <ul class="nav-menu">
                        <li class="nav-section-label">Main</li>
                        <li class="nav-item">
                            <a href="/billing-app/api/Dashboard" class="nav-link ${activePage === 'dashboard' ? 'active' : ''}">
                                <i class="fa-solid fa-house nav-icon"></i>
                                <span>Dashboard</span>
                            </a>
                        </li>
                        <li class="nav-item">
                            <button class="nav-dropdown-toggle ${activePage === 'stocks' || activePage === 'categories' ? 'open' : ''}" id="inv-toggle">
                                <i class="fa-solid fa-boxes-stacked nav-icon"></i>
                                <span>Inventory</span>
                                <i class="fa-solid fa-chevron-down nav-dropdown-arrow"></i>
                            </button>
                            <ul class="nav-submenu ${activePage === 'stocks' || activePage === 'categories' ? 'open' : ''}" id="inv-submenu">
                                <li><a href="/billing-app/api/ManageCategories" class="nav-sublink ${activePage === 'categories' ? 'active' : ''}">Categories</a></li>
                                <li><a href="/billing-app/api/ManageStocks" class="nav-sublink ${activePage === 'stocks' ? 'active' : ''}">Products & Services</a></li>
                            </ul>
                        </li>
                        ${role === 'CLIENT' ? `
                        <li class="nav-section-label">Billing Services</li>
                        <li class="nav-item">
                            <button class="nav-dropdown-toggle ${activePage === 'generate-bill' || activePage === 'generate-service-bill' ? 'open' : ''}" id="invoice-toggle">
                                <i class="fa-solid fa-file-invoice nav-icon"></i>
                                <span>Generate Invoice</span>
                                <i class="fa-solid fa-chevron-down nav-dropdown-arrow"></i>
                            </button>
                            <ul class="nav-submenu ${activePage === 'generate-bill' || activePage === 'generate-service-bill' ? 'open' : ''}" id="invoice-submenu">
                                <li><a href="/billing-app/api/GenerateProductBill" class="nav-sublink ${activePage === 'generate-bill' ? 'active' : ''}">Product-Based Invoice</a></li>
                                <li><a href="/billing-app/api/GenerateServiceBill" class="nav-sublink ${activePage === 'generate-service-bill' ? 'active' : ''}">Service-Based Invoice</a></li>
                            </ul>
                        </li>
                        <li class="nav-item">
                            <a href="/billing-app/api/GenerateEWayBill" class="nav-link ${activePage === 'eway-bill' ? 'active' : ''}">
                                <i class="fa-solid fa-truck-fast nav-icon"></i>
                                <span>E-Way Bill</span>
                            </a>
                        </li>
                        ` : ''}
                        ${role === 'ADMIN' ? `
                        <li class="nav-section-label">System Management</li>
                        <li class="nav-item">
                            <a href="/billing-app/api/ManageAccessRequests" class="nav-link ${activePage === 'access-requests' ? 'active' : ''}">
                                <i class="fa-solid fa-user-check nav-icon"></i>
                                <span>Requests</span>
                                <span class="nav-badge" id="sidebar-pending-badge" style="display: none;">0</span>
                            </a>
                        </li>
                        <li class="nav-item">
                            <a href="/billing-app/api/ManageClients" class="nav-link ${activePage === 'clients' ? 'active' : ''}">
                                <i class="fa-solid fa-users nav-icon"></i>
                                <span>Owners</span>
                            </a>
                        </li>
                        ` : ''}
                        <li class="nav-section-label">Analytics & Reports</li>
                        <li class="nav-item">
                            <a href="/billing-app/api/Reports" class="nav-link ${activePage === 'reports' ? 'active' : ''}">
                                <i class="fa-solid fa-chart-column nav-icon"></i>
                                <span>Invoice Reports</span>
                            </a>
                        </li>
                        <li class="nav-item">
                            <a href="/billing-app/api/EWayBillLogs" class="nav-link ${activePage === 'eway-bill-logs' ? 'active' : ''}">
                                <i class="fa-solid fa-truck-ramp-box nav-icon"></i>
                                <span>E-Way Reports</span>
                            </a>
                        </li>
                    </ul>
                </div>
                <div class="sidebar-footer">
                    <a href="/billing-app/api/Profile" class="footer-item ${activePage === 'profile' ? 'active' : ''}">
                        <i class="fa-solid fa-user-gear"></i>
                        <span>My Profile</span>
                    </a>
                    <a href="/billing-app/api/HelpSupport" class="footer-item ${activePage === 'help' ? 'active' : ''}">
                        <i class="fa-solid fa-circle-question"></i>
                        <span>Help Support</span>
                    </a>
                </div>
            </aside>
        `;

        sidebarContainer.innerHTML = sidebarHTML;
        this.initSidebarEvents();
        if (role === 'ADMIN') this.startBadgeHeartbeat();
    }

    static async getUserContext() {
        try {
            const res = await fetch('/api/user/context');
            return await res.json();
        } catch (e) {
            return { userRole: 'CLIENT' };
        }
    }

    static initSidebarEvents() {
        const toggles = ['inv-toggle', 'invoice-toggle'];
        toggles.forEach(id => {
            const el = document.getElementById(id);
            if (el) {
                el.addEventListener('click', () => {
                    el.classList.toggle('open');
                    const submenu = el.nextElementSibling;
                    if (submenu) submenu.classList.toggle('open');
                });
            }
        });

        // Event delegation for navigation
        document.getElementById('appSidebar').addEventListener('click', (e) => {
            const link = e.target.closest('a.nav-link, a.nav-sublink');
            if (link && link.href && link.href !== '#' && !link.href.endsWith('#')) {
                window.location.href = link.href;
            }
        });
    }

    static startBadgeHeartbeat() {
        const updateBadge = () => {
            fetch('/billing-app/api/admin/pending-requests')
                .then(res => res.json())
                .then(data => {
                    const pending = Array.isArray(data) ? data.filter(u => u.status === 'PENDING').length : 0;
                    const badge = document.getElementById('sidebar-pending-badge');
                    if (badge) {
                        badge.innerText = pending;
                        badge.style.display = pending > 0 ? 'block' : 'none';
                    }
                }).catch(() => {});
        };
        updateBadge();
        setInterval(updateBadge, 30000);
    }
}
