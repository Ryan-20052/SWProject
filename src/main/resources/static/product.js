document.addEventListener('DOMContentLoaded', function () {
    // ----------------- Thu gọn header khi scroll -----------------
    let lastScrollY = window.scrollY;
    const header = document.getElementById("siteHeader");
    window.addEventListener("scroll", () => {
        if (window.scrollY > lastScrollY) {
            header?.classList.add("compact"); // khi kéo xuống
        } else {
            header?.classList.remove("compact"); // khi kéo lên
        }
        lastScrollY = window.scrollY;
    });

    // ----------------- CSRF helper -----------------
    function getCsrfHeaders() {
        const token = document.querySelector('meta[name="_csrf"]')?.content;
        const header = document.querySelector('meta[name="_csrf_header"]')?.content;
        return (token && header) ? { [header]: token } : {};
    }

    // ----------------- Biến chung -----------------
    const buyNowBtn = document.getElementById('buyNowBtn');
    const addToCartBtn = document.getElementById('addToCartBtn');
    const applyBtn = document.getElementById('applyVoucherBtn');
    const voucherInput = document.getElementById('voucherCode');
    const voucherMessage = document.getElementById('voucherMessage');
    const variantRadios = document.querySelectorAll("input[name='variantId']");
    const variantIdInput = document.getElementById('variantIdInput');
    const amountInput = document.getElementById('amountInput');

    let discountedTotal = null; // 💰 Tổng sau giảm
    let originalPrice = null; // 💰 Giá gốc của variant được chọn

    // ----------------- Đồng bộ variant được chọn -----------------
    function syncVariantFromRadio() {
        const checked = Array.from(variantRadios).find(r => r.checked);
        if (checked) {
            variantIdInput.value = checked.value;
            buyNowBtn.disabled = false;
            addToCartBtn.disabled = false;

            // ✅ Cập nhật giá hiển thị + lưu giá gốc
            const priceEl = document.querySelector('.price');
            const priceText = checked.nextElementSibling.querySelector('span:nth-child(2)')?.innerText;
            if (priceEl && priceText) {
                priceEl.innerText = priceText;
                originalPrice = parseFloat(priceText.replace(/[^\d]/g, '')) || 0; // Lưu giá gốc thật
            }
        } else if (!variantIdInput.value) {
            buyNowBtn.disabled = true;
            addToCartBtn.disabled = true;
        }
    }
    variantRadios.forEach(radio => radio.addEventListener('change', syncVariantFromRadio));
    syncVariantFromRadio();

    // ----------------- Áp dụng voucher -----------------
    applyBtn?.addEventListener('click', async function () {
        const code = voucherInput.value.trim();
        if (!code) {
            voucherMessage.textContent = '⚠️ Vui lòng nhập mã voucher!';
            voucherMessage.style.color = 'red';
            return;
        }

        // Lấy giá gốc từ text và nhân với số lượng
        const quantity = parseInt(amountInput.value || '1', 10);
        const total = (originalPrice || 0) * quantity; // ✅ Dùng giá gốc, không dùng .price đã giảm

        if (!total || total <= 0) {
            voucherMessage.textContent = '❌ Không thể áp dụng vì không xác định được giá sản phẩm!';
            voucherMessage.style.color = 'red';
            return;
        }

        try {
            const res = await fetch(`/api/vouchers/apply?code=${encodeURIComponent(code)}&total=${total}`);
            if (!res.ok) throw new Error('Không thể áp dụng voucher');

            const data = await res.json();
            discountedTotal = data.discountedTotal;

            voucherMessage.textContent =
                `✅ Giảm ${data.discountAmount.toLocaleString()}đ! Tổng mới: ${data.discountedTotal.toLocaleString()}đ`;
            document.querySelector('.price').innerText = data.discountedTotal.toLocaleString() + ' đ';
            // 🔧 Cập nhật giá hiển thị trên giao diện
            const priceEl = document.querySelector('.price');
            if (priceEl) {
                priceEl.innerText = data.discountedTotal.toLocaleString() + ' đ';
            }
        } catch (err) {
            console.error('❌ Voucher error:', err);
            voucherMessage.textContent = '❌ Mã không hợp lệ hoặc đã hết hạn!';
            voucherMessage.style.color = 'red';
            discountedTotal = null;
        }
    });

    // ----------------- Mua ngay (VNPAY) -----------------
    buyNowBtn?.addEventListener('click', async function () {
        if (!sessionCustomerId || sessionCustomerId === 'null') {
            alert('Vui lòng đăng nhập để mua sản phẩm!');
            window.location.href = '/login.html';
            return;
        }

        const variantId = variantIdInput.value;
        if (!variantId) {
            alert('Vui lòng chọn gói sản phẩm!');
            return;
        }

        const quantity = parseInt(amountInput.value || '1', 10);
        const voucherCode = document.getElementById('voucherCode')?.value?.trim() || null;

        const body = {
            customerId: parseInt(sessionCustomerId),
            items: [{ variantId: parseInt(variantId), quantity }],
            orderInfo: "Thanh toán " + productName,
            voucherCode: voucherCode,
            totalAfterDiscount: discountedTotal
        };

        const headers = { 'Content-Type': 'application/json', ...getCsrfHeaders() };

        try {
            const res = await fetch('/api/payment/create', {
                method: 'POST',
                headers,
                body: JSON.stringify(body),
                credentials: 'same-origin'
            });

            if (!res.ok) {
                const text = await res.text();
                console.error('❌ Payment create failed:', res.status, text);
                alert('Không thể tạo thanh toán. Vui lòng thử lại.');
                return;
            }

            const data = await res.json();
            if (data && data.paymentUrl) {
                window.location.href = data.paymentUrl;
            } else {
                alert('Không có URL thanh toán trả về!');
                console.error('⚠️ Response thiếu paymentUrl:', data);
            }
        } catch (err) {
            console.error('⚠️ Error creating payment:', err);
            alert('Có lỗi xảy ra khi tạo thanh toán!');
        }
    });

    // ----------------- Ghi log để kiểm tra -----------------
    console.log("✅ JS loaded: product.js ready");
});
