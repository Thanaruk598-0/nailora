(() => {
  // props
  const propsEl        = document.getElementById('props') || {};
  const slotId         = Number(propsEl.dataset?.slotid || 0);
  const serviceId      = Number(propsEl.dataset?.serviceid || 0);
  const publishableKey = (propsEl.dataset?.publishablekey || '').trim();

  // elements
  const form         = document.getElementById('f');
  const btnCreate    = document.getElementById('btnCreate');
  const payBox       = document.getElementById('pay');
  const btnPay       = document.getElementById('btnPay');
  const msgEl        = document.getElementById('msg');
  const addOnChecks  = Array.from(document.querySelectorAll('.addOnCheck')) || [];

  const elSvc   = document.getElementById('priceService');
  const elAdd   = document.getElementById('priceAddOn');
  const elTotal = document.getElementById('priceTotal');
  const elDep   = document.getElementById('priceDeposit');

  if (form) form.addEventListener('submit', e => e.preventDefault());

  let stripe, elements, cardEl, bookingId, phoneVal;
  let selectedAddons = [];

  const fmt = (n) => (n == null ? '—' :
    Number(n).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }));

  const setMsg = (t) => { if (msgEl) msgEl.textContent = t || ''; };

  async function getJSON(url) {
    const r = await fetch(url, { method: 'GET' });
    if (!r.ok) {
      const txt = await r.text().catch(()=>r.statusText);
      throw new Error(txt || 'Request failed');
    }
    return r.json();
  }

  async function postJSON(url, body) {
    const r = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body || {})
    });
    const j = await r.json().catch(()=> ({}));
    if (!r.ok) {
      throw new Error(j.message || r.statusText || 'Request failed');
    }
    return j;
  }

  function collectAddOnIds() {
    return addOnChecks.filter(ch => ch.checked).map(ch => Number(ch.dataset.id));
  }

  async function refreshQuote() {
    try {
      selectedAddons = collectAddOnIds();
      const qs = new URLSearchParams({
        serviceId: String(serviceId),
        addOnIds: selectedAddons.join(',') || ''
      });
      const q = await getJSON('/booking/price-quote?' + qs.toString());
      // expected:
      // { servicePrice, addOnPrice, totalPrice, depositAmount }
      elSvc && (elSvc.textContent   = fmt(q.servicePrice));
      elAdd && (elAdd.textContent   = fmt(q.addOnPrice));
      elTotal && (elTotal.textContent = fmt(q.totalPrice));
      elDep && (elDep.textContent   = fmt(q.depositAmount));
    } catch (e) {
      console.warn('[confirm] quote error', e);
    }
  }

  // init quote + re-calc when toggle addons
  refreshQuote();
  addOnChecks.forEach(ch => ch.addEventListener('change', refreshQuote));

  // 1) สร้างใบจอง
  if (btnCreate) {
    btnCreate.addEventListener('click', async () => {
      if (!slotId) { alert('slotId หายไปจากหน้า'); return; }
      btnCreate.disabled = true;
      try {
        const f = form;
        const payload = {
          timeSlotId: slotId,
          customerName: (f.customerName.value || '').trim(),
          phone: (f.phone.value || '').trim(),
          note: (f.note.value || '').trim(),
          addOnIds: collectAddOnIds()
        };
        if (!payload.customerName || !payload.phone) {
          throw new Error('กรอกชื่อและเบอร์โทรให้ครบ');
        }
        phoneVal = payload.phone;

        const data = await postJSON('/booking/create', payload);
        bookingId = data.bookingId;

        if (!publishableKey) {
          setMsg('สร้างใบจองสำเร็จ (ยังไม่ตั้งค่า Stripe publishable key)');
          return;
        }

        stripe   = Stripe(publishableKey);
        elements = stripe.elements();
        cardEl   = elements.create('card', { hidePostalCode: true });
        cardEl.mount('#card-element');
        payBox.style.display = 'block';
        setMsg('กรอกข้อมูลบัตรเพื่อชำระมัดจำ');

      } catch (e) {
        console.error('[confirm] create error', e);
        alert('สร้างใบจองไม่สำเร็จ: ' + e.message);
        btnCreate.disabled = false;
      }
    });
  }

  // 2) ชำระมัดจำ
  if (btnPay) {
    btnPay.addEventListener('click', async () => {
      if (!bookingId) { alert('ยังไม่มีหมายเลขการจอง'); return; }
      if (!stripe || !cardEl) { alert('Stripe ยังไม่พร้อม'); return; }

      btnPay.disabled = true;
      try {
        const r = await fetch('/payments/intent?bookingId=' + encodeURIComponent(bookingId), { method: 'POST' });
        const j = await r.json().catch(()=> ({}));
        if (!r.ok) throw new Error(j.message || 'สร้าง PaymentIntent ไม่สำเร็จ');

        const { clientSecret } = j;
        const { paymentIntent, error } = await stripe.confirmCardPayment(clientSecret, {
          payment_method: { card: cardEl }
        });

        if (error) throw error;

        if (paymentIntent && paymentIntent.status === 'succeeded') {
          setMsg('ชำระสำเร็จ กำลังพาไปหน้าเช็คคิว...');
          setTimeout(() => {
            location.href = '/my-bookings?phone=' + encodeURIComponent(phoneVal || '');
          }, 800);
        } else {
          throw new Error('สถานะการชำระ: ' + (paymentIntent && paymentIntent.status));
        }

      } catch (e) {
        console.error('[confirm] pay error', e);
        setMsg('ชำระไม่สำเร็จ: ' + (e.message || ''));
        btnPay.disabled = false;
      }
    });
  }
})();
