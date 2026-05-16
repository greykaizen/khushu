document.addEventListener('DOMContentLoaded', () => {
    // Check for reduced motion preference
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    if (prefersReducedMotion) {
        gsap.set('.reveal', { opacity: 1, y: 0, scale: 1, filter: 'blur(0px)' });
        return;
    }

    // Register ScrollTrigger
    gsap.registerPlugin(ScrollTrigger);

    // Initial Load Animations
    const tl = gsap.timeline();

    tl.from('nav', {
        y: -100,
        opacity: 0,
        duration: 1.2,
        ease: 'power3.out'
    })
    .from('.hero-eyebrow', {
        opacity: 0,
        y: 20,
        duration: 0.8,
        ease: 'power2.out'
    }, '-=0.4')
    .from('.hero-title', {
        opacity: 0,
        y: 30,
        duration: 1,
        ease: 'power3.out'
    }, '-=0.6')
    .from('.hero-body', {
        opacity: 0,
        y: 30,
        duration: 1,
        ease: 'power3.out'
    }, '-=0.8')
    .from('.hero-ctas', {
        opacity: 0,
        y: 30,
        duration: 1,
        ease: 'power3.out'
    }, '-=0.8')
    .from('.phone-mockup', {
        opacity: 0,
        y: 50,
        scale: 0.94,
        filter: 'blur(10px)',
        duration: 1.5,
        stagger: 0.2,
        ease: 'power4.out'
    }, '-=1');

    // Floating Animation for Phone Mockups
    gsap.to('.phone-mockup', {
        y: '+=15',
        rotation: '+=1',
        duration: 'random(4, 6)',
        repeat: -1,
        yoyo: true,
        ease: 'sine.inOut',
        stagger: {
            each: 0.5,
            from: 'random'
        }
    });

    // Ambient Glow Movement
    gsap.to('.glow', {
        x: 'random(-50, 50)',
        y: 'random(-50, 50)',
        duration: 'random(10, 20)',
        repeat: -1,
        yoyo: true,
        ease: 'sine.inOut',
        stagger: 2
    });

    // Scroll Reveal Animations
    const revealElements = document.querySelectorAll('.reveal');
    revealElements.forEach((el) => {
        gsap.from(el, {
            scrollTrigger: {
                trigger: el,
                start: 'top 85%',
                toggleActions: 'play none none none'
            },
            y: 40,
            opacity: 0,
            scale: 0.96,
            duration: 1.2,
            ease: 'power3.out'
        });
    });

    // Parallax Effect for Phone Cards (Desktop only)
    if (window.innerWidth > 1024) {
        gsap.to('.phone-stack', {
            scrollTrigger: {
                trigger: '.hero',
                start: 'top top',
                end: 'bottom top',
                scrub: true
            },
            y: 100,
            ease: 'none'
        });
    }
});
