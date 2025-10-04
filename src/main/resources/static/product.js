let lastScrollY = window.scrollY;
const header = document.getElementById("siteHeader");

// ----------------- Thu gọn header khi scroll -----------------
window.addEventListener("scroll", () => {
    if (window.scrollY > lastScrollY) {
        header.classList.add("compact");   // khi kéo xuống
    } else {
        header.classList.remove("compact"); // khi kéo lên
    }
    lastScrollY = window.scrollY;
});

// ----------------- CSRF helper -----------------
function getCsrfHeaders() {
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;
    return (token && header) ? { [header]: token } : {};
}

// ----------------- Lấy variant đang chọn -----------------
function getSelectedVariantId() {
    const checked = document.querySelector('input[name="variantId"]:checked');
    return checked ? Number(checked.value) : null;
}

// ----------------- Hàm gọi API mua ngay -----------------
async function buyNow() {
    const btn = document.querySelector('.buy');
    const variantId = getSelectedVariantId();

    if (!variantId) {
        alert('Vui lòng chọn gói/phiên bản.');
        return;
    }

    // 🔥 Lấy customerId từ session (inject ở product.html)
    if (typeof sessionCustomerId === "undefined" || !sessionCustomerId) {
        alert("Bạn cần đăng nhập trước khi thanh toán!");
        window.location.href = "/login.html";
        return;
    }

    // Payload gửi về API
    const payload = {
        customerId: Number(sessionCustomerId),
        items: [{ variantId, quantity: 1 }],
        orderInfo: 'Mua sản phẩm',
        orderType: 'other',
        locale: 'vn'
    };

    // Headers (kèm CSRF nếu có)
    const headers = {
        'Content-Type': 'application/json',
        ...getCsrfHeaders()
    };

    btn.disabled = true;
    btn.textContent = 'Đang chuyển đến VNPAY...';

    try {
        const res = await fetch('/api/payment/create', {
            method: 'POST',
            headers,
            body: JSON.stringify(payload),
            credentials: 'same-origin' // giữ session đăng nhập
        });

        if (!res.ok) {
            const text = await res.text();
            throw new Error(text || `HTTP ${res.status}`);
        }

        const data = await res.json();
        if (!data || !data.paymentUrl) {
            throw new Error('Không nhận được paymentUrl từ server.');
        }

        // 👉 Redirect trực tiếp sang VNPAY
        window.location.href = data.paymentUrl;

    } catch (err) {
        console.error("Thanh toán lỗi:", err);
        alert('Không tạo được thanh toán: ' + (err.message || 'Unknown error'));
        btn.disabled = false;
        btn.textContent = 'Mua ngay';
    }
}

// ----------------- Khởi tạo sau khi DOM load -----------------
document.addEventListener('DOMContentLoaded', function () {
    const buyBtn = document.querySelector('.buy');
    if (buyBtn) buyBtn.addEventListener('click', buyNow);
});