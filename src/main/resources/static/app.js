'use strict';

const api = async (url, opcoes = {}) => {
  const resposta = await fetch(url, { headers: { 'Content-Type': 'application/json' }, ...opcoes });
  if (resposta.status === 204) return null;
  const corpo = await resposta.json().catch(() => ({}));
  if (!resposta.ok) {
    const erros = corpo.erros ? ' (' + Object.values(corpo.erros).join('; ') + ')' : '';
    throw new Error((corpo.detail || 'Erro inesperado') + erros);
  }
  return corpo;
};

const el = (id) => document.getElementById(id);
const esc = (t) => String(t ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const data = (iso) => (iso ? new Date(iso + 'T00:00:00').toLocaleDateString('pt-BR') : '—');

function avisar(texto, erro = false) {
  const caixa = el('mensagem');
  caixa.textContent = texto;
  caixa.className = 'mensagem' + (erro ? ' erro' : '');
  caixa.hidden = false;
  clearTimeout(avisar.timer);
  avisar.timer = setTimeout(() => (caixa.hidden = true), 5000);
}

async function carregarVersao() {
  try {
    const info = await api('actuator/info');
    const ambiente = info.ambiente || 'desenvolvimento';
    const selo = el('ambiente');
    selo.textContent = ambiente;
    selo.className = 'ambiente ' + ambiente.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();
    const versao = info.build?.version || 'dev';
    const commit = info.git?.commit?.id?.abbrev || info.git?.commit?.id || '';
    el('build').textContent = 'v' + versao + (commit ? ' · ' + commit : '');
  } catch (e) {
    el('ambiente').textContent = 'indisponível';
  }
}

async function carregarCards() {
  const e = await api('api/estatisticas');
  const itens = [
    ['Títulos', e.titulos], ['Exemplares', e.exemplares], ['Disponíveis', e.exemplaresDisponiveis],
    ['Leitores ativos', e.leitoresAtivos], ['Empréstimos ativos', e.emprestimosAtivos],
    ['Em atraso', e.emprestimosAtrasados], ['Ocupação', e.taxaOcupacao + '%'],
  ];
  el('cards').innerHTML = itens.map(([rotulo, valor]) => `<div class="card"><strong>${esc(valor)}</strong><span>${rotulo}</span></div>`).join('');
}

async function carregarLivros(termo = '') {
  const livros = await api('api/livros' + (termo ? '?termo=' + encodeURIComponent(termo) : ''));
  el('tabela-livros').innerHTML = livros.map((l) => `<tr><td>${esc(l.titulo)}</td><td>${esc(l.autor)}</td><td>${esc(l.isbn)}</td>
    <td>${esc(l.categoria)}</td><td><span class="tag ${l.disponivel ? 'ok' : 'erro'}">${l.exemplaresDisponiveis}/${l.exemplaresTotais}</span></td></tr>`).join('');
  el('sel-livro').innerHTML = '<option value="">Selecione o livro…</option>' + livros.filter((l) => l.disponivel)
    .map((l) => `<option value="${l.id}">${esc(l.titulo)}</option>`).join('');
}

async function carregarLeitores() {
  const leitores = await api('api/leitores');
  el('tabela-leitores').innerHTML = leitores.map((l) => `<tr><td>${esc(l.nome)}</td><td>${esc(l.email)}</td><td>${esc(l.tipo)}</td>
    <td><span class="tag ${l.ativo ? 'ok' : 'erro'}">${l.ativo ? 'Ativo' : 'Inativo'}</span></td></tr>`).join('');
  el('sel-leitor').innerHTML = '<option value="">Selecione o leitor…</option>' + leitores.filter((l) => l.ativo)
    .map((l) => `<option value="${l.id}">${esc(l.nome)} (${esc(l.tipo)})</option>`).join('');
}

async function carregarEmprestimos() {
  const emprestimos = await api('api/emprestimos/ativos');
  el('tabela-emprestimos').innerHTML = emprestimos.map((e) => `<tr><td>${esc(e.tituloLivro)}</td><td>${esc(e.nomeLeitor)}</td>
    <td>${data(e.dataEmprestimo)}</td><td>${data(e.dataPrevistaDevolucao)} ${e.atrasado ? '<span class="tag erro">atrasado</span>' : ''}</td>
    <td><button class="secundario" data-acao="devolucao" data-id="${e.id}">Devolver</button>
        <button class="secundario" data-acao="renovacao" data-id="${e.id}">Renovar</button></td></tr>`).join('')
    || '<tr><td colspan="5">Nenhum empréstimo ativo.</td></tr>';
}

const recarregar = () => Promise.all([carregarCards(), carregarLivros(el('busca').value), carregarLeitores(), carregarEmprestimos()]);

function formJson(form) {
  const dados = Object.fromEntries(new FormData(form).entries());
  for (const chave of ['anoPublicacao', 'exemplares', 'livroId', 'leitorId']) {
    if (chave in dados) dados[chave] = dados[chave] === '' ? null : Number(dados[chave]);
  }
  return JSON.stringify(dados);
}

function aoEnviar(idForm, url, sucesso) {
  el(idForm).addEventListener('submit', async (evento) => {
    evento.preventDefault();
    try {
      await api(url, { method: 'POST', body: formJson(evento.target) });
      evento.target.reset();
      avisar(sucesso);
      await recarregar();
    } catch (erro) {
      avisar(erro.message, true);
    }
  });
}

document.querySelectorAll('.aba').forEach((aba) => aba.addEventListener('click', () => {
  document.querySelectorAll('.aba').forEach((a) => a.classList.toggle('ativa', a === aba));
  document.querySelectorAll('.painel').forEach((p) => (p.hidden = p.id !== 'painel-' + aba.dataset.aba));
}));

el('tabela-emprestimos').addEventListener('click', async (evento) => {
  const botao = evento.target.closest('button[data-acao]');
  if (!botao) return;
  try {
    const r = await api(`api/emprestimos/${botao.dataset.id}/${botao.dataset.acao}`, { method: 'POST' });
    avisar(botao.dataset.acao === 'devolucao'
      ? `Devolução registrada. Multa: R$ ${Number(r.multa).toFixed(2).replace('.', ',')}`
      : `Empréstimo renovado até ${data(r.dataPrevistaDevolucao)}`);
    await recarregar();
  } catch (erro) {
    avisar(erro.message, true);
  }
});

let atraso;
el('busca').addEventListener('input', (e) => { clearTimeout(atraso); atraso = setTimeout(() => carregarLivros(e.target.value), 250); });

aoEnviar('form-livro', 'api/livros', 'Livro cadastrado com sucesso.');
aoEnviar('form-leitor', 'api/leitores', 'Leitor cadastrado com sucesso.');
aoEnviar('form-emprestimo', 'api/emprestimos', 'Empréstimo realizado com sucesso.');

carregarVersao();
recarregar().catch((erro) => avisar(erro.message, true));
