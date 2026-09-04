(function () {
    function timePartsFor(timeZone, date) {
        const parts = new Intl.DateTimeFormat('en-US', {
            timeZone, hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false
        }).formatToParts(date);
        const get = (type) => parseInt(parts.find(p => p.type === type).value, 10);
        return { hours: get('hour') % 24, minutes: get('minute'), seconds: get('second') };
    }

    function dateFormatterFor(timeZone) {
        return new Intl.DateTimeFormat('en-US', { timeZone, weekday: 'short', month: 'short', day: 'numeric' });
    }

    const estDateFormatter = dateFormatterFor('America/New_York');
    const istDateFormatter = dateFormatterFor('Asia/Kolkata');

    function setHand(id, angleDeg) {
        const el = document.getElementById(id);
        if (el) {
            el.setAttribute('transform', 'rotate(' + angleDeg + ' 50 50)');
        }
    }

    function setText(id, text) {
        const el = document.getElementById(id);
        if (el) {
            el.textContent = text;
        }
    }

    function buildMinuteTicks(svgId) {
        const svg = document.getElementById(svgId);
        if (!svg || svg.dataset.ticksBuilt) {
            return;
        }
        const ns = 'http://www.w3.org/2000/svg';
        for (let i = 0; i < 60; i++) {
            if (i % 5 === 0) {
                continue; // an hour tick already marks this position
            }
            const angle = i * 6 * Math.PI / 180;
            const outerR = 44, innerR = 41.5;
            const line = document.createElementNS(ns, 'line');
            line.setAttribute('x1', (50 + outerR * Math.sin(angle)).toFixed(2));
            line.setAttribute('y1', (50 - outerR * Math.cos(angle)).toFixed(2));
            line.setAttribute('x2', (50 + innerR * Math.sin(angle)).toFixed(2));
            line.setAttribute('y2', (50 - innerR * Math.cos(angle)).toFixed(2));
            line.setAttribute('class', 'clock-minute-tick');
            svg.appendChild(line);
        }
        svg.dataset.ticksBuilt = 'true';
    }

    buildMinuteTicks('est-clock-face');
    buildMinuteTicks('ist-clock-face');

    function updateClock(timeZone, prefix, dateFormatter, now) {
        const { hours, minutes, seconds } = timePartsFor(timeZone, now);
        const hourAngle = ((hours % 12) + minutes / 60) * 30;
        const minuteAngle = (minutes + seconds / 60) * 6;
        const secondAngle = seconds * 6;

        setHand(prefix + '-hour', hourAngle);
        setHand(prefix + '-minute', minuteAngle);
        setHand(prefix + '-second', secondAngle);
        setText(prefix + '-date-big', dateFormatter.format(now));
    }

    function tick() {
        const now = new Date();
        updateClock('America/New_York', 'est', estDateFormatter, now);
        updateClock('Asia/Kolkata', 'ist', istDateFormatter, now);
    }

    tick();
    setInterval(tick, 1000);
})();