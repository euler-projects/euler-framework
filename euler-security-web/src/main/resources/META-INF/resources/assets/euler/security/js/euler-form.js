/*
 * Vanilla DOM helpers for euler-security-web form pages (signup,
 * change-password). Replaces the previous jQuery-based eulerForm helper.
 *
 * Public API exposed on window.eulerForm:
 *   setLoadStatus(input)
 *   setSuccessStatus(input)
 *   setErrorStatus(input, msg)
 *   clearStatus(input)
 *   validForm(formEl)
 *   bindBlurValidator(input, url, paramName, extraParams?)
 *   bindConfirmPassword(confirmInput, sourceInput, errMsg)
 *   refreshCaptcha(imgEl, baseUrl)
 */
(function (global) {
    'use strict';

    function getFormGroup(element) {
        // The original markup keeps the input directly inside .form-group.
        return element.parentElement;
    }

    function clearStatus(element) {
        var group = getFormGroup(element);
        if (!group) return;
        group.classList.remove('has-error');
        group.classList.remove('has-success');
        var nodes = Array.prototype.slice.call(group.children);
        var seen = false;
        for (var i = 0; i < nodes.length; i++) {
            var node = nodes[i];
            if (node === element) { seen = true; continue; }
            if (!seen) continue;
            if (node.classList &&
                (node.classList.contains('form-control-feedback') ||
                 node.classList.contains('form-control-feedback-msg'))) {
                group.removeChild(node);
            }
        }
    }

    function setLoadStatus(element) {
        clearStatus(element);
        var group = getFormGroup(element);
        if (!group) return;
        var span = document.createElement('span');
        span.className = 'form-control-feedback';
        var spinner = document.createElement('div');
        spinner.className = 'loading';
        span.appendChild(spinner);
        group.appendChild(span);
    }

    function setSuccessStatus(element) {
        clearStatus(element);
        var group = getFormGroup(element);
        if (!group) return;
        group.classList.add('has-success');
        var span = document.createElement('span');
        span.className = 'icon-ok form-control-feedback';
        group.appendChild(span);
    }

    function setErrorStatus(element, msg) {
        clearStatus(element);
        var group = getFormGroup(element);
        if (!group) return;
        group.classList.add('has-error');
        var icon = document.createElement('span');
        icon.className = 'icon-remove form-control-feedback';
        group.appendChild(icon);
        var note = document.createElement('span');
        note.className = 'form-control-feedback-msg';
        note.textContent = msg == null ? '' : String(msg);
        group.appendChild(note);
    }

    function validForm(formEl) {
        if (!formEl) return true;
        var bad = formEl.querySelectorAll('.form-group.has-error');
        return !bad || bad.length === 0;
    }

    function buildQuery(params) {
        var pairs = [];
        Object.keys(params).forEach(function (k) {
            var v = params[k];
            if (v === undefined || v === null) return;
            pairs.push(encodeURIComponent(k) + '=' + encodeURIComponent(v));
        });
        return pairs.join('&');
    }

    /**
     * Bind a blur-time async validator that posts the input value to the
     * given URL as a query parameter.
     *
     * @param input        the input element being validated
     * @param url          absolute URL (caller is expected to prefix the
     *                     servlet context path)
     * @param paramName    the query parameter name carrying the value
     * @param extraParams  optional flat object merged into the query
     */
    function bindBlurValidator(input, url, paramName, extraParams) {
        if (!input) return;
        input.addEventListener('blur', function () {
            setLoadStatus(input);
            var params = {};
            params[paramName] = input.value;
            if (extraParams) {
                Object.keys(extraParams).forEach(function (k) {
                    params[k] = extraParams[k];
                });
            }
            var query = buildQuery(params);
            var fullUrl = url + (url.indexOf('?') >= 0 ? '&' : '?') + query;
            fetch(fullUrl, {
                method: 'GET',
                credentials: 'same-origin',
                headers: { 'Accept': 'application/json' }
            }).then(function (response) {
                if (response.ok) {
                    setSuccessStatus(input);
                    return;
                }
                return response.text().then(function (body) {
                    var msg = response.statusText || 'invalid';
                    if (body) {
                        try {
                            var json = JSON.parse(body);
                            if (json && json.error_description) {
                                msg = json.error_description;
                            }
                        } catch (e) { /* keep default */ }
                    }
                    setErrorStatus(input, msg);
                });
            }).catch(function (err) {
                setErrorStatus(input, (err && err.message) || 'network error');
            });
        });
    }

    function bindConfirmPassword(confirmInput, sourceInput, errMsg) {
        if (!confirmInput || !sourceInput) return;
        confirmInput.addEventListener('blur', function () {
            var v = confirmInput.value;
            if (v == null || v === '') {
                return;
            }
            if (v === sourceInput.value) {
                setSuccessStatus(confirmInput);
            } else {
                setErrorStatus(confirmInput, errMsg);
            }
        });
    }

    function refreshCaptcha(imgEl, baseUrl) {
        if (!imgEl) return;
        imgEl.addEventListener('click', function () {
            var sep = baseUrl.indexOf('?') >= 0 ? '&' : '?';
            imgEl.src = baseUrl + sep + '_r=' + Date.now();
        });
    }

    global.eulerForm = {
        setLoadStatus: setLoadStatus,
        setSuccessStatus: setSuccessStatus,
        setErrorStatus: setErrorStatus,
        clearStatus: clearStatus,
        validForm: validForm,
        bindBlurValidator: bindBlurValidator,
        bindConfirmPassword: bindConfirmPassword,
        refreshCaptcha: refreshCaptcha
    };
})(window);
