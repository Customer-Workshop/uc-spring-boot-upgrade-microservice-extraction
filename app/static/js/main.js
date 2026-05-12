/* Global UI helpers and agent status polling */

// Modal helpers (global scope)
function showModal(id) {
    document.getElementById(id).classList.remove('hidden');
}

function hideModal(id) {
    document.getElementById(id).classList.add('hidden');
}

// Agent status polling
(function pollAgentStatus() {
    const stateEl = document.getElementById('agent-state');
    if (!stateEl) return;

    async function poll() {
        try {
            const r = await fetch('/agent/status');
            if (r.ok) {
                const data = await r.json();
                stateEl.textContent = data.state;
                stateEl.className = '';
                if (data.state === 'IDLE') stateEl.className = 'badge-pending text-xs px-2 py-1 rounded';
                else if (data.state === 'EXECUTING') stateEl.className = 'badge-running text-xs px-2 py-1 rounded';
                else if (data.state === 'ERROR') stateEl.className = 'badge-fail text-xs px-2 py-1 rounded';
                else stateEl.className = 'badge-running text-xs px-2 py-1 rounded';
            }
        } catch (e) {}
    }

    poll();
    setInterval(poll, 5000);
})();
