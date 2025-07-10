document.addEventListener('DOMContentLoaded', function() {
    // 初始化分类选择器
    if (document.getElementById('categorySelect')) {
        new TomSelect('#categorySelect', {
            plugins: ['remove_button', 'clear_button'],
            create: false,
            maxItems: 1,
            placeholder: '选择分类...',
            dropdownParent: 'body'
        });
    }

    // 默认启用列表视图
    document.addEventListener('DOMContentLoaded', function() {
        document.body.classList.add('list-view');
        document.getElementById('listViewBtn').classList.add('btn-primary');
        document.getElementById('gridViewBtn').classList.remove('btn-primary');
        document.getElementById('listViewBtn').classList.remove('btn-outline-secondary');
        document.getElementById('gridViewBtn').classList.add('btn-outline-secondary');
    });

    // 视图切换功能
    document.getElementById('gridViewBtn')?.addEventListener('click', function() {
        document.body.classList.remove('list-view');
        this.classList.add('btn-primary');
        this.classList.remove('btn-outline-secondary');
        document.getElementById('listViewBtn').classList.remove('btn-primary');
        document.getElementById('listViewBtn').classList.add('btn-outline-secondary');
    });

    document.getElementById('listViewBtn')?.addEventListener('click', function() {
        document.body.classList.add('list-view');
        this.classList.add('btn-primary');
        this.classList.remove('btn-outline-secondary');
        document.getElementById('gridViewBtn').classList.remove('btn-primary');
        document.getElementById('gridViewBtn').classList.add('btn-outline-secondary');
    });
});