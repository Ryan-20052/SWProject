document.addEventListener('DOMContentLoaded', function () {
    // ----------------- Thu gọn header khi scroll -----------------
    let lastScrollY = window.scrollY;
    const header = document.getElementById("siteHeader");
    window.addEventListener("scroll", () => {
        if (window.scrollY > lastScrollY) {
            header?.classList.add("compact");
        } else {
            header?.classList.remove("compact");
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
    const applyBtn = document.getElementById('applyVoucherBtn');
    const removeVoucherBtn = document.getElementById('removeVoucherBtn');
    const voucherInput = document.getElementById('voucherCode');
    const voucherMessage = document.getElementById('voucherMessage');
    const voucherDetails = document.getElementById('voucherDetails');
    const variantRadios = document.querySelectorAll("input[name='variantId']");
    const variantIdInput = document.getElementById('variantIdInput');
    const amountInput = document.getElementById('amountInput');
    const originalPriceDisplay = document.getElementById('originalPriceDisplay');
    const finalPriceDisplay = document.getElementById('finalPriceDisplay');

    let discountedTotal = null;
    let originalPrice = null;
    let currentVoucherCode = null;
    let currentVoucherData = null;

    // ----------------- Đồng bộ variant được chọn -----------------
    function syncVariantFromRadio() {
        const checked = Array.from(variantRadios).find(r => r.checked);
        if (checked) {
            variantIdInput.value = checked.value;
            buyNowBtn.disabled = false;

            // ✅ Cập nhật giá hiển thị + lưu giá gốc
            const priceText = checked.nextElementSibling.querySelector('.variant-price')?.innerText;
            if (priceText) {
                const price = parseFloat(priceText.replace(/[^\d]/g, '')) || 0;
                originalPrice = price;
                updatePriceDisplay(price);
            }
        } else if (!variantIdInput.value) {
            buyNowBtn.disabled = true;
        }
    }

    // 🔥 CẬP NHẬT HIỂN THỊ GIÁ
    function updatePriceDisplay(price, discountedPrice = null) {
        const quantity = parseInt(amountInput.value || '1', 10);
        const totalOriginal = price * quantity;

        if (discountedPrice !== null && discountedPrice < totalOriginal) {
            // Hiển thị giá gốc bị gạch ngang và giá khuyến mãi
            originalPriceDisplay.textContent = totalOriginal.toLocaleString() + ' đ';
            finalPriceDisplay.textContent = discountedPrice.toLocaleString() + ' đ';
            originalPriceDisplay.style.display = 'inline';
        } else {
            // Chỉ hiển thị giá gốc
            originalPriceDisplay.style.display = 'none';
            finalPriceDisplay.textContent = totalOriginal.toLocaleString() + ' đ';
        }
    }

    variantRadios.forEach(radio => radio.addEventListener('change', syncVariantFromRadio));
    syncVariantFromRadio();

    // 🔥 XÓA VOUCHER
    removeVoucherBtn?.addEventListener('click', function() {
        voucherInput.value = '';
        voucherMessage.textContent = '🗑️ Đã xóa voucher';
        voucherMessage.style.color = 'gray';
        voucherDetails.style.display = 'none';
        removeVoucherBtn.style.display = 'none';
        currentVoucherCode = null;
        currentVoucherData = null;

        // Khôi phục giá gốc
        if (originalPrice) {
            updatePriceDisplay(originalPrice);
        }
    });

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
        const total = (originalPrice || 0) * quantity;

        if (!total || total <= 0) {
            voucherMessage.textContent = '❌ Không thể áp dụng vì không xác định được giá sản phẩm!';
            voucherMessage.style.color = 'red';
            return;
        }

        try {
            const res = await fetch(`/api/vouchers/apply?code=${encodeURIComponent(code)}&total=${total}`);

            // 🔥 SỬA LẠI PHẦN XỬ LÝ RESPONSE
            if (!res.ok) {
                let errorMessage = 'Không thể áp dụng voucher';
                try {
                    const errorData = await res.json();
                    errorMessage = errorData.error || errorMessage;
                } catch (e) {
                    // Nếu không parse được JSON, lấy text thô
                    const errorText = await res.text();
                    errorMessage = errorText || errorMessage;
                }
                throw new Error(errorMessage);
            }

            const data = await res.json();

            // 🔥 KIỂM TRA LỖI TỪ SERVER (nếu có field error trong response success)
            if (data.error) {
                voucherMessage.textContent = `❌ ${data.error}`;
                voucherMessage.style.color = 'red';
                voucherDetails.style.display = 'none';
                removeVoucherBtn.style.display = 'none';
                discountedTotal = null;
                currentVoucherCode = null;
                return;
            }

            // 🔥 LƯU THÔNG TIN VOUCHER
            discountedTotal = data.discountedTotal;
            currentVoucherCode = code;
            currentVoucherData = data;

            // 🔥 HIỂN THỊ THÔNG TIN CHI TIẾT VOUCHER
            let message = `✅ Áp dụng thành công!`;
            voucherMessage.textContent = message;
            voucherMessage.style.color = 'green';

            // Hiển thị thông tin chi tiết
            document.getElementById('discountDetails').textContent =
                `💰 Giảm ${data.discountAmount.toLocaleString()}đ (Tổng mới: ${data.discountedTotal.toLocaleString()}đ)`;

            document.getElementById('minOrderInfo').textContent =
                `📦 Đơn tối thiểu: ${(data.minOrderAmount || 0).toLocaleString()}đ`;

            if (data.maxDiscountAmount) {
                document.getElementById('maxDiscountInfo').textContent =
                    `🎯 Giảm tối đa: ${data.maxDiscountAmount.toLocaleString()}đ`;
            } else {
                document.getElementById('maxDiscountInfo').textContent = '';
            }

            voucherDetails.style.display = 'block';
            removeVoucherBtn.style.display = 'inline-block';

            // Hiển thị cảnh báo nếu đạt mức tối thiểu
            if (data.reachedMinimum) {
                voucherMessage.textContent += ` (Đã đạt mức tối thiểu 5,000đ cho VNPay)`;
                voucherMessage.style.color = 'orange';
            }

            // Cập nhật giá hiển thị
            updatePriceDisplay(originalPrice, data.discountedTotal);

        } catch (err) {
            console.error('❌ Voucher error:', err);
            // 🔥 HIỂN THỊ LỖI CỤ THỂ TỪ SERVER
            voucherMessage.textContent = `❌ ${err.message}`;
            voucherMessage.style.color = 'red';
            voucherDetails.style.display = 'none';
            removeVoucherBtn.style.display = 'none';
            discountedTotal = null;
            currentVoucherCode = null;
        }
    });

    // ----------------- Cập nhật giá khi thay đổi số lượng -----------------
    amountInput.addEventListener('keydown', function (e) {
        // Chặn e, E, +, - (type=number vẫn cho nhập)
        if (["e", "E", "+", "-"].includes(e.key)) {
            e.preventDefault();
        }
    });

    amountInput.addEventListener('input', function () {
        // Xóa toàn bộ ký tự không phải số
        this.value = this.value.replace(/\D/g, "");

        let quantity = parseInt(this.value || "1", 10);

        // Nếu trống thì set lại 1
        if (!quantity) quantity = 1;

        // Validate tối đa 10
        if (quantity > 10) {
            alert("Bạn chỉ được mua tối đa 10 sản phẩm 1 lần!");
            quantity = 10;
        }

        this.value = quantity;

        // Cập nhật giá
        if (originalPrice) {
            const total = originalPrice * quantity;

            // Nếu có voucher -> apply lại
            if (currentVoucherCode && total >= (currentVoucherData?.minOrderAmount || 0)) {
                applyBtn.click();
            } else {
                updatePriceDisplay(originalPrice);
            }
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
        const voucherCode = currentVoucherCode; // 🔥 Sử dụng voucher đã được áp dụng

        const body = {
            customerId: parseInt(sessionCustomerId),
            items: [{ variantId: parseInt(variantId), quantity }],
            orderInfo: "Thanh toán " + productName,
            voucherCode: voucherCode,
            totalAfterDiscount: discountedTotal // 🔥 Gửi tổng sau giảm giá
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

                // 🔥 XỬ LÝ LỖI VOUCHER CỤ THỂ
                if (text.includes('Voucher') || text.includes('voucher')) {
                    voucherMessage.textContent = `❌ Lỗi voucher: ${text}`;
                    voucherMessage.style.color = 'red';
                } else {
                    alert('Không thể tạo thanh toán. Vui lòng thử lại.');
                }
                return;
            }

            const data = await res.json();
            if (data && data.paymentUrl) {
                // 🔥 TRỪ LƯỢT SỬ DỤNG VOUCHER SAU KHI THANH TOÁN THÀNH CÔNG
                if (voucherCode) {
                    try {
                        await fetch(`/api/vouchers/${voucherCode}/use`, {
                            method: 'POST',
                            headers: getCsrfHeaders()
                        });
                    } catch (e) {
                        console.warn('⚠️ Không thể trừ lượt voucher:', e);
                    }
                }

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
    console.log("✅ JS loaded: product.js ready with enhanced voucher features");
});