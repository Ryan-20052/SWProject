const CART_KEY = 'license_shop_cart_v1';
function loadCart() {
    try { return JSON.parse(localStorage.getItem(CART_KEY) || '[]'); }
    catch (e) { return []; }
}
function saveCart(cart) { localStorage.setItem(CART_KEY, JSON.stringify(cart)); }
function updateCartCountUI() {
    const cart = loadCart();
    const btn = document.getElementById('cartBtn');
    const total = cart.reduce((s, it) => s + (it.qty||1), 0);
    if (btn) btn.innerText = `Giỏ hàng (${total})`;
}
function showToast(msg) {
    let t = document.querySelector('.toast-cart');
    if (!t) { t = document.createElement('div'); t.className = 'toast-cart'; document.body.appendChild(t); }
    t.textContent = msg;
    t.classList.add('show');
    clearTimeout(t._timeout);
    t._timeout = setTimeout(() => t.classList.remove('show'), 1800);
}

/* =========================
   Utility: escape regex
   ========================= */
function escapeRegExp(string) {
    return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

/* =========================
   Header compact handler
   ========================= */
function updateHeaderSpacer() {
    const header = document.getElementById('siteHeader');
    if (!header) return;
    // body padding-top = header height so content không nhảy
    const h = header.getBoundingClientRect().height;
    document.body.style.paddingTop = h + 'px';
}

function handleScrollForHeader() {
    const header = document.getElementById('siteHeader');
    if (!header) return;
    const shouldCompact = window.scrollY > 16;
    header.classList.toggle('compact', shouldCompact);
    // đảm bảo spacer luôn cập nhật nếu header height thay đổi khi compact
    updateHeaderSpacer();
}

/* =========================
   Search / Filter logic (an toàn hơn với highlight)
   ========================= */
function performSearch(rawQuery) {
    const query = (rawQuery || '').trim();
    const qLower = query.toLowerCase();
    const allCards = Array.from(document.querySelectorAll('.grid-all .card, .grid-featured .card'));
    let visibleCount = 0;

    let splitRegex = null;
    let exactMatchRegex = null;
    if (qLower.length > 0) {
        splitRegex = new RegExp('(' + escapeRegExp(query) + ')', 'ig');
        exactMatchRegex = new RegExp('^' + escapeRegExp(query) + '$', 'i');
    }

    allCards.forEach(card => {
        const titleEl = card.querySelector('h3');
        const titleText = (titleEl ? titleEl.textContent : '').trim();
        const match = !query || titleText.toLowerCase().includes(qLower);

        if (match) {
            card.style.display = '';
            if (splitRegex && titleEl) {
                // xây dựng DOM an toàn (không dùng innerHTML với input trực tiếp)
                const parts = titleText.split(splitRegex);
                const frag = document.createDocumentFragment();
                parts.forEach(p => {
                    if (exactMatchRegex && exactMatchRegex.test(p)) {
                        const span = document.createElement('span');
                        span.className = 'highlight';
                        span.textContent = p;
                        frag.appendChild(span);
                    } else {
                        frag.appendChild(document.createTextNode(p));
                    }
                });
                titleEl.innerHTML = '';
                titleEl.appendChild(frag);
            } else if (titleEl) {
                titleEl.textContent = titleText;
            }
            visibleCount++;
        } else {
            card.style.display = 'none';
        }
    });

    const noResultsEl = document.getElementById('noResults');
    if (noResultsEl) {
        noResultsEl.style.display = (visibleCount === 0 ? '' : 'none');
    }

    const clearBtn = document.getElementById('clearSearch');
    if (clearBtn) {
        if (query.length > 0) clearBtn.classList.remove('hidden');
        else clearBtn.classList.add('hidden');
    }
}

/* =========================
   Event binding và khởi tạo
   ========================= */
document.addEventListener('DOMContentLoaded', function() {
    // Setup header spacer & scroll handler
    updateHeaderSpacer();
    handleScrollForHeader();

    // Recalc khi resize (header height có thể thay đổi)
    window.addEventListener('resize', () => {
        // throttle đơn giản
        clearTimeout(window._hdrResizeTimer);
        window._hdrResizeTimer = setTimeout(updateHeaderSpacer, 120);
    });
    window.addEventListener('scroll', () => {
        // throttle nhẹ để tránh spam
        if (window._hdrScrollPending) return;
        window._hdrScrollPending = true;
        requestAnimationFrame(() => {
            handleScrollForHeader();
            window._hdrScrollPending = false;
        });
    });

    // Cập nhật cart count ban đầu
    updateCartCountUI();

    // Kèm event cho các nút Thêm vào giỏ
    document.querySelectorAll('.add-to-cart').forEach(btn => {
        btn.addEventListener('click', function() {
            const pid = this.getAttribute('data-product-id');
            const vid = this.getAttribute('data-variant-id');
            const price = Number(this.getAttribute('data-price') || 0);
            if (!pid) { showToast('Không xác định được sản phẩm.'); return; }

            const cart = loadCart();
            let item = cart.find(i => String(i.productId) === String(pid) && String(i.variantId) === String(vid));
            if (item) { item.qty = (item.qty || 1) + 1; }
            else { cart.push({ productId: pid, variantId: vid, price: price, qty: 1 }); }
            saveCart(cart);
            updateCartCountUI();
            showToast('Đã thêm vào giỏ hàng');
        });
    });

    // Tìm kiếm: input + clear
    const searchInput = document.getElementById('searchInput');
    const clearBtn = document.getElementById('clearSearch');

    if (searchInput) {
        let debounceTimer = null;
        searchInput.addEventListener('input', function(e) {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => performSearch(e.target.value), 150);
        });

        searchInput.addEventListener('keydown', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                performSearch(this.value);
            }
        });
    }

    if (clearBtn) {
        clearBtn.addEventListener('click', function() {
            const input = document.getElementById('searchInput');
            if (input) {
                input.value = '';
                performSearch('');
                input.focus();
            }
        });
    }

    // Khởi chạy một lần (đảm bảo UI đúng khi đã có products)
    performSearch('');
});