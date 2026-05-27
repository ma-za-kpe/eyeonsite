(function () {
  window.__eyeonsitePageLoadedAt = Date.now();

  function track() {
    if (typeof window.gtag === 'function') {
      window.gtag.apply(window, arguments);
    }
  }

  document.addEventListener('click', function (event) {
    var cta = event.target.closest('[data-cta-id]');
    if (!cta) return;

    track('event', 'cta_click', {
      cta_id: cta.dataset.ctaId,
      section: cta.dataset.ctaSection || 'landing'
    });
  }, true);

  document.addEventListener('submit', function (event) {
    var form = event.target;
    if (!form || form.id !== 'waitlist-form') return;

    var honeypot = document.getElementById('wl-company');
    var trapValue = honeypot ? honeypot.value.trim() : '';
    var tooFast = Date.now() - window.__eyeonsitePageLoadedAt < 2000;

    if (trapValue || tooFast) {
      event.preventDefault();
      event.stopImmediatePropagation();
      track('event', 'waitlist_spam_blocked', {
        reason: trapValue ? 'honeypot' : 'timing'
      });
    }
  }, true);
})();
