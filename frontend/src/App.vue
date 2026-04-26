<template>
  <TopBar
      v-model="q"
      :token="token"
      :userInfo="profile"
      @search="handleSearch"
      @openAuth="switchView('auth')"
      @logout="logout"
      @goOrders="switchView('orders')"
      @goProfile="switchView('profile')"
      @goHome="switchView('home')"
      @goPost="handleGoPost"
      @goMyItems="switchView('myItems')"
  />

  <!-- 首页 -->
  <template v-if="currentView === 'home'">
    <div class="container page-section">
      <div class="hero-card">
        <div class="hero-title">校园闲置，轻松流转</div>
        <div class="hero-desc">
          发布、浏览、下单、推荐与 AI 客服一体化，让校内二手交易更方便。
        </div>
      </div>
    </div>

    <div class="container layout">
      <div class="row">
        <!-- 左侧板块 -->
        <div class="left col">
          <BoardNav :boards="boards" :active="activeBoard" @select="selectBoard" />
        </div>

        <!-- 中间主内容 -->
        <div class="main col">


          <ItemList
              :items="items"
              :title="listTitle"
              :total="items.length"
              :error="listError"
              @open="openDetail"
              @buy="doCreateOrder"
          />

          <div class="card section-spacing">
            <div class="list-head">
              <div class="title">为你推荐</div>
              <button class="btn" @click="loadRecs" :disabled="!token">刷新推荐</button>
            </div>

            <!-- 推荐商品卡片展示 -->
            <div style="padding: 12px 14px; display:flex; flex-wrap:wrap; gap:10px;">
              <div
                  v-for="r in recs"
                  :key="r.id"
                  class="card"
                  style="cursor:pointer; width:200px; height:300px; border:1px solid #ddd; border-radius:8px; text-align:center;"
                  @click="openDetail(r.id)"
              >
                <img :src="r.coverImageUrl" alt="商品封面" style="width:100%; height:150px; object-fit:cover; border-radius:8px;" />
                <div class="card-content" style="padding:10px;">
                  <h3>{{ r.title }}</h3>
                  <p>¥ {{ r.price }}</p>
                </div>
              </div>
              <div v-if="recs.length === 0" style="color:#888;">
                暂无推荐（多浏览几个帖子试试）
              </div>
            </div>

            <div v-if="recErr" class="msg" style="margin: 0 14px 14px;">{{ recErr }}</div>
          </div>
        </div>

        <!-- 右侧信息区 -->
        <div class="right col">
          <SideBar @filter="quickFilter" />

          <div class="card section-spacing">
            <div class="sidebar-section">
              <div class="sidebar-title">当前状态</div>
              <div class="kv"><span>登录</span><span>{{ token ? '已登录' : '未登录' }}</span></div>
              <div class="kv"><span>板块</span><span>{{ boardsMap[activeBoard] }}</span></div>
              <div class="kv"><span>关键词</span><span>{{ q || '-' }}</span></div>
              <div class="kv"><span>筛选</span><span>{{ status || '全部' }}</span></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>

  <div v-if="currentView === 'post'" class="container page-section">
    <div class="page-title">发布商品</div>

    <div class="card" style="max-width: 820px; margin: 0 auto;">
      <div class="list-head">
        <div class="title">填写商品信息</div>
        <button class="btn" @click="switchView('home')">返回首页</button>
      </div>

      <div class="form">
        <div class="grid">
          <input v-model="form.title" placeholder="标题（如：二手耳机）" />
          <input v-model="form.price" placeholder="价格（如：99.00）" />

          <select v-model="form.board">
            <option value="digital">数码</option>
            <option value="books">书籍</option>
            <option value="daily">日用</option>
            <option value="sports">运动</option>
          </select>

          <div></div>

          <input
              class="full"
              v-model="form.description"
              placeholder="描述（成色/配件/交易方式）"
          />
        </div>

        <div class="section-spacing">
          <div style="font-size: 15px; font-weight: 800; margin-bottom: 10px;">上传图片</div>

          <div class="upload-grid">
            <label
                v-if="postImages.length < 3"
                class="upload-box"
            >
              <input
                  type="file"
                  accept="image/*"
                  multiple
                  style="display:none;"
                  @change="handleSelectImages"
              />
              <div class="upload-plus">+</div>
              <div class="upload-text">{{ uploading ? '上传中...' : '上传图片' }}</div>
              <div class="upload-tip">最多 3 张</div>
            </label>

            <div
                v-for="(img, index) in postImages"
                :key="img.url"
                class="upload-preview"
                :class="{ active: coverIndex === index }"
            >
              <img :src="img.url" alt="商品图片" class="upload-preview-img" />

              <div class="upload-preview-actions">
                <button
                    class="btn"
                    :class="{ primary: coverIndex === index }"
                    @click="setCover(index)"
                >
                  {{ coverIndex === index ? '当前封面' : '设为封面' }}
                </button>

                <button class="btn" @click="removeImage(index)">删除</button>
              </div>
            </div>
          </div>
        </div>

        <div class="panel-actions" style="margin-top: 16px;">
          <button
              class="btn primary"
              @click="isEditMode ? updatePost() : createPost()"
              :disabled="uploading"
          >
            {{ isEditMode ? '保存修改' : '立即发布' }}
          </button>
          <button
              v-if="isEditMode"
              class="btn"
              @click="cancelEdit"
          >
            取消编辑
          </button>
          <button class="btn" @click="resetPostForm">清空内容</button>
        </div>

        <div v-if="postError" class="msg">{{ postError }}</div>
      </div>
    </div>
  </div>

  <!-- 我的订单 -->
  <div v-if="currentView === 'orders'" class="container page-section">
    <div class="page-title">我的订单</div>

    <div class="card">
      <div class="list-head">
        <div class="title">订单列表</div>
        <button class="btn" @click="loadOrders">刷新</button>
      </div>

      <div class="panel-actions" style="margin-bottom: 14px;">
        <button
            class="btn"
            :class="{ primary: orderTab === 'buy' }"
            @click="orderTab = 'buy'"
        >
          我购买的
        </button>

        <button
            class="btn"
            :class="{ primary: orderTab === 'sell' }"
            @click="orderTab = 'sell'"
        >
          我发布的
        </button>
      </div>

      <div v-if="orderTab === 'buy'">
        <div v-if="orderError" class="msg">{{ orderError }}</div>
        <div v-if="orders.length === 0" style="color:#888;">暂无购买订单</div>

        <div
            v-for="order in orders"
            :key="order.id"
            class="order-card"
            style="margin-bottom: 12px;"
        >
          <div style="display:flex; justify-content:space-between; gap:12px; align-items:flex-start; flex-wrap:wrap;">
            <div>
              <div style="font-weight:800; font-size:16px;">{{ order.item?.title || '未命名商品' }}</div>
              <div style="margin-top:6px; color:#d03050; font-weight:800;">¥ {{ order.item?.price ?? '-' }}</div>
              <div style="margin-top:6px; color:#666;">
                卖家：{{ order.item?.seller?.nickname || order.item?.seller?.username || '-' }}
              </div>
              <div style="margin-top:6px; color:#888; font-size:12px;">
                订单状态：{{ order.status }} ｜ 商品状态：{{ order.item?.status || '-' }}
              </div>

              <div
                  v-if="order.status === 'CREATED'"
                  style="margin-top:6px; font-size:13px; font-weight:700;"
                  :style="{ color: isOrderExpired(order) ? '#ef4444' : '#f59e0b' }"
              >
                {{ formatRemaining(order) }}
              </div>
            </div>

            <div class="panel-actions" style="justify-content:flex-end;">
              <button
                  v-if="order.status === 'CREATED' && !isOrderExpired(order)"
                  class="btn primary"
                  @click="openPayDialog(order)"
              >
                去支付
              </button>

              <button
                  v-if="order.status === 'CREATED' && !isOrderExpired(order)"
                  class="btn"
                  @click="doCancelOrder(order.id)"
              >
                取消订单
              </button>

              <button
                  v-if="order.item?.id"
                  class="btn"
                  @click="openDetail(order.item.id)"
              >
                查看商品
              </button>
            </div>
          </div>
        </div>
      </div>

      <div v-else>
        <div v-if="salesOrderError" class="msg">{{ salesOrderError }}</div>
        <div v-if="salesOrders.length === 0" style="color:#888;">暂无卖出订单</div>

        <div
            v-for="order in salesOrders"
            :key="order.id"
            class="order-card"
            style="margin-bottom: 12px;"
        >
          <div style="display:flex; justify-content:space-between; gap:12px; align-items:flex-start; flex-wrap:wrap;">
            <div>
              <div style="font-weight:800; font-size:16px;">{{ order.item?.title || '未命名商品' }}</div>
              <div style="margin-top:6px; color:#d03050; font-weight:800;">¥ {{ order.item?.price ?? '-' }}</div>
              <div style="margin-top:6px; color:#666;">
                买家：{{ order.buyer?.nickname || order.buyer?.username || '-' }}
              </div>
              <div style="margin-top:6px; color:#888; font-size:12px;">
                订单状态：{{ getOrderStatusLabel(order) }} ｜ 商品状态：{{ order.item?.status || '-' }}
              </div>
            </div>

            <div class="panel-actions" style="justify-content:flex-end;">
              <button
                  v-if="order.item?.id"
                  class="btn"
                  @click="openDetail(order.item.id)"
              >
                查看商品
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 个人中心 -->
  <div v-if="currentView === 'profile'" class="container page-section">
    <div class="page-title">个人中心</div>

    <div class="row">
      <div class="main col">
        <div class="card">
          <div class="list-head">
            <div class="title">个人资料</div>
            <button class="btn primary" @click="saveProfile">保存资料</button>
          </div>

          <div class="form">
            <div style="display:flex; gap:16px; align-items:flex-start; flex-wrap:wrap;">
              <div
                  style="width:100px; height:100px; border-radius:50%; overflow:hidden; background:#f3f3f3; display:flex; align-items:center; justify-content:center; border:1px solid #eee;"
              >
                <img
                    v-if="profile.avatarUrl"
                    :src="profile.avatarUrl"
                    alt="avatar"
                    style="width:100%; height:100%; object-fit:cover;"
                />
                <span v-else style="color:#999;">头像</span>
              </div>

              <div style="flex:1; min-width:260px;">
                <div class="grid">
                  <input v-model="profile.nickname" placeholder="昵称" />
                  <input v-model="profile.avatarUrl" placeholder="头像图片地址（URL）" />
                  <input class="full" v-model="profile.bio" placeholder="个人简介" />
                </div>
              </div>
            </div>

            <div v-if="profileMsg" class="msg">{{ profileMsg }}</div>
          </div>
        </div>

        <div class="card section-spacing">
          <div class="list-head">
            <div class="title">支付设置</div>
            <button class="btn primary" @click="savePayment">保存收款码</button>
          </div>

          <div class="form">
            <div class="grid">
              <input
                  class="full"
                  v-model="profile.wechatQrUrl"
                  placeholder="微信收款码图片地址（URL）"
              />
            </div>

            <div class="soft-box section-spacing" style="width: fit-content;">
              <div style="font-size:13px; color:#666; margin-bottom:8px;">收款码预览</div>
              <div
                  style="width:220px; height:220px; border:1px solid #eee; border-radius:12px; background:#fafafa; display:flex; align-items:center; justify-content:center; overflow:hidden;"
              >
                <img
                    v-if="profile.wechatQrUrl"
                    :src="profile.wechatQrUrl"
                    alt="wechat-qr"
                    style="width:100%; height:100%; object-fit:contain;"
                />
                <span v-else style="color:#999;">暂无收款码</span>
              </div>
            </div>

            <div v-if="paymentMsg" class="msg">{{ paymentMsg }}</div>
          </div>
        </div>
      </div>

      <div class="right col">
        <div class="card">
          <div class="sidebar-section">
            <div class="sidebar-title">个人信息</div>
            <div class="kv"><span>用户名</span><span>{{ profile.username || '-' }}</span></div>
            <div class="kv"><span>角色</span><span>{{ profile.role || '-' }}</span></div>
            <div class="kv"><span>昵称</span><span>{{ profile.nickname || '-' }}</span></div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div v-if="currentView === 'myItems'" class="container page-section">
    <div class="page-title">我的发布</div>

    <div class="card">
      <div class="list-head">
        <div class="title">我发布的商品</div>
        <button class="btn" @click="loadMyItems">刷新</button>
      </div>

      <div style="padding:14px;">
        <div v-if="myItemsError" class="msg">{{ myItemsError }}</div>
        <div v-if="myItems.length === 0" style="color:#888;">暂无已发布商品</div>

        <div
            v-for="item in myItems"
            :key="item.id"
            class="order-card"
            style="margin-bottom:12px;"
        >
          <div style="display:flex; justify-content:space-between; gap:12px; align-items:flex-start; flex-wrap:wrap;">
            <div>
              <div style="font-weight:800; font-size:16px;">{{ item.title }}</div>
              <div style="margin-top:6px; color:#d03050; font-weight:800;">¥ {{ item.price }}</div>
              <div style="margin-top:6px; color:#888; font-size:12px;">
                商品状态：{{ item.status }}
              </div>
            </div>


            <div class="panel-actions">
              <button class="btn" @click="openEditItem(item)">编辑</button>

              <button
                  v-if="item.status !== 'OFF_SHELF'"
                  class="btn"
                  @click="doOffShelf(item.id)"
              >
                下架
              </button>

              <button
                  v-else
                  class="btn primary"
                  @click="doPutOnShelf(item.id)"
              >
                重新上架
              </button>

              <button
                  v-if="item.status === 'OFF_SHELF'"
                  class="btn"
                  @click="doDeleteItem(item.id)"
              >
                删除
              </button>

              <button class="btn" @click="openDetail(item.id)">查看详情</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 商品详情弹窗 -->
  <div
      v-if="showDetail"
      style="position:fixed; inset:0; background:rgba(0,0,0,.35); display:flex; align-items:center; justify-content:center; z-index:30;"
  >
    <div class="card" style="width:720px; max-width: calc(100vw - 24px);">
      <div class="list-head">
        <div class="title">帖子详情</div>
        <button class="btn" @click="showDetail = false">关闭</button>
      </div>

      <div style="padding:14px;">
        <div style="display:flex; gap:14px; flex-wrap:wrap;">
          <div class="thumb" style="width:180px; height:180px; overflow:hidden;">
            <img
                v-if="detail?.imageUrls && detail.imageUrls.length > 0"
                :src="detail.imageUrls[0]"
                alt="商品图片"
                style="width:100%; height:100%; object-fit:cover;"
            />
            <span v-else>图片</span>
          </div>

          <div style="flex:1; min-width:220px;">
            <div style="font-weight:900; font-size:18px;">{{ detail?.title }}</div>
            <div style="margin-top:8px; color:#d03050; font-weight:900;">¥ {{ detail?.price }}</div>
            <div style="margin-top:8px; color:#666;">{{ detail?.description || '（无描述）' }}</div>
            <div style="margin-top:10px; color:#888; font-size:12px;">
              卖家：{{ detail?.seller?.nickname || detail?.seller?.username || '-' }} ｜ 状态：{{ detail?.status }}
            </div>

            <div class="panel-actions" style="margin-top:14px;">
              <button
                  class="btn primary"
                  @click="doCreateOrder(detail?.id)"
                  :disabled="!token || !detail || detail.status !== 'AVAILABLE'"
              >
                下单
              </button>
            </div>
          </div>
        </div>

        <div v-if="detailErr" class="msg">{{ detailErr }}</div>
      </div>
    </div>
  </div>

  <!-- 支付弹窗 -->
  <div
      v-if="showPayDialog"
      style="position:fixed; inset:0; background:rgba(0,0,0,.35); display:flex; align-items:center; justify-content:center; z-index:40;"
  >
    <div class="card" style="width:520px; max-width: calc(100vw - 24px);">
      <div class="list-head">
        <div class="title">订单支付</div>
        <button class="btn" @click="showPayDialog = false">关闭</button>
      </div>

      <div style="padding:14px;">
        <div style="font-weight:800; font-size:16px;">{{ payOrderInfo?.item?.title || '-' }}</div>
        <div style="margin-top:8px; color:#d03050; font-weight:800;">
          ¥ {{ payOrderInfo?.item?.price ?? '-' }}
        </div>
        <div style="margin-top:8px; color:#666;">
          卖家：{{ payOrderInfo?.item?.seller?.nickname || payOrderInfo?.item?.seller?.username || '-' }}
        </div>

        <div
            v-if="payOrderInfo?.status === 'CREATED'"
            style="margin-top:8px; font-size:13px; font-weight:700;"
            :style="{ color: isOrderExpired(payOrderInfo) ? '#ef4444' : '#f59e0b' }"
        >
          {{ formatRemaining(payOrderInfo) }}
        </div>

        <div style="margin-top:14px; font-size:13px; color:#666;">请扫码向卖家付款</div>
        <div
            class="soft-box"
            style="margin-top:10px; width:260px; height:260px; display:flex; align-items:center; justify-content:center; overflow:hidden;"
        >
          <img
              v-if="payOrderInfo?.item?.seller?.wechatQrUrl"
              :src="payOrderInfo.item.seller.wechatQrUrl"
              alt="seller-wechat-qr"
              style="width:100%; height:100%; object-fit:contain;"
          />
          <span v-else style="color:#999;">卖家暂未上传收款码</span>
        </div>

        <div class="panel-actions" style="margin-top:14px;">
          <button
              class="btn primary"
              @click="doPayOrder(payOrderInfo.id)"
              :disabled="!payOrderInfo || isOrderExpired(payOrderInfo)"
          >
            我已完成支付
          </button>
          <button
              class="btn"
              @click="doCancelOrder(payOrderInfo.id)"
              :disabled="!payOrderInfo"
          >
            取消订单
          </button>
        </div>

        <div v-if="payMsg" class="msg">{{ payMsg }}</div>
      </div>
    </div>
  </div>

  <AiChatWidget @openDetail="openDetail" />

  <!-- 登录弹层 -->
  <div v-if="currentView === 'auth'" class="container page-section">
    <div style="max-width: 460px; margin: 0 auto;">
      <div class="hero-card" style="margin-bottom: 16px;">
        <div class="hero-title">欢迎来到校园二手论坛</div>
        <div class="hero-desc">登录后可发布商品、下单、管理订单与完善个人资料。</div>
      </div>

      <div class="card">
        <div class="list-head">
          <div class="title">{{ authMode === 'login' ? '用户登录' : '用户注册' }}</div>
          <button class="btn" @click="switchView('home')">返回首页</button>
        </div>

        <div class="form">
          <div style="display:flex; gap:8px; margin-bottom:14px;">
            <button
                class="btn"
                :class="{ primary: authMode === 'login' }"
                @click="authMode = 'login'"
            >
              登录
            </button>
            <button
                class="btn"
                :class="{ primary: authMode === 'register' }"
                @click="authMode = 'register'"
            >
              注册
            </button>
          </div>

          <div class="grid">
            <input v-model="auth.username" placeholder="请输入用户名" />
            <input v-model="auth.password" placeholder="请输入密码" type="password" />
          </div>

          <div class="panel-actions" style="margin-top: 14px;">
            <button
                v-if="authMode === 'register'"
                class="btn primary"
                @click="doRegister"
            >
              立即注册
            </button>

            <button
                v-if="authMode === 'login'"
                class="btn primary"
                @click="doLogin"
            >
              立即登录
            </button>
          </div>

          <div v-if="authError" class="msg">{{ authError }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import './styles/app.css'
import { reactive, ref, computed, onMounted, onBeforeUnmount  } from 'vue'
import TopBar from './components/TopBar.vue'
import BoardNav from './components/BoardNav.vue'
import ItemList from './components/ItemList.vue'
import SideBar from './components/SideBar.vue'
import AiChatWidget from './components/AiChatWidget.vue'

import {
  register,
  login,
  listItems,
  createItem,
  createOrder,
  getItemDetail,
  getRecommendations,
  getMyProfile,
  updateMyProfile,
  updateMyPayment,
  updateItem,
  deleteItem,
  listMyOrders,
  listMySalesOrders,
  listMyItems,
  offShelfItem,
  putOnShelfItem,
  payOrder,
  cancelOrder,
  uploadImage
} from './api'

const token = ref(localStorage.getItem('token') || '')
const q = ref('')
const status = ref('')
const showAuth = ref(false)
const currentView = ref('home')

const authMode = ref('login')
const auth = reactive({ username: '', password: '' })
const authError = ref('')

const form = reactive({
  title: '',
  price: '',
  description: '',
  board: 'digital'
})

const isEditMode = ref(false)
const editingItemId = ref(null)

const postImages = ref([])
const coverIndex = ref(0)
const uploading = ref(false)

const postError = ref('')

const items = ref([])
const listError = ref('')

const recs = ref([])
const recErr = ref('')

const showDetail = ref(false)
const detail = ref(null)
const detailErr = ref('')

const orders = ref([])
const orderError = ref('')
const orderTab = ref('buy')
const salesOrders = ref([])
const salesOrderError = ref('')
const nowTs = ref(Date.now())
let countdownTimer = null

const myItems = ref([])
const myItemsError = ref('')

const showPayDialog = ref(false)
const payOrderInfo = ref(null)
const payMsg = ref('')

const profile = reactive({
  id: null,
  username: '',
  role: '',
  nickname: '',
  avatarUrl: '',
  bio: '',
  wechatQrUrl: ''
})
const profileMsg = ref('')
const paymentMsg = ref('')

const boards = [
  { key: 'all', name: '全部' },
  { key: 'digital', name: '数码' },
  { key: 'books', name: '书籍' },
  { key: 'daily', name: '日用' },
  { key: 'sports', name: '运动' }
]
const boardsMap = boards.reduce((m, x) => (m[x.key] = x.name, m), {})
const activeBoard = ref('all')


const listTitle = computed(() => {
  const b = boardsMap[activeBoard.value] || '全部'
  const s = status.value ? (status.value === 'AVAILABLE' ? '在售' : '已售') : '全部'
  return `帖子列表 - ${b}（${s}）`
})

function setToken(res) {
  if (!res.success) throw new Error(res.message || 'auth failed')
  localStorage.setItem('token', res.data.token)
  token.value = res.data.token
}


function fillProfile(data) {
  profile.id = data?.id ?? null
  profile.username = data?.username ?? ''
  profile.role = data?.role ?? ''
  profile.nickname = data?.nickname ?? ''
  profile.avatarUrl = data?.avatarUrl ?? ''
  profile.bio = data?.bio ?? ''
  profile.wechatQrUrl = data?.wechatQrUrl ?? ''
}

function resetPostForm() {
  form.title = ''
  form.price = ''
  form.description = ''
  form.board = 'digital'
  postImages.value = []
  coverIndex.value = 0
  postError.value = ''
  isEditMode.value = false
  editingItemId.value = null
}

function cancelEdit() {
  isEditMode.value = false
  editingItemId.value = null
  resetPostForm()
  currentView.value = 'myItems'
}

async function doRegister() {
  authError.value = ''
  try {
    setToken(await register(auth.username, auth.password))
    currentView.value = 'home'
    await loadInitialData()
  } catch (e) {
    authError.value = e.message
  }
}

async function doLogin() {
  authError.value = ''
  try {
    setToken(await login(auth.username, auth.password))
    currentView.value = 'home'
    await loadInitialData()
  } catch (e) {
    authError.value = e.message
  }
}

function logout() {
  localStorage.removeItem('token')
  token.value = ''
  items.value = []
  recs.value = []
  orders.value = []
  fillProfile(null)
  currentView.value = 'home'
}


// 在切换板块时加载数据
function selectBoard(key) {
  activeBoard.value = key
  loadItems()
}


function quickFilter(s) {
  status.value = s
  loadItems()
}

function handleSearch() {
  loadItems()
}

function handleGoPost() {
  if (!token.value) {
    currentView.value = 'auth'
    return
  }
  currentView.value = 'post'
}

async function loadItems() {
  listError.value = ''  // 清空错误信息
  try {
    // 发请求时传递当前板块（activeBoard）以进行过滤
    const res = await listItems({
      q: q.value || undefined, // 搜索关键词
      status: status.value || undefined, // 商品状态
      board: activeBoard.value // 使用activeBoard进行板块过滤
    })

    // 如果请求失败，抛出错误
    if (!res.success) throw new Error(res.message || '加载商品失败')

    // 更新页面上显示的商品列表
    items.value = res.data
  } catch (e) {
    // 捕获错误，展示在页面
    listError.value = e.message
  }
}

async function createPost() {
  postError.value = ''

  try {
    const res = await createItem({
      title: form.title,
      description: form.description,
      price: form.price,
      board: form.board,
      imageUrls: postImages.value.map(img => img.url),
      coverIndex: postImages.value.length ? coverIndex.value : 0
    })

    if (!res.success) throw new Error(res.message || 'create failed')

    resetPostForm()
    await loadItems()  // 发布后立即加载商品列表，确保新商品展示
    currentView.value = 'home'
  } catch (e) {
    postError.value = e.message
  }
}

async function updatePost() {
  postError.value = ''

  try {
    const res = await updateItem(editingItemId.value, {
      title: form.title,
      description: form.description,
      price: form.price,
      board: form.board,
      imageUrls: postImages.value.map(img => img.url),
      coverIndex: postImages.value.length ? coverIndex.value : 0
    })

    if (!res.success) throw new Error(res.message || '更新失败')

    resetPostForm()
    await loadMyItems()
    await loadItems()
    currentView.value = 'myItems'
  } catch (e) {
    postError.value = e.message
  }
}

async function doDeleteItem(id) {
  myItemsError.value = ''
  try {
    const res = await deleteItem(id)
    if (!res.success) throw new Error(res.message || '删除失败')

    myItems.value = myItems.value.filter(item => item.id !== id) // 删除成功后从列表中移除商品
    await loadMyItems()
    await loadItems()
  } catch (e) {
    myItemsError.value = e.message
  }
}



async function openDetail(id) {
  detailErr.value = ''
  try {
    const res = await getItemDetail(id)
    if (!res.success) throw new Error(res.message || 'load failed')
    detail.value = res.data
    showDetail.value = true
  } catch (e) {
    detailErr.value = e.message
  }
}

async function doCreateOrder(itemId) {
  if (!itemId) return
  try {
    const res = await createOrder(itemId)
    if (!res.success) throw new Error(res.message || '下单失败')
    showDetail.value = false
    currentView.value = 'orders'
    await loadOrders()
  } catch (e) {
    detailErr.value = e.message
  }
}

async function loadRecs() {
  recErr.value = ''
  try {
    const res = await getRecommendations(8) // 假设您获取了推荐商品
    if (!res.success) throw new Error(res.message || 'reco failed')

    // 过滤下架商品和已删除商品
    recs.value = res.data.filter(item => item.status !== 'OFF_SHELF' && !item.deleted)

    // 如果没有推荐商品，给出提示
    if (recs.value.length === 0) {
      recErr.value = '暂无推荐商品，请多浏览几个商品'
    }
  } catch (e) {
    recErr.value = e.message || '推荐加载失败'
  }
}

async function loadOrders() {
  orderError.value = ''
  try {
    const res = await listMyOrders()
    if (!res.success) throw new Error(res.message || '订单加载失败')
    orders.value = res.data || []
  } catch (e) {
    orderError.value = e.message
  }
}

function openPayDialog(order) {
  payMsg.value = ''
  payOrderInfo.value = order
  showPayDialog.value = true
}

async function doPayOrder(orderId) {
  payMsg.value = ''
  try {
    const res = await payOrder(orderId)
    if (!res.success) throw new Error(res.message || '支付确认失败')
    payMsg.value = '支付成功，订单已完成'
    await loadOrders()
    await loadItems()
  } catch (e) {
    payMsg.value = e.message
  }
}

async function doCancelOrder(orderId) {
  payMsg.value = ''
  orderError.value = ''
  try {
    const res = await cancelOrder(orderId)
    if (!res.success) throw new Error(res.message || '取消订单失败')
    showPayDialog.value = false
    await loadOrders()
    await loadItems()
  } catch (e) {
    const msg = e.message || '取消订单失败'
    payMsg.value = msg
    orderError.value = msg
  }
}

async function loadProfile() {
  profileMsg.value = ''
  paymentMsg.value = ''
  try {
    const res = await getMyProfile()
    if (!res.success) throw new Error(res.message || '个人信息加载失败')
    fillProfile(res.data)
  } catch (e) {
    profileMsg.value = e.message
  }
}

async function saveProfile() {
  profileMsg.value = ''
  try {
    const res = await updateMyProfile({
      nickname: profile.nickname,
      avatarUrl: profile.avatarUrl,
      bio: profile.bio
    })
    if (!res.success) throw new Error(res.message || '保存失败')
    fillProfile(res.data)
    profileMsg.value = '个人资料已保存'
  } catch (e) {
    profileMsg.value = e.message
  }
}

async function savePayment() {
  paymentMsg.value = ''
  try {
    const res = await updateMyPayment({
      wechatQrUrl: profile.wechatQrUrl
    })
    if (!res.success) throw new Error(res.message || '保存收款码失败')
    fillProfile(res.data)
    paymentMsg.value = '收款码已保存'
  } catch (e) {
    paymentMsg.value = e.message
  }
}

async function loadInitialData() {
  await loadItems()
  await loadRecs()
  await loadProfile()
}


async function loadSalesOrders() {
  salesOrderError.value = ''
  try {
    const res = await listMySalesOrders()
    if (!res.success) throw new Error(res.message || '卖出订单加载失败')
    salesOrders.value = res.data || []
  } catch (e) {
    salesOrderError.value = e.message
  }
}

async function loadMyItems() {
  myItemsError.value = ''
  try {
    const res = await listMyItems()
    if (!res.success) throw new Error(res.message || '我的发布加载失败')
    myItems.value = res.data || []
  } catch (e) {
    myItemsError.value = e.message
  }
}

async function doPutOnShelf(id) {
  myItemsError.value = ''
  try {
    const res = await putOnShelfItem(id)
    if (!res.success) throw new Error(res.message || '重新上架失败')
    await loadMyItems()
    await loadItems()
  } catch (e) {
    myItemsError.value = e.message
  }
}

async function doOffShelf(id) {
  myItemsError.value = ''
  try {
    const res = await offShelfItem(id)
    if (!res.success) throw new Error(res.message || '下架失败')
    await loadMyItems()
    await loadItems()
  } catch (e) {
    myItemsError.value = e.message
  }
}

async function openEditItem(item) {
  postError.value = ''

  try {
    const res = await getItemDetail(item.id)
    if (!res.success) throw new Error(res.message || '加载商品详情失败')

    const full = res.data

    isEditMode.value = true
    editingItemId.value = full.id

    form.title = full.title || ''
    form.price = full.price ?? ''
    form.description = full.description || ''
    form.board = full.board || 'digital'

    postImages.value = (full.imageUrls || []).map(url => ({ url }))

    if (full.coverImageUrl && full.imageUrls && full.imageUrls.length > 0) {
      const idx = full.imageUrls.findIndex(url => url === full.coverImageUrl)
      coverIndex.value = idx >= 0 ? idx : 0
    } else {
      coverIndex.value = 0
    }

    currentView.value = 'post'
  } catch (e) {
    postError.value = e.message || '加载商品详情失败'
  }
}


function switchView(view) {
  currentView.value = view
  if (!token.value) return

  if (view === 'orders') {
    loadOrders()
    loadSalesOrders()
  } else if (view === 'profile') {
    loadProfile()
  } else if (view === 'myItems') {
    loadMyItems()
  }
}


async function handleSelectImages(event) {
  const files = Array.from(event.target.files || [])

  if (!files.length) return

  const remain = 3 - postImages.value.length
  const selected = files.slice(0, remain)

  if (remain <= 0) {
    postError.value = '最多只能上传 3 张图片'
    event.target.value = ''
    return
  }

  uploading.value = true
  postError.value = ''

  try {
    for (const file of selected) {
      const res = await uploadImage(file)
      if (!res.success) throw new Error(res.message || '图片上传失败')

      postImages.value.push({
        url: res.data.url
      })
    }

    if (coverIndex.value >= postImages.value.length) {
      coverIndex.value = 0
    }
  } catch (e) {
    postError.value = e.message || '图片上传失败'
  } finally {
    uploading.value = false
    event.target.value = ''
  }
}

function setCover(index) {
  coverIndex.value = index
}

function removeImage(index) {
  postImages.value.splice(index, 1)

  if (postImages.value.length === 0) {
    coverIndex.value = 0
    return
  }

  if (coverIndex.value === index) {
    coverIndex.value = 0
  } else if (coverIndex.value > index) {
    coverIndex.value -= 1
  }
}

function getOrderDeadline(order) {
  if (!order?.createdAt) return null
  const created = new Date(order.createdAt).getTime()
  if (Number.isNaN(created)) return null
  return created + 10 * 60 * 1000
}

function getRemainingMs(order) {
  const deadline = getOrderDeadline(order)
  if (!deadline) return 0
  return Math.max(0, deadline - nowTs.value)
}

function isOrderExpired(order) {
  return getRemainingMs(order) <= 0
}

function formatRemaining(order) {
  const ms = getRemainingMs(order)
  if (ms <= 0) return '订单已超时'

  const totalSeconds = Math.floor(ms / 1000)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60

  return `剩余 ${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
}

function getOrderStatusLabel(order) {
  if (order.status === 'CREATED') {
    return isOrderExpired(order) ? '交易关闭' : '等待付款'
  }
  if (order.status === 'PAID') return '交易成功'
  if (order.status === 'CANCELED') return '已取消'
  return order.status
}


onMounted(async () => {
  countdownTimer = setInterval(() => {
    nowTs.value = Date.now()
  }, 1000)

  if (token.value) {
    await loadInitialData()
  }
})

onBeforeUnmount(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>