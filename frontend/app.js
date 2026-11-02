const API = 'http://localhost:8080';
const $ = id => document.getElementById(id);
let token = '';

async function request(path, options = {}) {
  const response = await fetch(`${API}${path}`, options);
  const body = await response.json().catch(() => ({}));
  return { response, body };
}

function showResult(code, title, message, good) {
  const box = $('result'); box.className = `result ${good ? 'good' : 'bad'}`;
  $('result-dot').className = `status-dot ${good ? '' : 'bad'}`;
  $('result').querySelector('.result-code').textContent = code;
  $('result').querySelector('strong').textContent = title;
  $('result').querySelector('p').textContent = message;
  $('response').textContent = JSON.stringify(message, null, 2);
  $('check-sign').textContent = good ? '✓' : '✕';
  $('check-encrypt').textContent = token ? '✓' : '—';
  $('check-claims').textContent = good ? '✓' : '✕';
}

async function generate(seconds = $('seconds').value) {
  const result = await request(`/demo/pedidos/token?seconds=${seconds}`, { method: 'POST' });
  token = result.body.token || '';
  $('token').value = token;
  $('response').textContent = 'Token generado. Ahora envíalo a Facturación.';
  $('result').className = 'result neutral'; $('result-dot').className = 'status-dot neutral';
  $('check-sign').textContent = '✓'; $('check-encrypt').textContent = '✓'; $('check-claims').textContent = '—';
}

async function send(value = token) {
  if (!value) { await generate(); value = token; }
  const { response, body } = await request('/demo/facturacion/procesar', { method: 'POST', headers: { Authorization: `Bearer ${value}` } });
  showResult(response.status, response.ok ? 'Solicitud aceptada' : 'Solicitud rechazada', body, response.status === 201);
  $('response').textContent = JSON.stringify(body, null, 2);
}

$('generate').onclick = () => generate();
$('valid').onclick = () => send();
$('tamper').onclick = () => { if (!token) return generate().then(() => send(`${token}x`)); send(`${token}x`); };
$('expired').onclick = async () => { await generate(1); setTimeout(() => send(token), 1500); };
