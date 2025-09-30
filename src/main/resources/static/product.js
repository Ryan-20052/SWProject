let lastScrollY = window.scrollY;
const header = document.getElementById("siteHeader");

window.addEventListener("scroll", () => {
    if (window.scrollY > lastScrollY) {
        // Kéo xuống -> thu gọn
        header.classList.add("compact");
    } else {
        // Kéo lên -> mở rộng lại
        header.classList.remove("compact");
    }
    lastScrollY = window.scrollY;
});
