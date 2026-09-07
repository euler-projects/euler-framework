/*
 * euler-form.js
 *
 * Drop this script into a Thymeleaf page rendered by euler-security-web
 * and the form interactions are wired up automatically based on data-*
 * attributes. No inline scripts in the templates are required.
 *
 * Everything here is progressive enhancement. A page renders complete
 * and usable without this file; the script only upgrades what it finds.
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
 * OTP ticket issue. Set these on the button that requests a ticket, or
 * on the enclosing <form> to share one configuration between several
 * triggers (a "continue" button and a "resend" link):
 *
 *   data-otp-issue-url       Endpoint that trades channel + recipient
 *                            for an otp_ticket (context-resolved via
 *                            Thymeleaf @{...}). CSRF and any other
 *                            hidden fields of the enclosing form ride
 *                            along.
 *   data-otp-channel         Delivery channel to request, e.g. "email".
 *   data-otp-sent-message    Localized confirmation shown on success.
 *                            May contain {0}, replaced by the recipient.
 *   data-otp-failed-message  Localized fallback used when the endpoint
 *                            reports no error_description of its own.
 *
 * The recipient and ticket inputs are found either by explicit id or by
 * marker, so both of these resolve:
 *
 *   data-otp-recipient="id"  Id of the input holding the recipient.
 *   data-otp-ticket="id"     Id of the hidden input receiving the ticket.
 *   data-otp-recipient       On the recipient input itself.
 *   data-otp-code            On the code input; enables the slot UI.
 *
 * Two-step flow. Set data-otp-flow on the <form> and data-otp-step on
 * each panel ("recipient", then "code"); the panels are stacked until
 * this script makes them mutually exclusive. Optional companions:
 *
 *   data-otp-issue           Marks a button that requests a ticket.
 *   data-otp-back            Marks a button that returns to step one.
 *   data-otp-error           Element receiving the wrong-code message.
 *   data-otp-sent            Element receiving the code-sent message.
 *   data-otp-length          Digit count for the slot UI (default 6).
 *   data-otp-autosubmit      "false" keeps the user on the sign-in
 *                            button instead of posting once the code is
 *                            complete (default: submit).
 *
 * Public API (window.eulerForm) exposes the low-level status helpers for
 * advanced custom logic; in normal use the DOM-driven wiring above is
 * sufficient.
 */
(() => {
    'use strict';

    /*
     * A ticket survives a redirect in sessionStorage so that a rejected
     * code lands the user back on step two instead of making them ask
     * for a new email. Scoped to the tab and expired against the TTL the
     * issue endpoint reported, so it can never outlive the ticket itself.
     */
    const OTP_STASH_KEY = 'euler.otp.pending';

    const readStash = () => {
        try {
            return JSON.parse(window.sessionStorage.getItem(OTP_STASH_KEY) || 'null');
        } catch (_) {
            return null;
        }
    };

    const writeStash = (record) => {
        try {
            window.sessionStorage.setItem(OTP_STASH_KEY, JSON.stringify(record));
        } catch (_) { /* storage unavailable: the flow still works, it just cannot resume */ }
    };

    const clearStash = () => {
        try {
            window.sessionStorage.removeItem(OTP_STASH_KEY);
        } catch (_) { /* nothing to undo */ }
    };

    const stashUsable = (record) =>
        !!record && typeof record.ticket === 'string' && record.ticket !== ''
        && (!record.expiresAt || record.expiresAt > Date.now());

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
        el.removeAttribute('aria-invalid');
        const span = document.createElement('span');
        span.className = 'icon-ok form-control-feedback';
        group.appendChild(span);
    };

    const setErrorStatus = (el, msg) => {
        clearStatus(el);
        const group = getFormGroup(el);
        if (!group) return;
        group.classList.add('has-error');
        el.setAttribute('aria-invalid', 'true');
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
     * Resolve the issue-endpoint configuration for a trigger. Attributes
     * may sit on the trigger itself or on any ancestor that carries the
     * endpoint, which lets a stepped form declare them once and share
     * them between its continue button and its resend link.
     */
    const issueConfigFor = (trigger) => {
        const carrier = trigger.closest('[data-otp-issue-url]');
        if (!carrier) return null;
        return {
            url: carrier.dataset.otpIssueUrl,
            channel: carrier.dataset.otpChannel || '',
            sentMessage: carrier.dataset.otpSentMessage || '',
            failedMessage: carrier.dataset.otpFailedMessage || ''
        };
    };

    /** An element named by id on the trigger, else found inside the form. */
    const resolvePart = (explicitId, selector, form) =>
        (explicitId ? document.getElementById(explicitId) : null)
        || (form ? form.querySelector(selector) : null);

    /** Interpolate the {0} placeholder i18n patterns carry. */
    const format = (pattern, value) =>
        String(pattern == null ? '' : pattern).replace('{0}', value == null ? '' : String(value));

    /**
     * Whether an element is on the panel the user is looking at. Judged
     * from the step state rather than from layout, because a message
     * written into a step the flow has hidden is a message nobody reads.
     * Elements outside any step - the single-form case - are always on
     * screen.
     */
    const onCurrentStep = (el) => {
        const step = el.closest('.otp-step');
        return !step || step.classList.contains('is-current');
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
     *
     * Returns {@code {ok, retryAfter}}. The caller owns the countdown,
     * because in a stepped flow the button that requested the ticket is
     * not always the one that should show it: continuing from step one
     * hands the wait to step two's resend link.
     */
    const requestOtpTicket = async (ctx) => {
        const { trigger, config, form, recipientInput, ticketInput } = ctx;
        const params = form ? new URLSearchParams(new FormData(form)) : new URLSearchParams();
        params.delete('otp_ticket');
        params.delete('otp');
        params.set('channel', config.channel);
        params.set('recipient', recipientInput.value);

        // Resending from step two leaves the recipient input on a hidden
        // panel, so its spinner and its error text would go unseen. Report
        // wherever the user is actually looking.
        const recipientVisible = onCurrentStep(recipientInput);
        const errorNote = form ? form.querySelector('[data-otp-error]') : null;

        const reportFailure = (message) => {
            ticketInput.value = '';
            clearStash();
            if (recipientVisible) {
                setErrorStatus(recipientInput, message);
            } else if (errorNote) {
                errorNote.textContent = message;
                errorNote.hidden = false;
            }
        };

        if (recipientVisible) setLoadStatus(recipientInput);
        trigger.disabled = true;
        try {
            const response = await fetch(config.url, {
                method: 'POST',
                credentials: 'same-origin',
                headers: { 'Accept': 'application/json' },
                body: params
            });
            if (!response.ok) {
                reportFailure(await readErrorMessage(response, config.failedMessage));
                return { ok: false, retryAfter: 0 };
            }
            const ticket = await response.json();
            ticketInput.value = ticket?.otp_ticket || '';
            if (recipientVisible) setSuccessStatus(recipientInput);

            // Only a stepped flow has anywhere to resume to, so only one
            // persists the ticket. A single-form page would write state
            // nothing ever reads back.
            if (ctx.stepped) {
                const seconds = Number(ticket?.expires_in) || 0;
                writeStash({
                    channel: config.channel,
                    recipient: recipientInput.value,
                    ticket: ticketInput.value,
                    // A little shorter than the server's TTL: by the time the
                    // last second ticks over, a retry could not succeed anyway.
                    expiresAt: seconds > 0 ? Date.now() + (seconds - 5) * 1000 : 0,
                    submitted: false
                });
            }

            if (errorNote) errorNote.hidden = true;
            announceSent(ctx, recipientInput.value);
            return { ok: true, retryAfter: Number(ticket?.retry_after) || 0 };
        } catch (err) {
            reportFailure(err?.message || config.failedMessage || 'network error');
            return { ok: false, retryAfter: 0 };
        } finally {
            trigger.disabled = false;
        }
    };

    /**
     * Report that the code is on its way - into the stepped flow's own
     * note element when there is one, otherwise beside the recipient
     * input as before.
     */
    const announceSent = (ctx, recipient) => {
        const note = ctx.form ? ctx.form.querySelector('[data-otp-sent]') : null;
        const text = format(ctx.config.sentMessage, recipient);
        if (note) {
            note.textContent = text;
            note.hidden = false;
            return;
        }
        const span = document.createElement('span');
        span.className = 'form-control-feedback-msg';
        span.textContent = text;
        getFormGroup(ctx.recipientInput)?.appendChild(span);
    };

    /**
     * Hold the button down for the interval the server asked for, so a
     * user cannot hammer the issue endpoint into its own rate limit.
     */
    const startRetryCountdown = (button, seconds) => {
        if (seconds <= 0) return;
        const label = button.dataset.otpCountdownLabel || button.textContent;
        button.dataset.otpCountdownLabel = label;
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

    /**
     * Upgrade a single code input into the slot UI.
     *
     * The input stays the only editable element: one real input per digit
     * would cost the page its `autocomplete="one-time-code"` autofill,
     * split a single field into six tab stops, and break pasting a whole
     * code. The slots mirror the input's value and are hidden from
     * assistive technology, which reads the one labelled control instead.
     */
    const enhanceOtpInput = (input) => {
        const declared = Math.max(1, Number(input.dataset.otpLength) || 6);
        const form = input.form;

        const wrapper = document.createElement('div');
        wrapper.className = 'otp-wrapper';
        const slotRow = document.createElement('div');
        slotRow.className = 'otp-slots';
        slotRow.setAttribute('aria-hidden', 'true');
        const slotEls = [];

        const addSlot = () => {
            const slot = document.createElement('span');
            slot.className = 'otp-slot';
            slotRow.appendChild(slot);
            slotEls.push(slot);
        };
        for (let i = 0; i < declared; i += 1) addSlot();

        input.parentNode.insertBefore(wrapper, input);
        wrapper.appendChild(slotRow);
        wrapper.appendChild(input);

        input.classList.add('otp-input');
        input.setAttribute('inputmode', 'numeric');
        input.setAttribute('autocomplete', 'one-time-code');
        if (!input.hasAttribute('aria-label') && !input.id) {
            input.setAttribute('aria-label', input.placeholder || 'Verification code');
        }
        // The visible border now belongs to the slots, not to the input.
        input.removeAttribute('placeholder');

        /*
         * No maxlength, deliberately. The declared length comes from
         * configuration, but an application may resolve its OTP policy
         * itself and issue something longer, and a code truncated by
         * maxlength is one that can never authenticate - a far worse
         * failure than letting the user overtype and be told the code is
         * wrong. Slots grow to cover a longer code, up to a ceiling that
         * keeps the row inside the form's width; past that the digits
         * still live in the input and still submit.
         */
        const MAX_SLOTS = 12;
        const ensureCapacity = (needed) => {
            while (slotEls.length < needed) addSlot();
        };

        const paint = () => {
            const value = input.value || '';
            ensureCapacity(Math.min(Math.max(declared, value.length), MAX_SLOTS));
            let caret = value.length;
            try {
                if (input.selectionStart != null) caret = input.selectionStart;
            } catch (_) { /* selection is not queryable on every input type */ }
            const active = Math.min(caret, slotEls.length - 1);
            slotEls.forEach((slot, i) => {
                slot.textContent = value.charAt(i);
                slot.classList.toggle('is-filled', value.charAt(i) !== '');
                slot.classList.toggle('is-active', i === active && document.activeElement === input);
            });
        };

        ['input', 'focus', 'blur', 'click', 'keyup'].forEach((evt) =>
                input.addEventListener(evt, paint));
        paint();

        // A complete code is a finished intention; posting it saves a
        // click. Opt out with data-otp-autosubmit="false", which a
        // rejected attempt also sets so the user is not caught in a
        // clear-and-resubmit loop they never asked for.
        let posting = false;
        input.addEventListener('input', () => {
            clearOtpError(form);
            if (posting || !form || input.dataset.otpAutosubmit === 'false') return;
            if ((input.value || '').length !== declared) return;
            posting = true;
            // requestSubmit fires the submit event, so the stash flag and
            // any data-prevalidate gate still see this submission.
            if (form.requestSubmit) form.requestSubmit(); else form.submit();
            // Release the guard if something prevented the submission; on
            // success the page is already navigating away.
            setTimeout(() => { posting = false; }, 600);
        });

        wrapper.classList.add('is-enhanced');
        return { wrapper, slotEls, paint };
    };

    /** Drop the wrong-code state from a form. */
    const clearOtpError = (form) => {
        if (!form) return;
        const note = form.querySelector('[data-otp-error]');
        const wrapper = form.querySelector('.otp-wrapper');
        const input = form.querySelector('.otp-input');
        if (note) note.hidden = true;
        if (wrapper) wrapper.classList.remove('is-invalid', 'is-shaking');
        if (input) input.removeAttribute('aria-invalid');
    };

    /**
     * Show the wrong-code state on step two: colour the slots, shake the
     * row, clear the digits so the next attempt starts clean, and say so
     * in the flow's own message element.
     */
    const showOtpError = (form, message) => {
        const wrapper = form.querySelector('.otp-wrapper');
        const input = form.querySelector('.otp-input');
        // Clear the digits first: the synthetic input event below wakes the
        // autosubmit listener, which drops any error state it finds.
        if (input) {
            input.value = '';
            input.dispatchEvent(new Event('input', { bubbles: true }));
            input.setAttribute('aria-invalid', 'true');
            // Hand control back to the sign-in button: the declared digit
            // count has now been contradicted once, so guessing when the
            // next code is complete is no longer safe.
            input.dataset.otpAutosubmit = 'false';
        }
        const note = form.querySelector('[data-otp-error]');
        if (note) {
            if (message) note.textContent = message;
            note.hidden = false;
        }
        if (wrapper) {
            wrapper.classList.add('is-invalid');
            wrapper.classList.remove('is-shaking');
            // Reflow so the animation restarts on a second failure.
            void wrapper.offsetWidth;
            wrapper.classList.add('is-shaking');
        }
        input?.focus();
    };

    /**
     * Drive the recipient -> code step sequence.
     *
     * Panels are already in the document and stacked; this only makes
     * them mutually exclusive once a ticket has been issued, and keeps
     * the user on step two across the redirect a rejected code causes.
     * The dispatch endpoint answers a failed OTP submission with a plain
     * redirect back to the login page and no query parameter, so the
     * return is recognised from the stash flag written at submit time
     * rather than from the URL.
     */
    const initOtpFlow = (form) => {
        const steps = Array.from(form.querySelectorAll('[data-otp-step]'));
        if (steps.length < 2) return;

        const recipientInput = form.querySelector('[data-otp-recipient]');
        const ticketInput = form.querySelector('input[name="otp_ticket"]');
        if (!recipientInput || !ticketInput) return;

        // The code input was already upgraded to the slot UI during init;
        // this controller only decides which step is showing.
        form.classList.add('otp-flow-active');

        const show = (name) => {
            steps.forEach((step) =>
                    step.classList.toggle('is-current', step.dataset.otpStep === name));
        };

        const stash = readStash();
        if (stashUsable(stash) && stash.channel === (form.dataset.otpChannel || stash.channel)) {
            recipientInput.value = stash.recipient || '';
            ticketInput.value = stash.ticket;
            show('code');
            // Resuming has no issue response to announce the recipient,
            // and on a return visit that is exactly the thing worth
            // restating.
            announceSent({
                form,
                config: { sentMessage: form.dataset.otpSentMessage || '' }
            }, stash.recipient);
            if (stash.submitted) {
                // Signing in successfully leaves this page, so arriving
                // back here with the submit flag still set means the code
                // was rejected. The ticket is not spent by a failure -
                // OtpTicketService only discards it once its failure
                // ceiling is reached - so retrying in place is legitimate.
                showOtpError(form);
                // The page-level banner, if the deployment surfaces one,
                // would describe a password failure; ours is more precise.
                document.querySelectorAll('.info-wrapper').forEach((el) => { el.hidden = true; });
            }
            writeStash({ ...stash, submitted: false });
            // A real focus repaints the slots with the restored digits and
            // leaves the caret where the user can carry on typing.
            form.querySelector('.otp-input')?.focus();
        } else {
            clearStash();
            show('recipient');
        }

        // Going back abandons the ticket: it belongs to the recipient it
        // was issued for, and step one is where that gets corrected.
        form.querySelectorAll('[data-otp-back]').forEach((button) => {
            button.addEventListener('click', () => {
                clearStash();
                clearOtpError(form);
                ticketInput.value = '';
                show('recipient');
                recipientInput.focus();
            });
        });

        form.addEventListener('submit', () => {
            const current = readStash();
            if (stashUsable(current)) writeStash({ ...current, submitted: true });
        });
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
        //
        // An element is a trigger when it asks for a ticket. Under the
        // legacy contract that was the button carrying the endpoint URL
        // itself; the stepped contract instead puts the URL on the <form>
        // so several buttons can share it, and marks the buttons with
        // data-otp-issue. A <form> is therefore a carrier, never a
        // trigger - without that exclusion a click on a button inside it
        // would bubble and issue the ticket twice, the second time
        // without the form's CSRF field.
        //
        // A stepped form also wires two triggers to one endpoint, so the
        // recipient-side listeners are registered once per input rather
        // than once per trigger.
        const recipientBound = new WeakSet();
        const isTrigger = (el) => el.hasAttribute('data-otp-issue')
                || (el.hasAttribute('data-otp-issue-url') && el.tagName !== 'FORM');
        document.querySelectorAll('[data-otp-issue-url], [data-otp-issue]').forEach((trigger) => {
            if (!isTrigger(trigger)) return;
            const config = issueConfigFor(trigger);
            if (!config) return;
            const form = trigger.form || trigger.closest('form');
            const recipientInput = resolvePart(trigger.dataset.otpRecipient,
                    '[data-otp-recipient]', form);
            const ticketInput = resolvePart(trigger.dataset.otpTicket,
                    'input[name="otp_ticket"]', form);
            if (!recipientInput || !ticketInput) return;

            const stepped = !!(form && form.hasAttribute('data-otp-flow'));

            trigger.addEventListener('click', async () => {
                if (!recipientInput.value) {
                    recipientInput.focus();
                    return;
                }
                const { ok, retryAfter } = await requestOtpTicket({
                    trigger, config, form, recipientInput, ticketInput, stepped
                });
                if (!ok) return;

                if (stepped && form) {
                    form.querySelector('[data-otp-step="code"]')?.classList.add('is-current');
                    form.querySelector('[data-otp-step="recipient"]')?.classList.remove('is-current');
                    // The wait belongs to the button that can end it, which
                    // is step two's resend link rather than the continue
                    // button that is now off screen.
                    const resend = form.querySelector('[data-otp-step="code"] [data-otp-issue]');
                    startRetryCountdown(resend || trigger, retryAfter);
                    form.querySelector('.otp-input')?.focus();
                } else {
                    startRetryCountdown(trigger, retryAfter);
                }
            });

            // A ticket belongs to the recipient it was issued for, so
            // editing the recipient invalidates it.
            if (!recipientBound.has(recipientInput)) {
                recipientBound.add(recipientInput);
                recipientInput.addEventListener('input', () => {
                    ticketInput.value = '';
                    clearStash();
                });
            }
        });

        // Slot UI for every code input, whether or not it sits in a
        // stepped flow.
        document.querySelectorAll('[data-otp-code]').forEach((input) => {
            if (input.closest('.otp-wrapper')) return; // already enhanced
            enhanceOtpInput(input);
        });

        // Two-step OTP flows
        document.querySelectorAll('form[data-otp-flow]').forEach(initOtpFlow);

        /*
         * Any other login method abandons a pending OTP ticket. Without
         * this, failing a password sign-in would resurrect step two of a
         * flow the user had already left.
         */
        document.querySelectorAll('form').forEach((form) => {
            if (form.hasAttribute('data-otp-flow')) return;
            form.addEventListener('submit', clearStash);
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
