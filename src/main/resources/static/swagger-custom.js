window.onload = function () {

    const waitForSwagger = setInterval(() => {

        const authBtn = document.querySelector('.authorize');
        if (!authBtn) return;

        clearInterval(waitForSwagger);

        // META 조회
        fetch("/internal/meta")
            .then(res => res.text())
            .then(meta => {

                if (!meta) {
                    console.warn("META 없음 - 자동 세팅 스킵");
                    return;
                }

                authBtn.click();

                setTimeout(() => {

                    // 정확한 input 선택 (name 기준)
                    const inputs = document.querySelectorAll('input');
                    let targetInput = null;

                    inputs.forEach(input => {
                        if (input.placeholder?.toLowerCase().includes("value")) {
                            targetInput = input;
                        }
                    });

                    if (!targetInput) {
                        console.error("META input 못 찾음");
                        return;
                    }

                    targetInput.value = meta;
                    targetInput.dispatchEvent(new Event('input', { bubbles: true }));

                    const okBtn = document.querySelector('.modal-btn.auth.authorize');
                    if (okBtn) okBtn.click();

                }, 500);

            });

    }, 300);
};