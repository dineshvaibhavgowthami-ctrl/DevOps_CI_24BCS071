const notice = document.getElementById("notice");
let activeMoveBarcode = "";

async function api(url, options = {}) {
    const response = await fetch(url, options);
    const body = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(body.error || "The request could not be completed.");
    return body;
}

function showNotice(message, isError = false) {
    notice.textContent = message;
    notice.className = `notice ${isError ? "error" : "success"}`;
    notice.hidden = false;
    window.scrollTo({ top: 0, behavior: "smooth" });
}

function locationText(item) {
    return [item.warehouse, item.rack, item.shelf, item.cell].filter(Boolean).join(" / ");
}

function renderItem(item, target) {
    target.innerHTML = `<dl>
        <div><dt>ITEM NAME</dt><dd>${escapeHtml(item.name)}</dd></div>
        <div><dt>QUANTITY</dt><dd>${item.quantity}</dd></div>
        <div><dt>BARCODE</dt><dd class="barcode">${escapeHtml(item.barcode)}</dd></div>
        <div><dt>STATUS</dt><dd>${escapeHtml(item.status.replaceAll("_", " "))}</dd></div>
        <div><dt>WAREHOUSE</dt><dd>${escapeHtml(item.warehouse || "Exited")}</dd></div>
        <div><dt>RACK</dt><dd>${escapeHtml(item.rack || "—")}</dd></div>
        <div><dt>SHELF</dt><dd>${escapeHtml(item.shelf || "—")}</dd></div>
        <div><dt>CELL</dt><dd>${escapeHtml(item.cell || "—")}</dd></div>
    </dl>`;
    target.hidden = false;
}

function escapeHtml(value) {
    return String(value).replace(/[&<>"']/g, character => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    })[character]);
}

async function loadDashboard() {
    const data = await api("/api/dashboard");
    document.getElementById("total-items").textContent = data.totalItems;
    document.getElementById("occupied-cells").textContent = data.occupiedCells;
    document.getElementById("available-cells").textContent = data.availableCells;
    const movements = document.getElementById("movement-rows");
    movements.innerHTML = data.recentMovements.length ? data.recentMovements.map(movement =>
        `<tr><td>${escapeHtml(movement.itemName)}</td><td>${escapeHtml(movement.barcode)}</td><td>${escapeHtml(movement.previousLocation)}</td><td>${escapeHtml(movement.newLocation)}</td></tr>`
    ).join("") : '<tr><td colspan="4" class="empty">No item movements yet.</td></tr>';
    const alerts = document.getElementById("alert-list");
    document.getElementById("alert-count").textContent = data.unauthorizedAlerts.length;
    alerts.innerHTML = data.unauthorizedAlerts.length ? data.unauthorizedAlerts.map(alert =>
        `<div class="alert-entry"><strong>${escapeHtml(alert.itemName)}</strong><span>${escapeHtml(alert.barcode)} · ${escapeHtml(alert.message)}</span></div>`
    ).join("") : '<p class="empty">No unauthorized exit attempts.</p>';
}

document.querySelectorAll(".nav-link").forEach(button => button.addEventListener("click", async () => {
    document.querySelectorAll(".nav-link").forEach(link => link.classList.toggle("active", link === button));
    document.querySelectorAll(".page").forEach(page => page.classList.toggle("active", page.id === `page-${button.dataset.page}`));
    document.getElementById("page-title").textContent = button.textContent.trim();
    notice.hidden = true;
    if (button.dataset.page === "dashboard") {
        try { await loadDashboard(); } catch (error) { showNotice(error.message, true); }
    }
}));

document.getElementById("add-form").addEventListener("submit", async event => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const result = document.getElementById("created-item");
    try {
        const item = await api(`/api/items?name=${encodeURIComponent(form.get("name"))}&quantity=${encodeURIComponent(form.get("quantity"))}`, { method: "POST" });
        result.innerHTML = `<strong>Item allocated successfully</strong><span class="barcode">${escapeHtml(item.barcode)}</span><div class="location-line">Location: ${escapeHtml(locationText(item))} · Quantity: ${item.quantity}</div>`;
        result.hidden = false;
        event.currentTarget.reset();
        showNotice("The item was allocated and its barcode was generated.");
    } catch (error) { showNotice(error.message, true); }
});

document.getElementById("search-form").addEventListener("submit", async event => {
    event.preventDefault();
    const barcode = new FormData(event.currentTarget).get("barcode");
    const result = document.getElementById("search-result");
    try {
        renderItem(await api(`/api/items/${encodeURIComponent(barcode)}`), result);
        showNotice("Item found.");
    } catch (error) { result.hidden = true; showNotice(error.message, true); }
});

document.getElementById("move-lookup-form").addEventListener("submit", async event => {
    event.preventDefault();
    activeMoveBarcode = new FormData(event.currentTarget).get("barcode");
    const current = document.getElementById("move-current");
    const moveForm = document.getElementById("move-form");
    const select = moveForm.elements.cellId;
    try {
        const item = await api(`/api/items/${encodeURIComponent(activeMoveBarcode)}`);
        if (item.status !== "IN_STOCK") throw new Error("Exited items cannot be moved.");
        const cells = await api("/api/cells");
        current.textContent = `Current location: ${locationText(item)} · Quantity: ${item.quantity}`;
        current.hidden = false;
        select.innerHTML = cells.filter(cell => cell.location !== locationText(item) && cell.maximumCapacity - cell.occupiedCapacity >= item.quantity)
            .map(cell => `<option value="${cell.id}">${escapeHtml(cell.location)} (${cell.maximumCapacity - cell.occupiedCapacity} free)</option>`).join("");
        moveForm.hidden = select.options.length === 0;
        if (!select.options.length) showNotice("No other cell has enough capacity for this item.", true);
        else showNotice("Select a destination cell.");
    } catch (error) { current.hidden = true; moveForm.hidden = true; showNotice(error.message, true); }
});

document.getElementById("move-form").addEventListener("submit", async event => {
    event.preventDefault();
    const output = document.getElementById("move-result");
    try {
        const result = await api(`/api/items/${encodeURIComponent(activeMoveBarcode)}/move`, {
            method: "POST", headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ cellId: Number(new FormData(event.currentTarget).get("cellId")) })
        });
        output.innerHTML = `<strong>Movement recorded</strong><div class="location-line">From: ${escapeHtml(result.previousLocation)}</div><div class="location-line">To: ${escapeHtml(result.newLocation)}</div><div class="location-line">Barcode retained: ${escapeHtml(result.barcode)}</div>`;
        output.hidden = false;
        showNotice("Item moved and movement history saved.");
    } catch (error) { showNotice(error.message, true); }
});

document.getElementById("exit-form").addEventListener("submit", async event => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const output = document.getElementById("exit-result");
    try {
        const result = await api("/api/exits", {
            method: "POST", headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ barcode: form.get("barcode"), authorized: form.get("authorized") === "true" })
        });
        output.innerHTML = `<strong>${escapeHtml(result.message)}</strong><div class="location-line">Barcode: ${escapeHtml(result.barcode)}</div>`;
        output.hidden = false;
        showNotice(result.message, !result.accepted);
        await loadDashboard();
    } catch (error) { showNotice(error.message, true); }
});

loadDashboard().catch(error => showNotice(error.message, true));
