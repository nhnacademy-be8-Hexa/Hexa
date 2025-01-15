// 로딩 스피너 라이브러리
const LoadingSpinner = (() => {
    // 스피너 HTML 템플릿
    const spinnerTemplate = `
    <div id="loading-overlay" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.5); z-index: 9999;">
  <div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); text-align: center;">
    <div class="spinner"></div>
    <p style="color: white; margin-top: 10px;">잠시만 기다려주세요...</p>
  </div>
</div>
    <style>
  .spinner {
    width: 50px;
    height: 50px;
    border: 5px solid rgba(255, 255, 255, 0.2);
    border-top: 5px solid white;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin: 0 auto; /* 가운데 정렬 */
  }
  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }
</style>
  `;

    // DOM에 로딩 스피너 추가
    const init = () => {
        if (!document.getElementById('loading-overlay')) {
            const template = document.createElement('div');
            template.innerHTML = spinnerTemplate;
            document.body.appendChild(template);
        }
    };

    return {
        show: () => {
            init();
            document.getElementById('loading-overlay').style.display = 'flex';
        },
        hide: () => {
            const overlay = document.getElementById('loading-overlay');
            if (overlay) overlay.style.display = 'none';
        },
    };
})();
