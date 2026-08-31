(function () {
    function formatterFor(timeZone) {
        return new Intl.DateTimeFormat('en-US', {
            timeZone,
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: true,
            timeZoneName: 'short'
        });
    }

    const estFormatter = formatterFor('America/New_York');
    const istFormatter = formatterFor('Asia/Kolkata');

    function tick() {
        const now = new Date();
        const estEl = document.getElementById('est-clock');
        const istEl = document.getElementById('ist-clock');
        if (estEl) {
            estEl.textContent = estFormatter.format(now);
        }
        if (istEl) {
            istEl.textContent = istFormatter.format(now);
        }
    }

    tick();
    setInterval(tick, 1000);
})();