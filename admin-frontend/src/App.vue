<template>
  <!-- 登录页 -->
  <div v-if="!token" class="login-wrap">
    <div class="login-card">
      <div class="hd">
        <div class="t">商城总后台</div>
        <div class="small">最高管理权限 · 商品治理</div>
      </div>
      <div class="bd">
        <div class="row">
          <input class="input" v-model="login.username" placeholder="管理员账号" />
        </div>
        <div style="height:10px"></div>
        <div class="row">
          <input class="input" v-model="login.password" placeholder="密码" type="password" @keydown.enter="doLogin"/>
        </div>
        <div style="height:12px"></div>
        <div class="row">
          <button class="btn primary" style="flex:1" @click="doLogin">登录</button>
        </div>
        <div v-if="err" class="msg">{{ err }}</div>
      </div>
    </div>
  </div>

  <!-- 后台布局 -->
  <div v-else class="layout">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-badge">A</div>
        <div>
          <div>Admin Console</div>
          <div class="small" style="color:rgba(199,210,254,.8)">Super Admin</div>
        </div>
      </div>

      <nav class="nav">
        <a class="active" href="javascript:void(0)">
          商品管理 <span class="small">Items</span>
        </a>
        <a href="javascript:void(0)" style="opacity:.6; cursor:not-allowed">
          订单管理 <span class="small">Soon</span>
        </a>
        <a href="javascript:void(0)" style="opacity:.6; cursor:not-allowed">
          用户管理 <span class="small">Soon</span>
        </a>
      </nav>
    </aside>

    <main class="content">
      <header class="topbar">
        <div>
          <div class="title">商品管理</div>
          <div class="meta">支持搜索 / 状态筛选 / 强制已售 / 删除</div>
        </div>
        <div class="topbar-actions">
          <span class="small">已登录：admin</span>
          <button class="btn" @click="logout">退出</button>
        </div>
      </header>

      <div class="container">
        <section class="card">
          <div class="card-hd">
            <h3>筛选条件</h3>
            <div class="small">接口：GET /api/admin/items</div>
          </div>
          <div class="card-bd">
            <div class="filters">
              <input class="input" v-model="query.q" placeholder="关键词（标题）搜索" @keydown.enter="load"/>
              <select class="select" v-model="query.status">
                <option value="">全部状态</option>
                <option value="AVAILABLE">在售</option>
                <option value="RESERVED">已锁定</option>
                <option value="SOLD">已售</option>
              </select>
              <button class="btn primary" @click="load">查询</button>
              <button class="btn" @click="reset">重置</button>
            </div>
            <div v-if="err" class="msg">{{ err }}</div>
          </div>
        </section>

        <section class="card">
          <div class="card-hd">
            <h3>商品列表</h3>
            <div class="small">共 {{ items.length }} 条</div>
          </div>

          <div class="card-bd" style="padding:0">
            <table class="table">
              <thead>
              <tr>
                <th style="width:70px">ID</th>
                <th>标题</th>
                <th style="width:110px">价格</th>
                <th style="width:120px">状态</th>
                <th style="width:160px">卖家</th>
                <th style="width:220px; text-align:right">操作</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="it in items" :key="it.id">
                <td>{{ it.id }}</td>
                <td>
                  <div style="font-weight:700">{{ it.title }}</div>
                  <div class="small" style="margin-top:4px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; max-width:520px;">
                    {{ it.description || '（无描述）' }}
                  </div>
                </td>
                <td style="font-weight:800; color:#d03050">¥ {{ it.price }}</td>
                <td>
                    <span class="badge" :class="statusClass(it.status)">
                      {{ statusText(it.status) }}
                    </span>
                </td>
                <td>{{ it.seller?.username || '-' }}</td>
                <td>
                  <div class="right">
                    <button class="btn" @click="markSold(it.id)" :disabled="it.status==='SOLD'">强制已售</button>
                    <button class="btn danger" @click="del(it.id)">删除</button>
                  </div>
                </td>
              </tr>
              <tr v-if="items.length===0">
                <td colspan="6" style="padding:18px; color:var(--muted);">暂无数据</td>
              </tr>
              </tbody>
            </table>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { adminLogin, adminListItems, adminMarkSold, adminDeleteItem } from './api'

const token = ref(localStorage.getItem('admin_token') || '')
const err = ref('')

const login = reactive({ username: 'admin', password: '' })
const query = reactive({ q: '', status: '' })
const items = ref([])

function statusText(s){
  if (s === 'AVAILABLE') return '在售'
  if (s === 'RESERVED') return '已锁定'
  if (s === 'SOLD') return '已售'
  return s || '-'
}
function statusClass(s){
  if (s === 'AVAILABLE') return 'ok'
  if (s === 'RESERVED') return 'warn'
  if (s === 'SOLD') return 'gray'
  return ''
}

async function doLogin() {
  err.value = ''
  try {
    const res = await adminLogin(login.username, login.password)
    if (!res.success) throw new Error(res.message || 'login failed')
    localStorage.setItem('admin_token', res.data.token)
    token.value = res.data.token
    await load()
  } catch (e) {
    err.value = e.message
  }
}

function logout() {
  localStorage.removeItem('admin_token')
  token.value = ''
  items.value = []
}

function reset(){
  query.q = ''
  query.status = ''
  load()
}

async function load() {
  err.value = ''
  try {
    const res = await adminListItems({
      q: query.q || undefined,
      status: query.status || undefined
    })
    if (!res.success) throw new Error(res.message || 'load failed')
    items.value = res.data
  } catch (e) {
    err.value = e.message
  }
}

async function markSold(id) {
  err.value = ''
  try {
    const res = await adminMarkSold(id)
    if (!res.success) throw new Error(res.message || 'mark sold failed')
    await load()
  } catch (e) {
    err.value = e.message
  }
}

async function del(id) {
  err.value = ''
  try {
    const res = await adminDeleteItem(id)
    if (!res.success) throw new Error(res.message || 'delete failed')
    await load()
  } catch (e) {
    err.value = e.message
  }
}
</script>