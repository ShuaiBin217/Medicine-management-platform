<template>
    <div class="detail-container">
        <!-- 未带 id 进入：先选药品 -->
        <div v-if="!goodsId" class="card empty-card">
            <div class="empty-kicker mono">FLOW / LOOKUP</div>
            <h1 class="empty-title">选择要查看的药品</h1>
            <el-select v-model="pickedId" filterable placeholder="输入药品名搜索" style="width: 360px; max-width: 100%;" @change="goPicked">
                <el-option
                        v-for="item in goodsOptions"
                        :key="item.id"
                        :label="item.name + '（' + storageName(item.storage) + ' · 库存 ' + item.count + '）'"
                        :value="item.id">
                </el-option>
            </el-select>
        </div>

        <template v-else>
            <header class="detail-head">
                <div class="head-top">
                    <button class="back-btn" @click="back">← 返回药品管理</button>
                    <el-select v-model="pickedId" filterable placeholder="切换药品" size="default"
                               style="width: 280px;" @change="goPicked">
                        <el-option
                                v-for="item in goodsOptions"
                                :key="item.id"
                                :label="item.name + '（' + storageName(item.storage) + '）'"
                                :value="item.id">
                        </el-option>
                    </el-select>
                </div>

                <div class="head-main">
                    <div class="head-info">
                        <div class="head-kicker mono">DRUG / {{ goods.id }}</div>
                        <h1 class="drug-name">{{ goods.name }}</h1>
                        <div class="drug-meta">
                            <span class="meta-item"><span class="meta-label">药房</span>{{ goods.storagename || '未分配药房' }}</span>
                            <span class="meta-item"><span class="meta-label">分类</span>{{ goods.goodstypename || '未分类' }}</span>
                            <span class="meta-item" v-if="goods.remark"><span class="meta-label">备注</span>{{ goods.remark }}</span>
                        </div>
                    </div>
                    <div class="head-stock">
                        <span class="stock-label">本药房库存</span>
                        <span class="stock-line"><span class="stock-value mono">{{ goods.count == null ? 0 : goods.count }}</span><span class="stock-unit">件</span></span>
                    </div>
                </div>
            </header>

            <section class="stat-row">
                <div class="stat">
                    <span class="stat-label">同名药品总库存</span>
                    <span class="stat-value mono">{{ totalStock }}</span>
                </div>
                <div class="stat">
                    <span class="stat-label">累计入库</span>
                    <span class="stat-value mono">{{ totalIn }}</span>
                </div>
                <div class="stat">
                    <span class="stat-label">累计出库</span>
                    <span class="stat-value mono">{{ totalOut }}</span>
                </div>
                <div class="stat">
                    <span class="stat-label">待确认</span>
                    <span class="stat-value mono" :class="{ 'is-warn': pending > 0 }">{{ pending }}</span>
                </div>
            </section>

            <div class="split">
                <section class="card flow-card">
                    <div class="card-head">
                        <div>
                            <h2 class="card-title">出入库流水</h2>
                        </div>
                        <div class="head-right">
                            <span class="count mono">{{ records.length }} 条</span>
                            <el-tag v-if="isReadOnly" size="small" type="info">仅本人经手</el-tag>
                        </div>
                    </div>

                    <div v-if="!records.length" class="empty-state">该药品暂无出入库记录</div>
                    <div v-else class="flow-list">
                        <div v-for="item in records" :key="item.id" class="flow-item">
                            <div class="flow-time mono">
                                <span class="flow-date">{{ datePart(item.createtime) }}</span>
                                <span class="flow-clock">{{ timePart(item.createtime) }}</span>
                            </div>
                            <div class="flow-rail">
                                <span class="flow-dot" :class="item.count >= 0 ? 'is-in' : 'is-out'"></span>
                            </div>
                            <div class="flow-body">
                                <div class="flow-main">
                                    <span class="flow-type">{{ item.count >= 0 ? '入库' : '出库' }}</span>
                                    <span class="flow-qty mono">{{ item.count >= 0 ? '+' : '−' }}{{ Math.abs(item.count || 0) }}</span>
                                    <span class="flow-status" :class="statusClass(item.status)">
                                        <span class="status-dot" :class="statusDotClass(item.status)"></span>{{ statusText(item.status) }}
                                    </span>
                                    <span class="flow-id mono">#{{ item.id }}</span>
                                </div>
                                <div class="flow-meta">
                                    <span>{{ item.storagename }}</span>
                                    <span>操作人 {{ item.adminname || '-' }}</span>
                                    <span v-if="item.count < 0">取药人 {{ item.username || '-' }}</span>
                                </div>
                                <div v-if="item.remark" class="flow-remark">备注：{{ item.remark }}</div>
                            </div>
                        </div>
                    </div>
                </section>

                <aside class="card dist-card">
                    <div class="card-head">
                        <div>
                            <h2 class="card-title">同名药品分布</h2>
                        </div>
                    </div>

                    <div v-if="sameName.length" class="dist-list">
                        <div v-for="row in sameName" :key="row.id" class="dist-row" :class="{ 'is-current': row.id == goods.id }">
                            <div class="dist-main">
                                <span class="dist-name">{{ row.storagename }}</span>
                                <span class="dist-id mono">#{{ row.id }}</span>
                            </div>
                            <div class="dist-bar">
                                <span :style="{ width: percent(row) + '%' }"></span>
                            </div>
                            <div class="dist-foot">
                                <span class="dist-count mono">{{ row.count || 0 }}</span>
                                <span class="dist-pct mono">{{ percentText(row) }}</span>
                                <button v-if="row.id != goods.id" class="dist-link" @click="goPicked(row.id)">查看</button>
                                <span v-else class="dist-now">当前</span>
                            </div>
                        </div>
                    </div>
                    <div v-else class="empty-state small">暂无同名药品</div>
                </aside>
            </div>
        </template>
    </div>
</template>

<script>
    export default {
        name: "GoodsDetail",
        data() {
            return {
                user: JSON.parse(sessionStorage.getItem('CurUser')),
                goodsId: '',
                pickedId: '',
                goodsOptions: [],
                storageMap: {},
                goods: {},
                sameName: [],
                records: [],
                totalIn: 0,
                totalOut: 0,
                pending: 0,
                totalStock: 0
            }
        },
        computed: {
            isReadOnly() {
                return this.user && this.user.roleId == 2
            }
        },
        methods: {
            storageName(id) {
                if (id === '' || id === null || id === undefined) return '未分配'
                return this.storageMap[id] || ('药房' + id)
            },
            datePart(value) {
                if (!value) return '—'
                return String(value).slice(0, 10)
            },
            timePart(value) {
                if (!value) return ''
                const text = String(value)
                return text.length > 11 ? text.slice(11, 16) : ''
            },
            statusText(status) {
                const value = Number(status)
                if (value === 2) return '已回滚'
                return value === 0 ? '待确认' : '已确认'
            },
            statusClass(status) {
                const value = Number(status)
                if (value === 2) return 'is-rolled'
                return value === 0 ? 'is-pending' : 'is-done'
            },
            statusDotClass(status) {
                const value = Number(status)
                if (value === 2) return 'is-muted'
                return value === 0 ? 'is-warn' : 'is-ok'
            },
            percent(row) {
                if (!this.totalStock) return 0
                return Math.max(4, Math.round((row.count || 0) * 100 / this.totalStock))
            },
            percentText(row) {
                if (!this.totalStock) return '—'
                return ((row.count || 0) * 100 / this.totalStock).toFixed(1) + '%'
            },
            back() {
                this.$router.push('/Goods')
            },
            goPicked(id) {
                if (!id || id == this.goodsId) return
                this.$router.replace({path: '/GoodsDetail', query: {id: id}})
            },
            loadOptions() {
                this.$axios.post(this.$httpUrl + '/goods/listPage', {
                    pageSize: 500,
                    pageNum: 1,
                    param: {}
                }).then(res => res.data).then(res => {
                    if (res.code == 200) {
                        this.goodsOptions = res.data || []
                    }
                })
            },
            loadStorages() {
                this.$axios.get(this.$httpUrl + '/storage/list').then(res => res.data).then(res => {
                    if (res.code == 200) {
                        const map = {}
                        ;(res.data || []).forEach(item => {
                            map[item.id] = item.name
                        })
                        this.storageMap = map
                    }
                }).catch(() => {
                    // 药房名取不到时退回“药房{id}”占位，不影响主流程
                })
            },
            loadDetail(id) {
                this.$axios.get(this.$httpUrl + '/goods/detail?id=' + id).then(res => res.data).then(res => {
                    if (res.code != 200) {
                        this.$message.error(res.msg || '获取药品详情失败')
                        return
                    }
                    const data = res.data
                    this.goods = data.goods || {}
                    this.sameName = data.sameName || []
                    this.records = data.records || []
                    this.totalIn = data.totalIn || 0
                    this.totalOut = data.totalOut || 0
                    this.pending = data.pending || 0
                    this.totalStock = data.totalStock || 0
                    this.pickedId = this.goods.id
                }).catch(() => {
                    this.$message.error('网络错误，无法获取药品详情')
                })
            },
            syncFromRoute() {
                const id = this.$route.query.id
                this.goodsId = id ? id + '' : ''
                if (this.goodsId) {
                    this.loadDetail(this.goodsId)
                }
            }
        },
        watch: {
            '$route.query.id'() {
                this.syncFromRoute()
            }
        },
        mounted() {
            this.loadOptions()
            this.loadStorages()
            this.syncFromRoute()
        }
    }
</script>

<style scoped>
    .detail-container {
        padding: 0;
    }
    .card {
        background: var(--card);
        border: 1px solid var(--line);
        border-radius: 12px;
    }
    .empty-card {
        min-height: 380px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 10px;
        text-align: center;
        padding: 40px;
    }
    .empty-kicker {
        font-size: 10px;
        letter-spacing: 0.16em;
        color: var(--muted);
    }
    .empty-title {
        font-size: 22px;
        font-weight: 500;
        color: var(--ink);
        margin: 0;
    }

    .detail-head {
        margin-bottom: 18px;
    }
    .head-top {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 16px;
        margin-bottom: 18px;
    }
    .back-btn {
        appearance: none;
        border: none;
        background: none;
        padding: 0;
        font: inherit;
        font-size: 12px;
        color: var(--muted);
        cursor: pointer;
        transition: var(--transition);
    }
    .back-btn:hover {
        color: var(--ink);
    }
    .head-main {
        display: flex;
        align-items: flex-end;
        justify-content: space-between;
        gap: 24px;
    }
    .head-kicker {
        font-size: 10px;
        letter-spacing: 0.14em;
        color: var(--muted);
        margin-bottom: 6px;
    }
    .drug-name {
        font-size: 28px;
        font-weight: 500;
        color: var(--ink);
        margin: 0 0 8px;
        letter-spacing: 0.01em;
    }
    .drug-meta {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 6px 18px;
    }
    .meta-item {
        font-size: 12px;
        color: var(--ink-2);
    }
    .meta-label {
        color: var(--muted);
        margin-right: 6px;
        font-size: 11px;
    }
    .head-stock {
        text-align: right;
        flex-shrink: 0;
        padding-bottom: 2px;
    }
    .stock-label {
        display: block;
        font-size: 11px;
        color: var(--muted);
        margin-bottom: 2px;
    }
    .stock-line {
        display: flex;
        align-items: baseline;
        justify-content: flex-end;
        gap: 4px;
    }
    .stock-value {
        font-size: 34px;
        font-weight: 500;
        color: var(--ink);
        line-height: 1.1;
    }
    .stock-unit {
        font-size: 11px;
        color: var(--muted);
    }

    .stat-row {
        display: grid;
        grid-template-columns: repeat(4, minmax(0, 1fr));
        background: var(--card);
        border: 1px solid var(--line);
        border-radius: 12px;
        margin-bottom: 16px;
    }
    .stat {
        padding: 16px 20px;
        display: flex;
        flex-direction: column;
        border-left: 1px solid var(--line);
    }
    .stat:first-child {
        border-left: none;
    }
    .stat-label {
        font-size: 11px;
        color: var(--muted);
    }
    .stat-value {
        font-size: 24px;
        font-weight: 500;
        color: var(--ink);
        line-height: 1.3;
        margin: 3px 0 2px;
    }
    .stat-value.is-warn {
        color: var(--amber);
    }

    .split {
        display: grid;
        grid-template-columns: minmax(0, 1fr) 380px;
        gap: 16px;
        align-items: start;
    }
    .flow-card,
    .dist-card {
        padding: 20px 22px;
    }
    .card-head {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        gap: 12px;
        margin-bottom: 14px;
    }
    .card-title {
        font-size: 15px;
        font-weight: 500;
        color: var(--ink);
        margin: 0 0 4px;
    }
    .head-right {
        display: flex;
        align-items: center;
        gap: 8px;
    }
    .count {
        font-size: 11px;
        color: var(--muted);
    }

    .flow-list {
        margin-top: 8px;
    }
    .flow-item {
        display: grid;
        grid-template-columns: 78px 20px minmax(0, 1fr);
        gap: 10px;
    }
    .flow-time {
        padding-top: 10px;
        text-align: right;
    }
    .flow-date {
        display: block;
        font-size: 11px;
        color: var(--muted);
    }
    .flow-clock {
        display: block;
        font-size: 12px;
        color: var(--ink-2);
        margin-top: 2px;
    }
    .flow-rail {
        position: relative;
        display: flex;
        justify-content: center;
    }
    .flow-rail::before {
        content: '';
        position: absolute;
        top: 0;
        bottom: 0;
        width: 1px;
        background: var(--line);
    }
    .flow-item:first-child .flow-rail::before {
        top: 16px;
    }
    .flow-item:last-child .flow-rail::before {
        bottom: calc(100% - 16px);
    }
    .flow-dot {
        width: 7px;
        height: 7px;
        border-radius: 50%;
        background: var(--muted);
        margin-top: 15px;
        position: relative;
        z-index: 1;
        box-shadow: 0 0 0 4px var(--card);
    }
    .flow-dot.is-in {
        background: var(--accent);
    }
    .flow-dot.is-out {
        background: var(--amber);
    }
    .flow-body {
        padding: 8px 0 16px;
        border-bottom: 1px solid var(--line);
        min-width: 0;
    }
    .flow-item:last-child .flow-body {
        border-bottom: none;
    }
    .flow-main {
        display: flex;
        align-items: baseline;
        flex-wrap: wrap;
        gap: 8px;
    }
    .flow-type {
        font-size: 12px;
        color: var(--ink-2);
    }
    .flow-qty {
        font-size: 17px;
        font-weight: 500;
        color: var(--ink);
    }
    .flow-status {
        font-size: 11px;
        color: var(--muted);
        display: inline-flex;
        align-items: center;
    }
    .flow-status.is-pending {
        color: var(--amber);
    }
    .flow-status.is-rolled {
        color: var(--muted);
    }
    .flow-id {
        font-size: 10px;
        color: var(--muted);
        margin-left: auto;
    }
    .flow-meta {
        display: flex;
        flex-wrap: wrap;
        gap: 4px 14px;
        margin-top: 4px;
        font-size: 11px;
        color: var(--muted);
    }
    .flow-remark {
        margin-top: 4px;
        font-size: 11px;
        color: var(--muted);
    }

    .dist-list {
        margin-top: 6px;
    }
    .dist-row {
        padding: 12px 10px;
        border-radius: 8px;
        border-top: 1px solid var(--line);
    }
    .dist-row:first-child {
        border-top: none;
    }
    .dist-row.is-current {
        background: #F5F0E7;
    }
    .dist-main {
        display: flex;
        align-items: baseline;
        justify-content: space-between;
        gap: 8px;
    }
    .dist-name {
        font-size: 13px;
        color: var(--ink);
    }
    .dist-id {
        font-size: 10px;
        color: var(--muted);
    }
    .dist-bar {
        height: 3px;
        background: var(--raised);
        border-radius: 3px;
        overflow: hidden;
        margin: 8px 0;
    }
    .dist-bar span {
        display: block;
        height: 100%;
        background: #C3B8A6;
        border-radius: 3px;
    }
    .is-current .dist-bar span {
        background: var(--accent);
    }
    .dist-foot {
        display: flex;
        align-items: baseline;
        gap: 8px;
    }
    .dist-count {
        font-size: 14px;
        color: var(--ink);
    }
    .dist-pct {
        font-size: 10px;
        color: var(--muted);
    }
    .dist-link {
        appearance: none;
        border: none;
        background: none;
        padding: 0;
        margin-left: auto;
        font: inherit;
        font-size: 11px;
        color: var(--ink-2);
        text-decoration: underline;
        text-underline-offset: 3px;
        cursor: pointer;
    }
    .dist-link:hover {
        color: var(--accent);
    }
    .dist-now {
        margin-left: auto;
        font-size: 10px;
        color: var(--accent);
    }
    .empty-state {
        height: 260px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--muted);
        font-size: 12px;
    }
    .empty-state.small {
        height: 160px;
    }

    @media (max-width: 1100px) {
        .split {
            grid-template-columns: 1fr;
        }
        .stat-row {
            grid-template-columns: repeat(2, minmax(0, 1fr));
        }
        .stat:nth-child(3) {
            border-left: none;
        }
        .stat:nth-child(n+3) {
            border-top: 1px solid var(--line);
        }
    }
    @media (max-width: 720px) {
        .head-main {
            flex-direction: column;
            align-items: flex-start;
        }
        .head-stock {
            text-align: left;
        }
        .stock-line {
            justify-content: flex-start;
        }
    }
</style>
