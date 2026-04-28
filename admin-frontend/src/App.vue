<template>
  <div v-if="!token && authMode === 'login'" class="login-wrap">
    <div class="login-card">
      <div class="hd">
        <div class="t">二手交易平台后台</div>
        <div class="small">Admin Console · 权限分级管理</div>
      </div>

      <div class="bd">
        <div class="row">
          <input class="input" v-model="login.username" placeholder="管理员账号" />
        </div>

        <div style="height:10px"></div>

        <div class="row">
          <input
            class="input"
            v-model="login.password"
            placeholder="密码"
            type="password"
            @keydown.enter="doLogin"
          />
        </div>

        <div style="height:12px"></div>

        <div class="row">
          <button class="btn primary" style="flex:1" @click="doLogin">登录后台</button>
        </div>

        <div style="height:10px"></div>

        <div class="row">
          <button class="btn" style="flex:1" @click="goRegister">注册管理员账号</button>
        </div>

        <div class="hint">
          固定超级管理员：admin / admin123456；其他管理员可直接注册，无需邀请码。
        </div>

        <div v-if="err" class="msg">{{ err }}</div>
      </div>
    </div>
  </div>

  <div v-else-if="!token && authMode === 'register'" class="login-wrap">
    <div class="login-card register-card">
      <div class="hd">
        <div class="t">注册管理员账号</div>
        <div class="small">无需邀请码 · 不能注册超级管理员</div>
      </div>

      <div class="bd">
        <div class="row">
          <input class="input" v-model="registerForm.username" placeholder="管理员账号，不能使用 admin" />
        </div>

        <div style="height:10px"></div>

        <div class="row">
          <input class="input" v-model="registerForm.nickname" placeholder="管理员昵称，可不填" />
        </div>

        <div style="height:10px"></div>

        <div class="row">
          <input
            class="input"
            v-model="registerForm.password"
            placeholder="密码，至少 6 位"
            type="password"
          />
        </div>

        <div style="height:10px"></div>

        <div class="row">
          <input
            class="input"
            v-model="registerForm.confirmPassword"
            placeholder="再次输入密码"
            type="password"
            @keydown.enter="doAdminRegister"
          />
        </div>

        <div style="height:10px"></div>

        <div class="row">
          <select class="select register-select" v-model="registerForm.role">
            <option v-for="r in registerRoleOptions" :key="r.value" :value="r.value">
              {{ r.label }}
            </option>
          </select>
        </div>

        <div style="height:12px"></div>

        <div class="row">
          <button class="btn primary" style="flex:1" @click="doAdminRegister">注册并进入后台</button>
        </div>

        <div style="height:10px"></div>

        <div class="row">
          <button class="btn" style="flex:1" @click="goLogin">返回登录</button>
        </div>

        <div class="hint">
          SUPER_ADMIN 只允许固定账号 admin 拥有；注册页面只能创建普通管理员或分工管理员。
        </div>

        <div v-if="err" class="msg">{{ err }}</div>
      </div>
    </div>
  </div>

  <div v-else class="layout">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-badge">A</div>
        <div>
          <div>Admin Console</div>
          <div class="small" style="color:rgba(199,210,254,.8)">
            {{ currentUsername }} · {{ currentRole }}
          </div>
        </div>
      </div>

      <nav class="nav">
        <a
          href="javascript:void(0)"
          :class="{ active: activeTab === 'dashboard' }"
          @click="switchTab('dashboard')"
        >
          数据统计 <span class="small">Stats</span>
        </a>

        <a
          v-if="canItems"
          href="javascript:void(0)"
          :class="{ active: activeTab === 'items' }"
          @click="switchTab('items')"
        >
          商品管理 <span class="small">Items</span>
        </a>

        <a
          v-if="canOrders"
          href="javascript:void(0)"
          :class="{ active: activeTab === 'orders' }"
          @click="switchTab('orders')"
        >
          订单管理 <span class="small">Orders</span>
        </a>

        <a
          v-if="canUsers"
          href="javascript:void(0)"
          :class="{ active: activeTab === 'users' }"
          @click="switchTab('users')"
        >
          用户管理 <span class="small">Users</span>
        </a>

        <a
          v-if="canLogs"
          href="javascript:void(0)"
          :class="{ active: activeTab === 'logs' }"
          @click="switchTab('logs')"
        >
          操作日志 <span class="small">Logs</span>
        </a>
      </nav>
    </aside>

    <main class="content">
      <header class="topbar">
        <div>
          <div class="title">{{ pageTitle }}</div>
          <div class="meta">{{ pageMeta }}</div>
        </div>

        <div class="topbar-actions">
          <button class="btn" @click="loadCurrentTab">刷新</button>
          <button class="btn danger" @click="logout">退出</button>
        </div>
      </header>

      <div class="container">
        <div v-if="err" class="msg global-msg">{{ err }}</div>

        <section v-if="activeTab === 'dashboard'" class="grid-cards">
          <div class="stat-card">
            <div class="stat-label">用户总数</div>
            <div class="stat-num">{{ stats.userTotal ?? '-' }}</div>
            <div class="small">封禁用户：{{ stats.userBanned ?? '-' }}</div>
          </div>

          <div class="stat-card">
            <div class="stat-label">商品总数</div>
            <div class="stat-num">{{ stats.itemTotal ?? '-' }}</div>
            <div class="small">已删除：{{ stats.itemDeleted ?? '-' }}</div>
          </div>

          <div class="stat-card">
            <div class="stat-label">订单总数</div>
            <div class="stat-num">{{ stats.orderTotal ?? '-' }}</div>
            <div class="small">待支付：{{ stats.orderCreated ?? '-' }}</div>
          </div>

          <div class="stat-card">
            <div class="stat-label">已售商品</div>
            <div class="stat-num">{{ stats.itemSold ?? '-' }}</div>
            <div class="small">下架商品：{{ stats.itemOffShelf ?? '-' }}</div>
          </div>

          <div class="stat-card wide">
            <div class="stat-label">商品状态</div>
            <div class="chips">
              <span class="badge ok">在售 {{ stats.itemAvailable ?? '-' }}</span>
              <span class="badge warn">锁定 {{ stats.itemReserved ?? '-' }}</span>
              <span class="badge gray">已售 {{ stats.itemSold ?? '-' }}</span>
              <span class="badge dark">下架 {{ stats.itemOffShelf ?? '-' }}</span>
            </div>
          </div>

          <div class="stat-card wide">
            <div class="stat-label">订单状态</div>
            <div class="chips">
              <span class="badge warn">待支付 {{ stats.orderCreated ?? '-' }}</span>
              <span class="badge ok">已支付 {{ stats.orderPaid ?? '-' }}</span>
              <span class="badge gray">已取消 {{ stats.orderCanceled ?? '-' }}</span>
            </div>
          </div>
        </section>

        <section v-if="activeTab === 'items'" class="card">
          <div class="card-hd">
            <h3>商品筛选</h3>
            <div class="small">GET /api/admin/items</div>
          </div>

          <div class="card-bd">
            <div class="filters">
              <input class="input" v-model="itemQuery.q" placeholder="标题 / 描述 / 卖家" @keydown.enter="loadItems" />

              <select class="select" v-model="itemQuery.status">
                <option value="">全部状态</option>
                <option value="AVAILABLE">在售</option>
                <option value="RESERVED">已锁定</option>
                <option value="SOLD">已售</option>
                <option value="OFF_SHELF">已下架</option>
              </select>

              <label class="check">
                <input type="checkbox" v-model="itemQuery.includeDeleted" />
                包含已删除
              </label>

              <button class="btn primary" @click="loadItems">查询</button>
              <button class="btn" @click="resetItems">重置</button>
            </div>
          </div>
        </section>

        <section v-if="activeTab === 'items'" class="card">
          <div class="card-hd">
            <h3>商品列表</h3>
            <div class="small">共 {{ items.length }} 条</div>
          </div>

          <div class="card-bd table-wrap">
            <table class="table">
              <thead>
              <tr>
                <th style="width:70px">ID</th>
                <th>商品信息</th>
                <th style="width:100px">价格</th>
                <th style="width:110px">状态</th>
                <th style="width:140px">卖家</th>
                <th style="width:110px">订单关联</th>
                <th style="width:110px">删除状态</th>
                <th style="width:170px">发布时间</th>
                <th style="width:250px; text-align:right">操作</th>
              </tr>
              </thead>

              <tbody>
              <tr v-for="it in items" :key="it.id">
                <td>{{ it.id }}</td>

                <td>
                  <div class="main-text">{{ it.title }}</div>
                  <div class="small ellipsis">{{ it.description || '（无描述）' }}</div>
                  <div class="small">板块：{{ it.board || '-' }}</div>
                </td>

                <td class="price">¥ {{ it.price }}</td>

                <td>
                  <span class="badge" :class="itemStatusClass(it.status)">
                    {{ itemStatusText(it.status) }}
                  </span>
                </td>

                <td>
                  <div>{{ it.seller?.username || '-' }}</div>
                  <div class="small">{{ it.seller?.nickname || '' }}</div>
                </td>

                <td>
                  <span class="badge" :class="it.hasOrder ? 'warn' : 'ok'">
                    {{ it.hasOrder ? `有订单 ${it.orderCount}` : '无订单' }}
                  </span>
                </td>

                <td>
                  <span class="badge" :class="it.deleted ? 'gray' : 'ok'">
                    {{ it.deleted ? '已删除' : '正常' }}
                  </span>
                  <div class="small">{{ it.deleteCategory || '' }}</div>
                </td>

                <td>{{ fmtTime(it.createdAt) }}</td>

                <td>
                  <div class="right">
                    <button class="btn" @click="offShelfItem(it)" :disabled="it.deleted || it.status === 'OFF_SHELF'">
                      下架
                    </button>
                    <button class="btn" @click="markSold(it)" :disabled="it.deleted || it.status === 'SOLD'">
                      已售
                    </button>
                    <button class="btn danger" @click="deleteItem(it)" :disabled="it.deleted">
                      删除
                    </button>
                  </div>
                </td>
              </tr>

              <tr v-if="items.length === 0">
                <td colspan="9" class="empty">暂无商品数据</td>
              </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="activeTab === 'orders'" class="card">
          <div class="card-hd">
            <h3>订单筛选</h3>
            <div class="small">GET /api/admin/orders</div>
          </div>

          <div class="card-bd">
            <div class="filters">
              <input class="input" v-model="orderQuery.q" placeholder="商品 / 买家 / 卖家" @keydown.enter="loadOrders" />

              <select class="select" v-model="orderQuery.status">
                <option value="">全部状态</option>
                <option value="CREATED">待支付</option>
                <option value="PAID">已支付</option>
                <option value="CANCELED">已取消</option>
              </select>

              <button class="btn primary" @click="loadOrders">查询</button>
              <button class="btn" @click="resetOrders">重置</button>
            </div>
          </div>
        </section>

        <section v-if="activeTab === 'orders'" class="card">
          <div class="card-hd">
            <h3>订单列表</h3>
            <div class="small">共 {{ orders.length }} 条</div>
          </div>

          <div class="card-bd table-wrap">
            <table class="table">
              <thead>
              <tr>
                <th style="width:70px">ID</th>
                <th>商品</th>
                <th style="width:120px">金额</th>
                <th style="width:130px">订单状态</th>
                <th style="width:140px">买家</th>
                <th style="width:140px">卖家</th>
                <th style="width:170px">创建时间</th>
                <th style="width:240px; text-align:right">处理</th>
              </tr>
              </thead>

              <tbody>
              <tr v-for="o in orders" :key="o.id">
                <td>{{ o.id }}</td>
                <td>
                  <div class="main-text">{{ o.item?.title || '-' }}</div>
                  <div class="small">商品ID：{{ o.item?.id || '-' }}</div>
                </td>
                <td class="price">¥ {{ o.item?.price || '-' }}</td>
                <td>
                  <span class="badge" :class="orderStatusClass(o.status)">
                    {{ orderStatusText(o.status) }}
                  </span>
                </td>
                <td>{{ o.buyer?.username || '-' }}</td>
                <td>{{ o.item?.seller?.username || '-' }}</td>
                <td>{{ fmtTime(o.createdAt) }}</td>
                <td>
                  <div class="right">
                    <select class="select mini" v-model="o._newStatus">
                      <option value="CREATED">待支付</option>
                      <option value="PAID">已支付</option>
                      <option value="CANCELED">已取消</option>
                    </select>
                    <button class="btn primary" @click="updateOrderStatus(o)">修改</button>
                  </div>
                </td>
              </tr>

              <tr v-if="orders.length === 0">
                <td colspan="8" class="empty">暂无订单数据</td>
              </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="activeTab === 'users'" class="card">
          <div class="card-hd">
            <h3>用户筛选</h3>
            <div class="small">GET /api/admin/users</div>
          </div>

          <div class="card-bd">
            <div class="filters">
              <input class="input" v-model="userQuery.q" placeholder="用户名 / 昵称" @keydown.enter="loadUsers" />

              <select class="select" v-model="userQuery.role">
                <option value="">全部角色</option>
                <option value="USER">USER</option>
                <option value="ADMIN">ADMIN</option>
                <option value="SUPER_ADMIN">SUPER_ADMIN</option>
                <option value="ITEM_ADMIN">ITEM_ADMIN</option>
                <option value="ORDER_ADMIN">ORDER_ADMIN</option>
                <option value="USER_ADMIN">USER_ADMIN</option>
                <option value="SYSTEM_ADMIN">SYSTEM_ADMIN</option>
              </select>

              <select class="select" v-model="userQuery.status">
                <option value="">全部状态</option>
                <option value="NORMAL">正常</option>
                <option value="BANNED">封禁</option>
              </select>

              <button class="btn primary" @click="loadUsers">查询</button>
              <button class="btn" @click="resetUsers">重置</button>
            </div>
          </div>
        </section>

        <section v-if="activeTab === 'users'" class="card">
          <div class="card-hd">
            <h3>用户列表</h3>
            <div class="small">共 {{ users.length }} 条</div>
          </div>

          <div class="card-bd table-wrap">
            <table class="table">
              <thead>
              <tr>
                <th style="width:70px">ID</th>
                <th>用户</th>
                <th style="width:150px">角色</th>
                <th style="width:110px">状态</th>
                <th style="width:110px">发布商品</th>
                <th style="width:110px">买入订单</th>
                <th style="width:110px">卖出订单</th>
                <th style="width:170px">注册时间</th>
                <th style="width:280px; text-align:right">操作</th>
              </tr>
              </thead>

              <tbody>
              <tr v-for="u in users" :key="u.id">
                <td>{{ u.id }}</td>
                <td>
                  <div class="main-text">{{ u.username }}</div>
                  <div class="small">{{ u.nickname || '-' }}</div>
                </td>
                <td>
                  <select
                    class="select mini role-select"
                    v-model="u._role"
                    :disabled="u.username === 'admin'"
                  >
                    <option value="USER">USER</option>
                    <option value="ADMIN">ADMIN</option>
                    <option value="SUPER_ADMIN" disabled>SUPER_ADMIN（固定：admin）</option>
                    <option value="ITEM_ADMIN">ITEM_ADMIN</option>
                    <option value="ORDER_ADMIN">ORDER_ADMIN</option>
                    <option value="USER_ADMIN">USER_ADMIN</option>
                    <option value="SYSTEM_ADMIN">SYSTEM_ADMIN</option>
                  </select>
                </td>
                <td>
                  <span class="badge" :class="u.status === 'BANNED' ? 'danger-badge' : 'ok'">
                    {{ u.status === 'BANNED' ? '封禁' : '正常' }}
                  </span>
                </td>
                <td>{{ u.itemCount }}</td>
                <td>{{ u.buyOrderCount }}</td>
                <td>{{ u.sellOrderCount }}</td>
                <td>{{ fmtTime(u.createdAt) }}</td>
                <td>
                  <div class="right">
                    <button class="btn" @click="saveUserRole(u)" :disabled="u.username === 'admin'">
                      保存角色
                    </button>
                    <button
                      class="btn danger"
                      v-if="u.status !== 'BANNED'"
                      @click="changeUserStatus(u, 'BANNED')"
                      :disabled="u.username === 'admin'"
                    >
                      封禁
                    </button>
                    <button
                      class="btn"
                      v-else
                      @click="changeUserStatus(u, 'NORMAL')"
                    >
                      解封
                    </button>
                  </div>
                </td>
              </tr>

              <tr v-if="users.length === 0">
                <td colspan="9" class="empty">暂无用户数据</td>
              </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="activeTab === 'logs'" class="card">
          <div class="card-hd">
            <h3>操作日志</h3>
            <div class="small">GET /api/admin/logs</div>
          </div>

          <div class="card-bd">
            <div class="filters">
              <input class="input" v-model="logQuery.q" placeholder="管理员 / 动作 / 对象 / 原因" @keydown.enter="loadLogs" />
              <button class="btn primary" @click="loadLogs">查询</button>
              <button class="btn" @click="resetLogs">重置</button>
            </div>
          </div>

          <div class="card-bd table-wrap">
            <table class="table">
              <thead>
              <tr>
                <th style="width:70px">ID</th>
                <th style="width:140px">管理员</th>
                <th style="width:170px">动作</th>
                <th style="width:120px">对象</th>
                <th>内容</th>
                <th>原因</th>
                <th style="width:170px">时间</th>
              </tr>
              </thead>

              <tbody>
              <tr v-for="l in logs" :key="l.id">
                <td>{{ l.id }}</td>
                <td>{{ l.adminUsername }}</td>
                <td><span class="badge dark">{{ l.action }}</span></td>
                <td>{{ l.targetType }} #{{ l.targetId }}</td>
                <td>{{ l.detail }}</td>
                <td>{{ l.reason }}</td>
                <td>{{ fmtTime(l.createdAt) }}</td>
              </tr>

              <tr v-if="logs.length === 0">
                <td colspan="7" class="empty">暂无操作日志</td>
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
import { computed, onMounted, reactive, ref } from 'vue'
import {
  adminDeleteItem,
  adminListItems,
  adminListLogs,
  adminListOrders,
  adminListUsers,
  adminLogin,
  adminRegister,
  adminMarkSold,
  adminOffShelfItem,
  adminStats,
  adminUpdateOrderStatus,
  adminUpdateUser
} from './api'

const token = ref(localStorage.getItem('admin_token') || '')
const authMode = ref('login')
const currentRole = ref(localStorage.getItem('admin_role') || '')
const currentUsername = ref(localStorage.getItem('admin_username') || '')
const err = ref('')

const login = reactive({
  username: 'admin',
  password: ''
})

const registerForm = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  role: 'ADMIN'
})

const activeTab = ref('dashboard')

const stats = reactive({})
const items = ref([])
const orders = ref([])
const users = ref([])
const logs = ref([])

const itemQuery = reactive({
  q: '',
  status: '',
  includeDeleted: false
})

const orderQuery = reactive({
  q: '',
  status: ''
})

const userQuery = reactive({
  q: '',
  role: '',
  status: ''
})

const logQuery = reactive({
  q: ''
})

const adminRoles = ['ADMIN', 'SUPER_ADMIN', 'ITEM_ADMIN', 'ORDER_ADMIN', 'USER_ADMIN', 'SYSTEM_ADMIN']

const registerRoleOptions = [
  { value: 'ADMIN', label: 'ADMIN（普通管理员）' },
  { value: 'ITEM_ADMIN', label: 'ITEM_ADMIN（商品管理员）' },
  { value: 'ORDER_ADMIN', label: 'ORDER_ADMIN（订单管理员）' },
  { value: 'USER_ADMIN', label: 'USER_ADMIN（用户管理员）' },
  { value: 'SYSTEM_ADMIN', label: 'SYSTEM_ADMIN（系统/日志管理员）' }
]

const canItems = computed(() => ['ADMIN', 'SUPER_ADMIN', 'ITEM_ADMIN'].includes(currentRole.value))
const canOrders = computed(() => ['ADMIN', 'SUPER_ADMIN', 'ORDER_ADMIN'].includes(currentRole.value))
const canUsers = computed(() => ['ADMIN', 'SUPER_ADMIN', 'USER_ADMIN'].includes(currentRole.value))
const canLogs = computed(() => ['ADMIN', 'SUPER_ADMIN', 'SYSTEM_ADMIN'].includes(currentRole.value))

const pageTitle = computed(() => {
  if (activeTab.value === 'dashboard') return '数据统计'
  if (activeTab.value === 'items') return '商品管理'
  if (activeTab.value === 'orders') return '订单管理'
  if (activeTab.value === 'users') return '用户管理'
  if (activeTab.value === 'logs') return '操作日志'
  return '后台管理'
})

const pageMeta = computed(() => {
  if (activeTab.value === 'dashboard') return '平台总览：用户、商品、订单统计'
  if (activeTab.value === 'items') return '支持商品查询、下架、标记已售、删除业务约束'
  if (activeTab.value === 'orders') return '支持订单查询、状态查看、异常订单处理'
  if (activeTab.value === 'users') return '支持用户查看、封禁、解封、角色分配'
  if (activeTab.value === 'logs') return '记录管理员操作人、时间、对象、内容和原因'
  return ''
})

function fmtTime(t) {
  if (!t) return '-'
  return String(t).replace('T', ' ').replace('Z', '').slice(0, 19)
}

function itemStatusText(s) {
  if (s === 'AVAILABLE') return '在售'
  if (s === 'RESERVED') return '已锁定'
  if (s === 'SOLD') return '已售'
  if (s === 'OFF_SHELF') return '已下架'
  return s || '-'
}

function itemStatusClass(s) {
  if (s === 'AVAILABLE') return 'ok'
  if (s === 'RESERVED') return 'warn'
  if (s === 'SOLD') return 'gray'
  if (s === 'OFF_SHELF') return 'dark'
  return ''
}

function orderStatusText(s) {
  if (s === 'CREATED') return '待支付'
  if (s === 'PAID') return '已支付'
  if (s === 'CANCELED') return '已取消'
  return s || '-'
}

function orderStatusClass(s) {
  if (s === 'CREATED') return 'warn'
  if (s === 'PAID') return 'ok'
  if (s === 'CANCELED') return 'gray'
  return ''
}

function getReason(title) {
  const reason = window.prompt(`${title}\n请输入操作原因：`)
  if (reason === null) return null
  if (!reason.trim()) {
    alert('重要操作必须填写原因')
    return null
  }
  return reason.trim()
}

function saveAdminSession(data) {
  localStorage.setItem('admin_token', data.token)
  localStorage.setItem('admin_role', data.role)
  localStorage.setItem('admin_username', data.username)

  token.value = data.token
  currentRole.value = data.role
  currentUsername.value = data.username
}

function goRegister() {
  err.value = ''
  authMode.value = 'register'
}

function goLogin() {
  err.value = ''
  authMode.value = 'login'
}

async function doLogin() {
  err.value = ''
  try {
    const res = await adminLogin(login.username, login.password)
    if (!res.success) throw new Error(res.message || '登录失败')

    if (!adminRoles.includes(res.data.role)) {
      throw new Error('当前账号不是管理员账号，不能进入后台')
    }

    saveAdminSession(res.data)
    await loadStats()
  } catch (e) {
    err.value = e.message
  }
}

async function doAdminRegister() {
  err.value = ''

  const username = registerForm.username.trim()
  const password = registerForm.password
  const confirmPassword = registerForm.confirmPassword

  if (!username) {
    err.value = '请输入管理员账号'
    return
  }

  if (username.toLowerCase() === 'admin') {
    err.value = 'admin 是固定超级管理员账号，不能用于注册'
    return
  }

  if (!password || password.length < 6) {
    err.value = '密码至少需要 6 位'
    return
  }

  if (password !== confirmPassword) {
    err.value = '两次输入的密码不一致'
    return
  }

  try {
    const res = await adminRegister({
      username,
      nickname: registerForm.nickname.trim() || username,
      password,
      role: registerForm.role
    })
    if (!res.success) throw new Error(res.message || '注册失败')

    if (!adminRoles.includes(res.data.role)) {
      throw new Error('注册成功，但该账号不是管理员角色，不能进入后台')
    }

    saveAdminSession(res.data)
    await loadStats()
  } catch (e) {
    err.value = e.message
  }
}

function logout() {
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_role')
  localStorage.removeItem('admin_username')
  token.value = ''
  currentRole.value = ''
  currentUsername.value = ''
  authMode.value = 'login'
  items.value = []
  orders.value = []
  users.value = []
  logs.value = []
}

function switchTab(tab) {
  activeTab.value = tab
  loadCurrentTab()
}

async function loadCurrentTab() {
  if (activeTab.value === 'dashboard') return loadStats()
  if (activeTab.value === 'items') return loadItems()
  if (activeTab.value === 'orders') return loadOrders()
  if (activeTab.value === 'users') return loadUsers()
  if (activeTab.value === 'logs') return loadLogs()
}

async function loadStats() {
  err.value = ''
  try {
    const res = await adminStats()
    if (!res.success) throw new Error(res.message || '统计加载失败')
    Object.assign(stats, res.data || {})
  } catch (e) {
    err.value = e.message
  }
}

async function loadItems() {
  err.value = ''
  try {
    const res = await adminListItems({
      q: itemQuery.q || undefined,
      status: itemQuery.status || undefined,
      includeDeleted: itemQuery.includeDeleted
    })
    if (!res.success) throw new Error(res.message || '商品加载失败')
    items.value = res.data || []
  } catch (e) {
    err.value = e.message
  }
}

function resetItems() {
  itemQuery.q = ''
  itemQuery.status = ''
  itemQuery.includeDeleted = false
  loadItems()
}

async function markSold(item) {
  err.value = ''

  if (!window.confirm(`确定要把商品「${item.title}」强制标记为已售吗？`)) return
  const reason = getReason('强制标记已售属于高风险操作')
  if (!reason) return

  try {
    const res = await adminMarkSold(item.id, reason)
    if (!res.success) throw new Error(res.message || '操作失败')
    await loadItems()
    await loadStats()
  } catch (e) {
    err.value = e.message
  }
}

async function offShelfItem(item) {
  err.value = ''

  if (!window.confirm(`确定要强制下架商品「${item.title}」吗？`)) return
  const reason = getReason('强制下架属于高风险操作')
  if (!reason) return

  try {
    const res = await adminOffShelfItem(item.id, reason)
    if (!res.success) throw new Error(res.message || '操作失败')
    await loadItems()
    await loadStats()
  } catch (e) {
    err.value = e.message
  }
}

async function deleteItem(item) {
  err.value = ''

  const tip = item.hasOrder
    ? `商品「${item.title}」存在订单，后端会执行逻辑删除并下架，不会物理删除。确定继续吗？`
    : `商品「${item.title}」无订单，后端会物理删除。确定继续吗？`

  if (!window.confirm(tip)) return
  const reason = getReason('删除商品属于高风险操作')
  if (!reason) return

  try {
    const res = await adminDeleteItem(item.id, reason)
    if (!res.success) throw new Error(res.message || '删除失败')
    await loadItems()
    await loadStats()
  } catch (e) {
    err.value = e.message
  }
}

async function loadOrders() {
  err.value = ''
  try {
    const res = await adminListOrders({
      q: orderQuery.q || undefined,
      status: orderQuery.status || undefined
    })
    if (!res.success) throw new Error(res.message || '订单加载失败')

    orders.value = (res.data || []).map(o => ({
      ...o,
      _newStatus: o.status
    }))
  } catch (e) {
    err.value = e.message
  }
}

function resetOrders() {
  orderQuery.q = ''
  orderQuery.status = ''
  loadOrders()
}

async function updateOrderStatus(order) {
  err.value = ''

  if (order._newStatus === order.status) {
    alert('订单状态没有变化')
    return
  }

  if (!window.confirm(`确定要把订单 #${order.id} 从 ${orderStatusText(order.status)} 改为 ${orderStatusText(order._newStatus)} 吗？`)) return
  const reason = getReason('修改订单状态属于高风险操作')
  if (!reason) return

  try {
    const res = await adminUpdateOrderStatus(order.id, order._newStatus, reason)
    if (!res.success) throw new Error(res.message || '订单状态修改失败')
    await loadOrders()
    await loadStats()
  } catch (e) {
    err.value = e.message
  }
}

async function loadUsers() {
  err.value = ''
  try {
    const res = await adminListUsers({
      q: userQuery.q || undefined,
      role: userQuery.role || undefined,
      status: userQuery.status || undefined
    })
    if (!res.success) throw new Error(res.message || '用户加载失败')

    users.value = (res.data || []).map(u => ({
      ...u,
      _role: u.role
    }))
  } catch (e) {
    err.value = e.message
  }
}

function resetUsers() {
  userQuery.q = ''
  userQuery.role = ''
  userQuery.status = ''
  loadUsers()
}

async function saveUserRole(user) {
  err.value = ''

  if (user.username === 'admin') {
    alert('固定超级管理员 admin 的角色不能修改')
    return
  }

  if (user._role === user.role) {
    alert('用户角色没有变化')
    return
  }

  if (!window.confirm(`确定要把用户「${user.username}」的角色从 ${user.role} 改为 ${user._role} 吗？`)) return
  const reason = getReason('修改用户角色属于高风险操作')
  if (!reason) return

  try {
    const res = await adminUpdateUser(user.id, {
      role: user._role,
      reason
    })
    if (!res.success) throw new Error(res.message || '角色修改失败')
    await loadUsers()
    await loadStats()
  } catch (e) {
    err.value = e.message
  }
}

async function changeUserStatus(user, status) {
  err.value = ''

  if (user.username === 'admin' && status === 'BANNED') {
    alert('固定超级管理员 admin 不能被封禁')
    return
  }

  const action = status === 'BANNED' ? '封禁' : '解封'

  if (!window.confirm(`确定要${action}用户「${user.username}」吗？`)) return
  const reason = getReason(`${action}用户属于高风险操作`)
  if (!reason) return

  try {
    const res = await adminUpdateUser(user.id, {
      status,
      reason
    })
    if (!res.success) throw new Error(res.message || `${action}失败`)
    await loadUsers()
    await loadStats()
  } catch (e) {
    err.value = e.message
  }
}

async function loadLogs() {
  err.value = ''
  try {
    const res = await adminListLogs({
      q: logQuery.q || undefined,
      limit: 200
    })
    if (!res.success) throw new Error(res.message || '日志加载失败')
    logs.value = res.data || []
  } catch (e) {
    err.value = e.message
  }
}

function resetLogs() {
  logQuery.q = ''
  loadLogs()
}

onMounted(() => {
  if (token.value) {
    loadStats()
  }
})
</script>