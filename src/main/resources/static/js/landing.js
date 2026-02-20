/* ============================================
   CVPilot Landing — Interactions & Animations
   ============================================ */

(function () {
    'use strict';

    /* ── Carousel Slides Data ── */
    var slides = [
        {
            score: 72,
            theme: 'green',
            gradient: 'url(#scoreGradientGreen)',
            strength: '3 года опыта в React',
            gap: 'Нет опыта CI/CD',
            tip: 'Добавьте ключевые слова'
        },
        {
            score: 45,
            theme: 'orange',
            gradient: 'url(#scoreGradientOrange)',
            strength: 'Опыт управления командой',
            gap: 'Нет сертификации PMP',
            tip: 'Укажите KPI проектов'
        },
        {
            score: 91,
            theme: 'green',
            gradient: 'url(#scoreGradientGreen)',
            strength: 'Владение SQL и Python',
            gap: 'Мало опыта с Tableau',
            tip: 'Отличное совпадение!'
        }
    ];

    var CIRCUMFERENCE = 2 * Math.PI * 140; // ~879.6
    var SLIDE_INTERVAL = 4500;
    var currentSlide = 0;
    var carouselTimer = null;
    var carouselStarted = false;

    /* ── Scroll Reveal (IntersectionObserver) ── */
    var revealElements = document.querySelectorAll('.reveal');
    var revealObserver = new IntersectionObserver(
        function (entries) {
            entries.forEach(function (entry) {
                if (entry.isIntersecting) {
                    entry.target.classList.add('visible');
                    revealObserver.unobserve(entry.target);
                }
            });
        },
        { threshold: 0.15, rootMargin: '0px 0px -40px 0px' }
    );
    revealElements.forEach(function (el) { revealObserver.observe(el); });

    /* ── Nav scroll state ── */
    var nav = document.querySelector('.nav');
    var ticking = false;
    window.addEventListener('scroll', function () {
        if (!ticking) {
            requestAnimationFrame(function () {
                nav.classList.toggle('scrolled', window.scrollY > 40);
                ticking = false;
            });
            ticking = true;
        }
    });

    /* ── Smooth scroll for anchor links ── */
    document.querySelectorAll('a[href^="#"]').forEach(function (link) {
        link.addEventListener('click', function (e) {
            var href = link.getAttribute('href');
            if (href === '#') return;
            var target = document.querySelector(href);
            if (target) {
                e.preventDefault();
                target.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
        });
    });

    /* ── Score Ring Carousel ── */
    var wrapper = document.querySelector('.score-ring-wrapper');
    var ring = document.querySelector('.score-ring-progress');
    var scoreEl = document.querySelector('.score-value');
    var tagStrength = document.querySelector('.tag-strength .tag-text');
    var tagGap = document.querySelector('.tag-gap .tag-text');
    var tagTip = document.querySelector('.tag-tip .tag-text');
    var floatingTags = document.querySelectorAll('.floating-tag');
    var dots = document.querySelectorAll('.carousel-dot');

    function setRingOffset(score) {
        var offset = CIRCUMFERENCE - (CIRCUMFERENCE * score / 100);
        ring.style.strokeDasharray = CIRCUMFERENCE;
        ring.style.strokeDashoffset = offset;
    }

    function showSlide(index, animate) {
        var slide = slides[index];
        var prevScore = parseInt(scoreEl.textContent) || 0;

        // Update ring color
        ring.style.stroke = slide.gradient;
        wrapper.setAttribute('data-theme', slide.theme);

        // Update ring glow filter
        if (slide.theme === 'orange') {
            ring.style.filter = 'drop-shadow(0 0 8px rgba(249, 115, 22, 0.4))';
        } else {
            ring.style.filter = 'drop-shadow(0 0 8px rgba(34, 232, 139, 0.4))';
        }

        // Animate ring arc
        if (animate) {
            setRingOffset(slide.score);
            animateCounter(scoreEl, prevScore, slide.score, 1400);
        } else {
            setRingOffset(slide.score);
            scoreEl.textContent = slide.score;
        }

        // Fade out tags, update text, fade back in
        floatingTags.forEach(function (tag) { tag.classList.add('fade-out'); });

        setTimeout(function () {
            tagStrength.textContent = slide.strength;
            tagGap.textContent = slide.gap;
            tagTip.textContent = slide.tip;
            floatingTags.forEach(function (tag) { tag.classList.remove('fade-out'); });
        }, animate ? 400 : 0);

        // Update dots
        dots.forEach(function (dot, i) {
            dot.classList.toggle('active', i === index);
            // Tint active dot to match theme
            if (i === index) {
                dot.style.background = slide.theme === 'orange' ? '#f97316' : '';
                dot.style.boxShadow = slide.theme === 'orange'
                    ? '0 0 8px rgba(249, 115, 22, 0.4)' : '';
            } else {
                dot.style.background = '';
                dot.style.boxShadow = '';
            }
        });

        currentSlide = index;
    }

    function nextSlide() {
        var next = (currentSlide + 1) % slides.length;
        showSlide(next, true);
    }

    function startCarousel() {
        if (carouselTimer) return;
        carouselTimer = setInterval(nextSlide, SLIDE_INTERVAL);
    }

    function resetCarouselTimer() {
        clearInterval(carouselTimer);
        carouselTimer = null;
        startCarousel();
    }

    // Dot click handlers
    dots.forEach(function (dot) {
        dot.addEventListener('click', function () {
            var idx = parseInt(dot.getAttribute('data-slide'));
            if (idx === currentSlide) return;
            showSlide(idx, true);
            resetCarouselTimer();
        });
    });

    // Initialize carousel on first visibility
    if (wrapper && ring && scoreEl) {
        // Set initial dasharray
        ring.style.strokeDasharray = CIRCUMFERENCE;
        ring.style.strokeDashoffset = CIRCUMFERENCE;

        var ringObserver = new IntersectionObserver(
            function (entries) {
                entries.forEach(function (entry) {
                    if (entry.isIntersecting && !carouselStarted) {
                        carouselStarted = true;
                        showSlide(0, true);
                        // Start auto-rotation after first animation completes
                        setTimeout(startCarousel, 2000);
                        ringObserver.unobserve(entry.target);
                    }
                });
            },
            { threshold: 0.3 }
        );
        ringObserver.observe(wrapper);
    }

    /* ── Counter Animation ── */
    function animateCounter(element, from, to, duration) {
        var start = performance.now();

        function update(now) {
            var elapsed = now - start;
            var progress = Math.min(elapsed / duration, 1);
            var eased = 1 - Math.pow(1 - progress, 4);
            var current = Math.round(from + (to - from) * eased);
            element.textContent = current;

            if (progress < 1) {
                requestAnimationFrame(update);
            }
        }

        requestAnimationFrame(update);
    }
})();
