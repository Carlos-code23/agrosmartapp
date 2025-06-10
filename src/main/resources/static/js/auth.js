document.addEventListener('DOMContentLoaded', function() {
    // Toggle password visibility
    const togglePasswordButtons = document.querySelectorAll('.toggle-password');
    
    togglePasswordButtons.forEach(button => {
        button.addEventListener('click', function() {
            const input = this.parentElement.querySelector('input');
            const icon = this.querySelector('i');
            
            if (input.type === 'password') {
                input.type = 'text';
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
                this.setAttribute('aria-label', 'Ocultar contraseña');
            } else {
                input.type = 'password';
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
                this.setAttribute('aria-label', 'Mostrar contraseña');
            }
        });
    });

    // Password validation hints (for register page)
    const passwordInput = document.getElementById('password');
    if (passwordInput) {
        passwordInput.addEventListener('input', function() {
            const hints = document.querySelectorAll('.password-hints .hint');
            const value = this.value;
            
            hints.forEach(hint => {
                const validationType = hint.getAttribute('data-validate');
                let isValid = false;
                
                switch(validationType) {
                    case 'length':
                        const minLength = parseInt(hint.getAttribute('data-length')) || 8;
                        isValid = value.length >= minLength;
                        break;
                    case 'uppercase':
                        isValid = /[A-Z]/.test(value);
                        break;
                    // Add more validation types as needed
                }
                
                hint.style.color = isValid ? '#38a169' : '#a0aec0';
            });
        });
    }
});