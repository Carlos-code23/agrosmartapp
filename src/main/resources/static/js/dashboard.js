document.addEventListener('DOMContentLoaded', function() {
    // Mobile menu toggle
    const mobileMenuToggle = document.createElement('button');
    mobileMenuToggle.className = 'mobile-menu-toggle';
    mobileMenuToggle.innerHTML = '<i class="fas fa-bars"></i>';
    
    const sidebar = document.querySelector('.sidebar');
    const topBar = document.querySelector('.top-bar');
    
    if (window.innerWidth <= 768) {
        topBar.prepend(mobileMenuToggle);
        sidebar.classList.add('mobile-hidden');
    }
    
    mobileMenuToggle.addEventListener('click', function() {
        sidebar.classList.toggle('mobile-hidden');
    });
    
    // Task completion toggle
    const taskCheckboxes = document.querySelectorAll('.task-checkbox input');
    taskCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            const taskItem = this.closest('.task-item');
            if (this.checked) {
                taskItem.style.opacity = '0.6';
                taskItem.querySelector('h4').style.textDecoration = 'line-through';
            } else {
                taskItem.style.opacity = '1';
                taskItem.querySelector('h4').style.textDecoration = 'none';
            }
        });
    });
    
    // Responsive adjustments
    window.addEventListener('resize', function() {
        if (window.innerWidth <= 768) {
            if (!document.querySelector('.mobile-menu-toggle')) {
                topBar.prepend(mobileMenuToggle);
            }
            sidebar.classList.add('mobile-hidden');
        } else {
            if (document.querySelector('.mobile-menu-toggle')) {
                mobileMenuToggle.remove();
            }
            sidebar.classList.remove('mobile-hidden');
        }
    });
});