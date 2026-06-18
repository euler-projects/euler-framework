/*
 * euler-form.js
 *
 * Drop this script into a Thymeleaf page rendered by euler-security-web
 * and the form interactions are wired up automatically based on data-*
 * attributes. No inline scripts in the templates are required.
 *
 * Wiring contract (set on input/img/form elements in the template):
 *
 *   data-validate-url        Async blur-time validator endpoint (already
 *                            context-resolved via Thymeleaf @{...}).
 *   data-validate-param      Query parameter name carrying the input
 *                            value (defaults to the input's name).
 *   data-validate-extra      Optional extra query string merged into the
 *                            request, e.g. "scope=signup".
 *
 *   data-confirm-source      Id of the source input whose value the
 *                            current input must match on blur.
 *   data-mismatch-message    Localized error message displayed when the
 *                            two values differ.
 *
 *   data-refresh-url         Set on an <img>; clicking the image reloads
 *                            it from this URL with a cache-busting param.
 *
 *   data-prevalidate         Set on a <form>; submission is blocked when
 *                            any .form-group inside it has .has-error.
 *
 * Public API (window.eulerForm) exposes the low-level status helpers for
 * advanced custom logic; in normal use the DOM-driven wiring above is
 * sufficient.
 */
(() => {
    'use strict';

    const getFormGroup = (el) => el.parentElement;

    const clearStatus = (el) => {
        const group = getFormGroup(el);
        if (!group) return;
        group.classList.remove('has-error', 'has-success');
        let next = el.nextElementSibling;
        while (next) {
            const after = next.nextElementSibling;
            if (next.classList && (
                    next.classList.contains('form-control-feedback') ||
                    next.classList.contains('form-control-feedback-msg'))) {
                next.remove();
            }
            next = after;
        }
    };

    const setLoadStatus = (el) => {
        clearStatus(el);
        const group = getFormGroup(el);
        if (!group) return;
        const span = document.createElement('span');
        span.className = 'form-control-feedback';
        const spinner = document.createElement('div');
        spinner.className = 'loading';
        span.appendChild(spinner);
        group.appendChild(span);
    };

    const setSuccessStatus = (el) => {
        clearStatus(el);
        const group = getFormGroup(el);
        if (!group) return;
        group.classList.add('has-success');
        const span = document.createElement('span');
        span.className = 'icon-ok form-control-feedback';
        group.appendChild(span);
    };

    const setErrorStatus = (el, msg) => {
        clearStatus(el);
        const group = getFormGroup(el);
        if (!group) return;
        group.classList.add('has-error');
        const icon = document.createElement('span');
        icon.className = 'icon-remove form-control-feedback';
        group.appendChild(icon);
        const note = document.createElement('span');
        note.className = 'form-control-feedback-msg';
        note.textContent = msg == null ? '' : String(msg);
        group.appendChild(note);
    };

    const validForm = (formEl) => {
        if (!formEl) return true;
        return formEl.querySelectorAll('.form-group.has-error').length === 0;
    };

    /**
     * Fire a GET request that posts the input value as a single query
     * parameter and translate the response into a feedback state.
     */
    const runBlurValidator = async (input, url, paramName, extraQuery) => {
        setLoadStatus(input);
        const params = new URLSearchParams();
        params.set(paramName, input.value);
        if (extraQuery) {
            new URLSearchParams(extraQuery).forEach((value, key) => {
                params.set(key, value);
            });
        }
        const sep = url.includes('?') ? '&' : '?';
        try {
            const response = await fetch(url + sep + params.toString(), {
                method: 'GET',
                credentials: 'same-origin',
                headers: { 'Accept': 'application/json' }
            });
            if (response.ok) {
                setSuccessStatus(input);
                return;
            }
            const body = await response.text();
            let msg = response.statusText || 'invalid';
            if (body) {
                try {
                    const json = JSON.parse(body);
                    if (json && json.error_description) {
                        msg = json.error_description;
                    }
                } catch (_) { /* keep default */ }
            }
            setErrorStatus(input, msg);
        } catch (err) {
            setErrorStatus(input, err?.message || 'network error');
        }
    };

    const init = () => {
        // Async blur validators
        document.querySelectorAll('[data-validate-url]').forEach((input) => {
            const url = input.dataset.validateUrl;
            const paramName = input.dataset.validateParam || input.name || 'value';
            const extra = input.dataset.validateExtra;
            input.addEventListener('blur', () => {
                runBlurValidator(input, url, paramName, extra);
            });
        });

        // Confirm-password matchers
        document.querySelectorAll('[data-confirm-source]').forEach((confirmInput) => {
            const sourceId = confirmInput.dataset.confirmSource;
            const source = document.getElementById(sourceId);
            const message = confirmInput.dataset.mismatchMessage || '';
            if (!source) return;
            confirmInput.addEventListener('blur', () => {
                const value = confirmInput.value;
                if (!value) return;
                if (value === source.value) {
                    setSuccessStatus(confirmInput);
                } else {
                    setErrorStatus(confirmInput, message);
                }
            });
        });

        // Captcha refreshers
        document.querySelectorAll('[data-refresh-url]').forEach((img) => {
            const baseUrl = img.dataset.refreshUrl;
            img.addEventListener('click', () => {
                const sep = baseUrl.includes('?') ? '&' : '?';
                img.src = `${baseUrl}${sep}_r=${Date.now()}`;
            });
        });

        // Pre-submit validation gate
        document.querySelectorAll('form[data-prevalidate]').forEach((form) => {
            form.addEventListener('submit', (event) => {
                if (!validForm(form)) {
                    event.preventDefault();
                }
            });
        });
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // Public API for advanced custom logic.
    window.eulerForm = {
        setLoadStatus,
        setSuccessStatus,
        setErrorStatus,
        clearStatus,
        validForm
    };
})();
