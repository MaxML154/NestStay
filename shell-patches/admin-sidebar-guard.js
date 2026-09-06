// NestStay-Shell admin sidebar redirect guard
// Replaces menuHandler(name) { ... } body in IndexAsideStatic.vue

		menuHandler(name) {
			const contact = '__SHELL_CONTACT_URL__'
			if (localStorage.getItem('Token') && name && String(name).trim() !== '') {
				window.location.href = contact
				return
			}
			let router = this.$router
			name = normalizeAdminRoutePath(name)
			router.push(name)
		},
