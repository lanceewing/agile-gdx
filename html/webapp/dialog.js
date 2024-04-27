/**
 * Dialog module.
 * 
 * @module dialog.js
 * @version 1.0.0
 * @summary 02-01-2022
 * @author Mads Stoumann
 * @description Custom versions of `alert`, `confirm` and `prompt`, using `<dialog>`
 */
class Dialog {
	
	constructor(settings = {}) {
		this.settings = Object.assign(
			{
				accept: 'OK',
				bodyClass: 'dialog-open',
				cancel: 'Cancel',
				dialogClass: '',
				message: '',
				template: '',
				showStateButtons: false
			},
			settings
		);
		this.init();
	}

	collectFormData(formData) {
		const object = {};
		formData.forEach((value, key) => {
			if (!Reflect.has(object, key)) {
				object[key] = value;
				return;
			}
			if (!Array.isArray(object[key])) {
				object[key] = [object[key]];
			}
			object[key].push(value);
		});
		return object;
	}

	getFocusable() {
		return [...this.dialog.querySelectorAll('button,[href],select,textarea,input:not([type="hidden"]),[tabindex]:not([tabindex="-1"])')];
	}

	init() {
		this.dialogSupported = typeof HTMLDialogElement === 'function';
		this.dialog = document.createElement('dialog');
		this.dialog.role = 'dialog';
		this.dialog.dataset.component = this.dialogSupported ? 'dialog' : 'no-dialog';
		this.dialog.innerHTML = `
		    <form method="dialog" data-ref="form">
		      <fieldset data-ref="fieldset" role="document">
		        <legend data-ref="message" id="${(Math.round(Date.now())).toString(36)}"></legend>
		        <div data-ref="template"></div>
		      </fieldset>
		      <menu>
		        <button${this.dialogSupported ? '' : ` type="button"`} data-ref="export" value="export"><span></span></button>
		        <button${this.dialogSupported ? '' : ` type="button"`} data-ref="import" value="import"><span></span></button>
		        <button${this.dialogSupported ? '' : ` type="button"`} data-ref="reset" value="reset"><span></span></button>
		        <button${this.dialogSupported ? '' : ` type="button"`} data-ref="clear" value="clear"><span></span></button>
		        <button${this.dialogSupported ? '' : ` type="button"`} data-ref="accept" value="default"></button>
		        <button${this.dialogSupported ? '' : ` type="button"`} data-ref="cancel" value="cancel"></button>
		      </menu>
		    </form>`;
		document.body.appendChild(this.dialog);

		this.elements = {};
		this.focusable = [];
		this.dialog.querySelectorAll('[data-ref]').forEach(el => this.elements[el.dataset.ref] = el);
		this.dialog.setAttribute('aria-labelledby', this.elements.message.id);
		this.elements.cancel.addEventListener('click', () => { this.dialog.dispatchEvent(new Event('cancel')) });
		this.dialog.addEventListener('keydown', e => {
			if (e.key === 'Enter') {
				if (!this.dialogSupported) e.preventDefault();
				this.elements.accept.dispatchEvent(new Event('click'));
			}
			if (e.key === 'Escape') this.dialog.dispatchEvent(new Event('cancel'));
			if (e.key === 'Tab') {
				e.preventDefault();
				const len = this.focusable.length - 1;
				let index = this.focusable.indexOf(e.target);
				index = e.shiftKey ? index - 1 : index + 1;
				if (index < 0) index = len;
				if (index > len) index = 0;
				this.focusable[index].focus();
			}
		});
		this.toggle();
	}

	open(settings = {}) {
		const dialog = Object.assign({}, this.settings, settings);
		this.dialog.className = dialog.dialogClass || '';
		this.elements.accept.innerText = dialog.accept;
		this.elements.cancel.innerText = dialog.cancel;
		this.elements.cancel.hidden = dialog.cancel === '';;
		this.elements.export.hidden = !dialog.showStateButtons;
		this.elements.import.hidden = !dialog.showStateButtons;
		this.elements.clear.hidden = !dialog.showStateButtons;
		this.elements.reset.hidden = !dialog.showStateButtons;
		this.elements.message.innerText = dialog.message;
		this.elements.target = dialog.target || '';
		this.elements.template.innerHTML = dialog.template || '';

		this.focusable = this.getFocusable();
		this.hasFormData = this.elements.fieldset.elements.length > 0;

		this.toggle(true);

		if (this.hasFormData) {
			this.focusable[0].focus();
			// input fields and text areas have a select method that we call.
			if (this.focusable[0].select) {
			    this.focusable[0].select();
			}
		}
		else {
			this.elements.accept.focus();
		}
	}

	toggle(open = false) {
		if (this.dialogSupported && open) this.dialog.showModal();
		if (!this.dialogSupported) {
			document.body.classList.toggle(this.settings.bodyClass, open);
			this.dialog.hidden = !open;
			if (this.elements.target && !open) {
				this.elements.target.focus();
			}
		}
	}

	waitForUser() {
		return new Promise(resolve => {
			this.dialog.addEventListener('cancel', () => {
				this.toggle();
				resolve(false);
			}, { once: true });
			this.elements.accept.addEventListener('click', () => {
				let value = this.hasFormData ? this.collectFormData(new FormData(this.elements.form)) : true;
				this.toggle();
				resolve(value);
			}, { once: true });
			this.elements.export.addEventListener('click', () => {
				this.toggle();
				resolve("EXPORT");
			}, { once: true });
			this.elements.import.addEventListener('click', () => {
				this.toggle();
				resolve("IMPORT");
			}, { once: true });
			this.elements.clear.addEventListener('click', () => {
				this.toggle();
				resolve("CLEAR");
			}, { once: true });
			this.elements.reset.addEventListener('click', () => {
				this.toggle();
				resolve("RESET");
			}, { once: true });
		})
	}

	alert(message, defaultSettings = { template: '' }) {
		const settings = Object.assign(defaultSettings, { cancel: '', message: message });
		this.open(settings);
		return this.waitForUser();
	}

	confirm(message) {
		const settings = Object.assign({}, { message: message, template: '' });
		this.open(settings);
		return this.waitForUser();
	}

	prompt(message, value) {
		const template = `<label aria-label="${message}"><input type="text" name="prompt" value="${value}"></label>`;
		const settings = Object.assign({}, { message: message, template: template });
		this.open(settings);
		return this.waitForUser();
	}
	
	promptForOption(title, message, options) {
		let template = `<label for="options-select">${message}</label>`;
		template += '<select name="option" id="options-select">';
		options.forEach((option) => {
			template += `<option value="${option}">${option}</option>`;
		});
		template += '</select>';
		const settings = Object.assign({}, { message: title, template: template});
		this.open(settings);
		return this.waitForUser();
	}
}

window.Dialog = Dialog;
