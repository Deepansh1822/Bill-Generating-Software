/**
 * Global Utilities for SFP Billing App
 */

const Utils = {
    /**
     * Format a date string or object into a human-readable format
     */
    formatDate: (dateStr) => {
        if (!dateStr) return 'N/A';
        try {
            const date = new Date(dateStr);
            return date.toLocaleDateString('en-IN', {
                year: 'numeric',
                month: 'short',
                day: 'numeric'
            });
        } catch (e) {
            return dateStr;
        }
    },

    /**
     * Escape HTML characters to prevent XSS
     */
    escapeHtml: (unsafe) => {
        if (!unsafe) return "";
        return String(unsafe)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    },

    /**
     * Format a number as Indian Rupee (INR)
     */
    formatCurrency: (amount) => {
        const val = parseFloat(amount || 0);
        return '₹ ' + val.toLocaleString('en-IN', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
    },

    /**
     * Show a simple toast notification
     */
    showToast: (message, type = 'success') => {
        const toast = document.createElement('div');
        toast.style.position = 'fixed';
        toast.style.bottom = '30px';
        toast.style.right = '30px';
        toast.style.padding = '12px 24px';
        toast.style.borderRadius = '12px';
        toast.style.backgroundColor = type === 'success' ? '#10b981' : '#ef4444';
        toast.style.color = 'white';
        toast.style.fontWeight = '600';
        toast.style.boxShadow = '0 10px 15px -3px rgba(0, 0, 0, 0.1)';
        toast.style.zIndex = '9999';
        toast.style.transition = 'all 0.3s ease';
        toast.style.transform = 'translateY(100px)';
        toast.style.opacity = '0';

        toast.innerText = message;
        document.body.appendChild(toast);

        setTimeout(() => {
            toast.style.transform = 'translateY(0)';
            toast.style.opacity = '1';
        }, 100);

        setTimeout(() => {
            toast.style.transform = 'translateY(100px)';
            toast.style.opacity = '0';
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    },

    /**
     * Helper to show a chart fallback (Empty state)
     */
    showChartFallback: (containerId, title, message) => {
        const container = document.getElementById(containerId);
        if (!container) return;

        container.innerHTML = `
            <div class="premium-empty-state" style="padding: 40px 20px;">
                <i class="fa-solid fa-chart-pie" style="font-size: 2rem; width: 60px; height: 60px;"></i>
                <h3 style="font-size: 1.1rem;">${title}</h3>
                <p style="font-size: 0.85rem;">${message}</p>
            </div>
        `;
    }
};

window.Utils = Utils;
