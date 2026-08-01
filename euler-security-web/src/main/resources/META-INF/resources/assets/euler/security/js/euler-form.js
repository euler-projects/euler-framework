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
 *   data-otp-issue-url       Set on a button; requesting an OTP posts
 *                            channel + recipient here (already
 *                            context-resolved via Thymeleaf @{...}) and
 *                            the returned otp_ticket is stashed for the
 *                            login submission. CSRF and any other hidden
 *                            fields of the enclosing form ride along.
 *   data-otp-channel         Delivery channel to request, e.g. "sms".
 *   data-otp-recipient       Id of the input holding the recipient.
 *   data-otp-ticket          Id of the hidden input receiving the ticket.
 *   data-otp-sent-message    Localized confirmation shown on success.
 *   data-otp-failed-message  Localized fallback used when the endpoint
 *                            reports no error_description of its own.
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
            setErrorStatus(input, await readErrorMessage(response, response.statusText || 'invalid'));
        } catch (err) {
            setErrorStatus(input, err?.message || 'network error');
        }
    };

    /**
     * Read the error description an endpoint reported, falling back to
     * the caller's localized message.
     */
    const readErrorMessage = async (response, fallback) => {
        const body = await response.text();
        if (body) {
            try {
                const json = JSON.parse(body);
                if (json && json.error_description) {
                    return json.error_description;
                }
            } catch (_) { /* keep fallback */ }
        }
        return fallback || response.statusText || 'error';
    };

    /**
     * Ask the OTP issue endpoint for a ticket and stash it for the login
     * submission. Only the ticket comes back over the wire - the code
     * itself reaches the user out of band - so the recipient is never
     * part of the login form.
     *
     * Hidden fields of the enclosing form (the CSRF token above all) are
     * copied into the request, while the login fields are dropped: this
     * call issues a ticket, it does not sign anyone in.
     */
    const requestOtpTicket = async (button, recipientInput, ticketInput) => {
        const form = button.form;
        const params = form ? new URLSearchParams(new FormData(form)) : new URLSearchParams();
        params.delete('otp_ticket');
        params.delete('otp');
        params.set('channel', button.dataset.otpChannel || '');
        params.set('recipient', recipientInput.value);

        setLoadStatus(recipientInput);
        try {
            const response = await fetch(button.dataset.otpIssueUrl, {
                method: 'POST',
                credentials: 'same-origin',
                headers: { 'Accept': 'application/json' },
                body: params
            });
            if (!response.ok) {
                ticketInput.value = '';
                setErrorStatus(recipientInput,
                        await readErrorMessage(response, button.dataset.otpFailedMessage));
                return;
            }
            const ticket = await response.json();
            ticketInput.value = ticket?.otp_ticket || '';
            setSuccessStatus(recipientInput);
            const note = document.createElement('span');
            note.className = 'form-control-feedback-msg';
            note.textContent = button.dataset.otpSentMessage || '';
            getFormGroup(recipientInput)?.appendChild(note);
            startRetryCountdown(button, Number(ticket?.retry_after) || 0);
        } catch (err) {
            ticketInput.value = '';
            setErrorStatus(recipientInput,
                    err?.message || button.dataset.otpFailedMessage || 'network error');
        }
    };

    /**
     * Hold the button down for the interval the server asked for, so a
     * user cannot hammer the issue endpoint into its own rate limit.
     */
    const startRetryCountdown = (button, seconds) => {
        if (seconds <= 0) return;
        const label = button.textContent;
        let left = seconds;
        button.disabled = true;
        button.textContent = `${label} (${left})`;
        const timer = setInterval(() => {
            left -= 1;
            if (left <= 0) {
                clearInterval(timer);
                button.disabled = false;
                button.textContent = label;
                return;
            }
            button.textContent = `${label} (${left})`;
        }, 1000);
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

        // OTP ticket requests
        document.querySelectorAll('[data-otp-issue-url]').forEach((button) => {
            const recipientInput = document.getElementById(button.dataset.otpRecipient);
            const ticketInput = document.getElementById(button.dataset.otpTicket);
            if (!recipientInput || !ticketInput) return;
            button.addEventListener('click', () => {
                if (!recipientInput.value) {
                    recipientInput.focus();
                    return;
                }
                requestOtpTicket(button, recipientInput, ticketInput);
            });
            // A ticket belongs to the recipient it was issued for, so
            // editing the recipient invalidates it.
            recipientInput.addEventListener('input', () => {
                ticketInput.value = '';
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
