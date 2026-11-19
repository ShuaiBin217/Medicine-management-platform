<template>
    <div class="home">
        <header class="page-intro">
            <div class="intro-main">
                <div class="eyebrow mono">DASHBOARD</div>
                <h1 class="intro-title">库存仪表盘</h1>
                <p class="intro-desc">共 {{ storageData.length }} 个药房 · {{ goodstypeData.length }} 个分类 · {{ totalGoods }} 条药品库存</p>
            </div>
            <div class="intro-user">
                <div class="user-line">
                    <span class="user-name">{{ user.name }}</span>
                    <span class="role-badge">{{ roleText }}</span>
                </div>
                <div class="user-meta mono">
                    {{ user.no }}<template v-if="user.phone"> · {{ user.phone }}</template>
                </div>
            </div>
        </header>

        <section class="stat-row">
            <div class="stat">
                <span class="stat-label">药品总数</span>
                <span class="stat-value mono">{{ totalGoods }}</span>
                <span class="stat-note">在库药品条目</span>
            </div>
            <div class="stat">
                <span class="stat-label">药房总数</span>
                <span class="stat-value mono">{{ storageData.length }}</span>
                <span class="stat-note">独立库存台账</span>
            </div>
            <div class="stat">
                <span class="stat-label">分类总数</span>
                <span class="stat-value mono">{{ goodstypeData.length }}</span>
                <span class="stat-note">药品分类维度</span>
            </div>
            <div class="stat">
                <span class="stat-label">库存预警</span>
                <span class="stat-value mono" :class="{ 'is-warn': lowStockCount > 0 }">{{ lowStockCount }}</span>
                <span class="stat-note">数量 ≤ 10 的条目</span>
            </div>
        </section>

        <section class="chart-card">
            <div class="card-head">
                <div>
                    <h2 class="card-title">分类库存构成</h2>
                    <p class="card-desc">按药品分类汇总当前在库数量</p>
                </div>
                <span class="card-tag mono">QTY / 分类</span>
            </div>
            <div v-if="typeChartData.length" ref="typeChart" class="chart-main"></div>
            <div v-else class="empty-state">暂无库存数据</div>
        </section>

        <div class="lower-grid">
            <section class="card ledger">
                <div class="card-head">
                    <div>
                        <h2 class="card-title">药房库存</h2>
                        <p class="card-desc">各药房当前在库合计</p>
                    </div>
                </div>
                <div class="ledger-list" v-if="storageRows.length">
                    <div v-for="row in storageRows" :key="row.id" class="ledger-row">
                        <div class="ledger-name">
                            <span class="status-dot" :class="row.low ? 'is-warn' : 'is-ok'"></span>{{ row.name }}
                        </div>
                        <div class="ledger-bar"><span :style="{ width: row.percent + '%' }"></span></div>
                        <div class="ledger-count mono">{{ row.value }}</div>
                        <div class="ledger-low" :class="{ 'is-warn': row.low }">
                            {{ row.low ? row.low + ' 项偏低' : '正常' }}
                        </div>
                    </div>
                </div>
                <div v-else class="empty-state small">暂无药房数据</div>
            </section>

            <section class="card low-stock">
                <div class="card-head">
                    <div>
                        <h2 class="card-title">库存预警</h2>
                        <p class="card-desc">数量 ≤ 10 的药品，优先补货</p>
                    </div>
                    <span v-if="lowStockCount" class="warn-count mono">{{ lowStockCount }}</span>
                </div>
                <div v-if="lowStockList.length" class="low-list">
                    <div v-for="item in lowStockList" :key="item.id" class="low-row">
                        <div class="low-name">
                            {{ item.name }}
                            <span class="low-loc">{{ getStorageName(item.storage) }} · {{ getGoodstypeName(item.goodstype) }}</span>
                        </div>
                        <div class="low-count" :class="item.count <= 5 ? 'is-danger' : 'is-warn'">
                            <span class="mono">{{ item.count }}</span><span class="unit">件</span>
                        </div>
                    </div>
                </div>
                <div v-else class="empty-state small">当前没有低库存药品</div>
            </section>
        </div>

        <footer class="page-foot">
            <span class="mono">DRUGWMS</span>
            <span>数据来自当前库存台账</span>
        </footer>
    </div>
</template>

<script>
    import * as echarts from 'echarts';

    export default {
        name: "Home",
        data() {
            return {
                user: {},
                goodsData: [],
                storageData: [],
                goodstypeData: [],
                typeChart: null
            }
        },
        computed: {
            totalGoods() {
                return this.goodsData.length;
            },
            lowStockCount() {
                return this.goodsData.filter(g => g.count <= 10).length;
            },
            lowStockList() {
                return this.goodsData
                    .filter(g => g.count <= 10)
                    .sort((a, b) => a.count - b.count);
            },
            roleText() {
                const role = String(this.user.roleId)
                if (role === '0') return '超级管理员'
                if (role === '1') return '管理员'
                return '普通用户'
            },
            storageChartData() {
                let map = {};
                this.storageData.forEach(s => { map[s.id] = { name: s.name, value: 0 }; });
                this.goodsData.forEach(g => {
                    if (map[g.storage]) {
                        map[g.storage].value += Number(g.count) || 0;
                    }
                });
                return Object.values(map);
            },
            storageRows() {
                const total = this.storageChartData.reduce((sum, s) => sum + s.value, 0)
                return this.storageData.map(s => {
                    const items = this.goodsData.filter(g => g.storage == s.id)
                    const value = items.reduce((sum, g) => sum + (Number(g.count) || 0), 0)
                    return {
                        id: s.id,
                        name: s.name,
                        value: value,
                        low: items.filter(g => g.count <= 10).length,
                        percent: total > 0 ? Math.max(4, Math.round(value * 100 / total)) : 0
                    }
                }).sort((a, b) => b.value - a.value)
            },
            typeChartData() {
                let map = {};
                this.goodstypeData.forEach(t => { map[t.id] = { name: t.name, value: 0 }; });
                this.goodsData.forEach(g => {
                    if (map[g.goodstype]) {
                        map[g.goodstype].value += Number(g.count) || 0;
                    }
                });
                return Object.values(map).filter(d => d.value > 0);
            }
        },
        methods: {
            init() {
                this.user = JSON.parse(sessionStorage.getItem('CurUser')) || {}
            },
            getStorageName(id) {
                let item = this.storageData.find(s => s.id == id);
                return item ? item.name : '';
            },
            getGoodstypeName(id) {
                let item = this.goodstypeData.find(t => t.id == id);
                return item ? item.name : '';
            },
            loadGoods() {
                this.$axios.post(this.$httpUrl + '/goods/listPage', {
                    pageSize: 9999,
                    pageNum: 1,
                    param: { name: '', goodstype: '', storage: '' }
                }).then(res => res.data).then(res => {
                    if (res.code == 200) {
                        this.goodsData = res.data;
                        this.$nextTick(() => {
                            this.renderTypeChart();
                        });
                    }
                });
            },
            loadStorage() {
                this.$axios.get(this.$httpUrl + '/storage/list').then(res => res.data).then(res => {
                    if (res.code == 200) {
                        this.storageData = res.data;
                    }
                });
            },
            loadGoodstype() {
                this.$axios.get(this.$httpUrl + '/goodstype/list').then(res => res.data).then(res => {
                    if (res.code == 200) {
                        this.goodstypeData = res.data;
                    }
                });
            },
            renderTypeChart() {
                if (this.typeChart) {
                    this.typeChart.dispose();
                }
                if (!this.$refs.typeChart) return
                this.typeChart = echarts.init(this.$refs.typeChart);
                let data = [...this.typeChartData].sort((a, b) => a.value - b.value);
                this.typeChart.setOption({
                    tooltip: {
                        trigger: 'axis',
                        axisPointer: { type: 'shadow' },
                        backgroundColor: '#FDFBF7',
                        borderColor: 'rgba(31,27,22,0.12)',
                        textStyle: { color: '#1F1B16', fontSize: 12 },
                        formatter: (params) => params[0].name + '<br/>' + params[0].value + ' 件'
                    },
                    grid: {
                        left: 8,
                        right: 64,
                        top: 10,
                        bottom: 8,
                        containLabel: true
                    },
                    xAxis: {
                        type: 'value',
                        axisLabel: { color: '#7B7266', fontSize: 11 },
                        axisLine: { show: false },
                        axisTick: { show: false },
                        splitLine: { lineStyle: { color: '#EDE6DA', type: 'dashed' } }
                    },
                    yAxis: {
                        type: 'category',
                        data: data.map(d => d.name),
                        axisLabel: { color: '#5E554A', fontSize: 12 },
                        axisLine: { show: false },
                        axisTick: { show: false }
                    },
                    series: [{
                        name: '库存数量',
                        type: 'bar',
                        data: data.map(d => d.value),
                        barWidth: 10,
                        itemStyle: {
                            color: '#CFC5B4',
                            borderRadius: [0, 4, 4, 0]
                        },
                        emphasis: {
                            itemStyle: { color: '#1D6B4F' }
                        },
                        label: {
                            show: true,
                            position: 'right',
                            color: '#1F1B16',
                            fontSize: 11,
                            fontFamily: 'ui-monospace, Consolas, monospace'
                        }
                    }]
                });
            },
            handleResize() {
                if (this.typeChart) this.typeChart.resize();
            }
        },
        created() {
            this.init();
        },
        mounted() {
            this.loadStorage();
            this.loadGoodstype();
            this.loadGoods();
            window.addEventListener('resize', this.handleResize);
        },
        beforeUnmount() {
            window.removeEventListener('resize', this.handleResize);
            if (this.typeChart) {
                this.typeChart.dispose();
                this.typeChart = null;
            }
        }
    }
</script>

<style scoped>
    .home {
        padding-bottom: 8px;
    }
    .page-intro {
        display: flex;
        align-items: flex-end;
        justify-content: space-between;
        gap: 24px;
        margin-bottom: 24px;
    }
    .eyebrow {
        font-size: 10px;
        letter-spacing: 0.16em;
        color: var(--muted);
        margin-bottom: 8px;
    }
    .intro-title {
        font-size: 26px;
        font-weight: 500;
        color: var(--ink);
        margin: 0 0 6px;
        letter-spacing: 0.01em;
    }
    .intro-desc {
        font-size: 12px;
        color: var(--muted);
        margin: 0;
    }
    .intro-user {
        text-align: right;
        flex-shrink: 0;
    }
    .user-line {
        display: flex;
        align-items: center;
        justify-content: flex-end;
        gap: 8px;
    }
    .user-name {
        font-size: 14px;
        font-weight: 500;
        color: var(--ink);
    }
    .role-badge {
        font-size: 11px;
        color: var(--ink-2);
        background: var(--raised);
        border: 1px solid var(--line);
        border-radius: 999px;
        padding: 1px 8px;
    }
    .user-meta {
        font-size: 11px;
        color: var(--muted);
        margin-top: 5px;
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
        padding: 18px 20px;
        display: flex;
        flex-direction: column;
        border-left: 1px solid var(--line);
    }
    .stat:first-child {
        border-left: none;
    }
    .stat-label {
        font-size: 12px;
        color: var(--muted);
    }
    .stat-value {
        font-size: 28px;
        font-weight: 500;
        color: var(--ink);
        line-height: 1.25;
        margin: 4px 0 2px;
    }
    .stat-value.is-warn {
        color: var(--amber);
    }
    .stat-note {
        font-size: 11px;
        color: var(--muted);
    }

    .chart-card,
    .card {
        background: var(--card);
        border: 1px solid var(--line);
        border-radius: 12px;
    }
    .chart-card {
        padding: 20px 22px 12px;
        margin-bottom: 16px;
    }
    .card-head {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        gap: 16px;
    }
    .card-title {
        font-size: 15px;
        font-weight: 500;
        color: var(--ink);
        margin: 0 0 4px;
    }
    .card-desc {
        font-size: 11px;
        color: var(--muted);
        margin: 0;
    }
    .card-tag {
        font-size: 10px;
        color: var(--muted);
        letter-spacing: 0.08em;
        padding-top: 2px;
    }
    .chart-main {
        width: 100%;
        height: 300px;
        margin-top: 10px;
    }
    .lower-grid {
        display: grid;
        grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
        gap: 16px;
    }
    .ledger,
    .low-stock {
        padding: 20px 22px;
    }
    .ledger-list {
        margin-top: 18px;
    }
    .ledger-row {
        display: grid;
        grid-template-columns: minmax(80px, 1.1fr) minmax(60px, 2fr) 56px 64px;
        align-items: center;
        gap: 12px;
        padding: 11px 0;
        border-top: 1px solid var(--line);
    }
    .ledger-row:first-child {
        border-top: none;
    }
    .ledger-name {
        font-size: 13px;
        color: var(--ink);
        white-space: nowrap;
    }
    .ledger-bar {
        height: 4px;
        background: var(--raised);
        border-radius: 4px;
        overflow: hidden;
    }
    .ledger-bar span {
        display: block;
        height: 100%;
        background: #BFB3A0;
        border-radius: 4px;
    }
    .ledger-count {
        text-align: right;
        font-size: 13px;
        color: var(--ink);
    }
    .ledger-low {
        text-align: right;
        font-size: 11px;
        color: var(--muted);
        white-space: nowrap;
    }
    .ledger-low.is-warn {
        color: var(--amber);
    }
    .low-stock .card-head {
        margin-bottom: 18px;
    }
    .warn-count {
        font-size: 12px;
        color: var(--amber);
        background: var(--amber-soft);
        border: 1px solid rgba(180, 83, 9, 0.18);
        border-radius: 999px;
        padding: 2px 9px;
    }
    .low-list {
        display: flex;
        flex-direction: column;
    }
    .low-row {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 12px;
        padding: 10px 0;
        border-top: 1px solid var(--line);
    }
    .low-row:first-child {
        border-top: none;
        padding-top: 0;
    }
    .low-name {
        font-size: 13px;
        color: var(--ink);
        min-width: 0;
    }
    .low-loc {
        display: block;
        font-size: 11px;
        color: var(--muted);
        margin-top: 3px;
    }
    .low-count {
        display: flex;
        align-items: baseline;
        gap: 3px;
        flex-shrink: 0;
    }
    .low-count .mono {
        font-size: 16px;
    }
    .low-count .unit {
        font-size: 10px;
        color: var(--muted);
    }
    .low-count.is-warn {
        color: var(--amber);
    }
    .low-count.is-danger {
        color: var(--red);
    }
    .empty-state {
        height: 260px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--muted);
        font-size: 12px;
        border-top: 1px dashed var(--line);
        margin-top: 16px;
    }
    .empty-state.small {
        height: 180px;
        margin-top: 18px;
    }
    .page-foot {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 18px 2px 0;
        font-size: 10px;
        color: var(--muted);
        letter-spacing: 0.04em;
    }
    @media (max-width: 1100px) {
        .stat-row {
            grid-template-columns: repeat(2, minmax(0, 1fr));
        }
        .stat:nth-child(3) {
            border-left: none;
        }
        .stat:nth-child(n+3) {
            border-top: 1px solid var(--line);
        }
        .lower-grid {
            grid-template-columns: 1fr;
        }
    }
    @media (max-width: 720px) {
        .page-intro {
            flex-direction: column;
            align-items: flex-start;
        }
        .intro-user {
            text-align: left;
        }
        .user-line {
            justify-content: flex-start;
        }
        .stat-row {
            grid-template-columns: 1fr;
        }
        .stat {
            border-left: none;
        }
        .stat + .stat {
            border-top: 1px solid var(--line);
        }
    }
</style>
