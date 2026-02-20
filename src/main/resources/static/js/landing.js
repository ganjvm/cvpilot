/* ============================================
   CVPilot Landing — Interactions & Animations
   ============================================ */

(function () {
    'use strict';

    /* ── Scroll Reveal (IntersectionObserver) ── */
    const revealElements = document.querySelectorAll('.reveal');
    const revealObserver = new IntersectionObserver(
        (entries) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('visible');
                    revealObserver.unobserve(entry.target);
                }
            });
        },
        { threshold: 0.15, rootMargin: '0px 0px -40px 0px' }
    );
    revealElements.forEach((el) => revealObserver.observe(el));

    /* ── Nav scroll state ── */
    const nav = document.querySelector('.nav');
    let ticking = false;
    window.addEventListener('scroll', () => {
        if (!ticking) {
            requestAnimationFrame(() => {
                nav.classList.toggle('scrolled', window.scrollY > 40);
                ticking = false;
            });
            ticking = true;
        }
    });

    /* ── Smooth scroll for anchor links ── */
    document.querySelectorAll('a[href^="#"]').forEach((link) => {
        link.addEventListener('click', (e) => {
            const target = document.querySelector(link.getAttribute('href'));
            if (target) {
                e.preventDefault();
                target.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
        });
    });

    /* ── Score Ring Animation ── */
    const scoreRing = document.querySelector('.score-ring-progress');
    const scoreValue = document.querySelector('.score-value');
    const targetScore = 87;

    if (scoreRing && scoreValue) {
        const ringObserver = new IntersectionObserver(
            (entries) => {
                entries.forEach((entry) => {
                    if (entry.isIntersecting) {
                        scoreRing.classList.add('animate');
                        animateCounter(scoreValue, 0, targetScore, 1800);
                        ringObserver.unobserve(entry.target);
                    }
                });
            },
            { threshold: 0.4 }
        );
        ringObserver.observe(scoreRing.closest('.score-ring-wrapper'));
    }

    function animateCounter(element, from, to, duration) {
        const start = performance.now();

        function update(now) {
            const elapsed = now - start;
            const progress = Math.min(elapsed / duration, 1);
            // ease-out expo
            const eased = 1 - Math.pow(1 - progress, 4);
            const current = Math.round(from + (to - from) * eased);
            element.textContent = current;

            if (progress < 1) {
                requestAnimationFrame(update);
            }
        }

        requestAnimationFrame(update);
    }
})();
