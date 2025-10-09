window.app = (function(){
function countdown(targetDate, el){
function tick(){
const sec = Math.max(0, Math.floor((targetDate - new Date())/1000));
const m = String(Math.floor(sec/60)).padStart(2,'0');
const s = String(sec%60).padStart(2,'0');
el.textContent = m+":"+s;
if(sec>0) setTimeout(tick, 500);
}
tick();
}


async function startBooking(ev, slotId, serviceId){
ev.preventDefault();
// เด้งไปหน้า confirm พร้อม slotId/serviceId
window.location.href = `/booking/confirm?slotId=${slotId}&serviceId=${serviceId}`;
return false;
}


async function cancelBooking(ev){
const id = ev.currentTarget.getAttribute('data-id');
const phone = new URLSearchParams(window.location.search).get('phone') || prompt('ใส่เบอร์ที่ใช้จองเพื่อยืนยัน');
if(!confirm('ยืนยันการยกเลิกคิวนี้?')) return;
const r = await fetch(`/my-bookings/${id}/cancel`, {
method:'POST', headers:{'Content-Type':'application/json'},
body: JSON.stringify({phone, reason: 'cancel-via-ui'})
});
if(r.ok){
location.reload();
} else {
alert('ยกเลิกไม่สำเร็จ');
}
}


return {countdown, startBooking, cancelBooking};
})();