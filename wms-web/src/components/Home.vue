<template>
    <div class="home-container">
        <div class="home-welcome">
            <div class="welcome-text">
                <h1 class="welcome-title">欢迎你！{{user.name}}</h1>
                <p class="welcome-subtitle">这是药品管理系统的个人中心，您可以在此查看和管理您的信息</p>
            </div>
        </div>
        <div class="home-card">
            <el-descriptions  title="个人信息" :column="2" size="medium" border class="user-descriptions">
                <el-descriptions-item>
                    <template slot="label">
                        <i class="el-icon-s-custom"></i>
                        账号
                    </template>
                    {{user.no}}
                </el-descriptions-item>
                <el-descriptions-item>
                    <template slot="label">
                        <i class="el-icon-mobile-phone"></i>
                        电话
                    </template>
                    {{user.phone}}
                </el-descriptions-item>
                <el-descriptions-item>
                    <template slot="label">
                        <i class="el-icon-location-outline"></i>
                        性别
                    </template>
                    <el-tag
                            :type="user.sex === '1' ? 'primary' : 'danger'"
                            disable-transitions><i :class="user.sex==1?'el-icon-male':'el-icon-female'"></i>{{user.sex==1?"男":"女"}}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item>
                    <template slot="label">
                        <i class="el-icon-tickets"></i>
                        角色
                    </template>
                    <el-tag
                            type="success"
                            disable-transitions>{{user.roleId==0?"超级管理员":(user.roleId==1?"管理员":"用户")}}</el-tag>

                </el-descriptions-item>
            </el-descriptions>
        </div>

        <DateUtils></DateUtils>

        <div class="dashboard-section">
            <h2 class="dashboard-title">
                <i class="el-icon-data-analysis"></i>
                库存仪表盘
            </h2>

            <div class="stat-cards">
                <div class="stat-card stat-card--blue">
                    <div class="stat-card__icon">
                        <i class="el-icon-goods"></i>
                    </div>
                    <div class="stat-card__info">
                        <span class="stat-card__value">{{ totalGoods }}</span>
                        <span class="stat-card__label">药品总数</span>
                    </div>
                </div>
                <div class="stat-card stat-card--green">
                    <div class="stat-card__icon">
                        <i class="el-icon-office-building"></i>
                    </div>
                    <div class="stat-card__info">
                        <span class="stat-card__value">{{ storageData.length }}</span>
                        <span class="stat-card__label">药房总数</span>
                    </div>
                </div>
                <div class="stat-card stat-card--purple">
                    <div class="stat-card__icon">
                        <i class="el-icon-collection-tag"></i>
                    </div>
                    <div class="stat-card__info">
                        <span class="stat-card__value">{{ goodstypeData.length }}</span>
                        <span class="stat-card__label">分类总数</span>
                    </div>
                </div>
                <div class="stat-card stat-card--orange">
                    <div class="stat-card__icon">
                        <i class="el-icon-warning-outline"></i>
                    </div>
                    <div class="stat-card__info">
                        <span class="stat-card__value">{{ lowStockCount }}</span>
                        <span class="stat-card__label">库存预警</span>
                    </div>
                </div>
            </div>

            <div class="chart-row">
                <div class="chart-card">
                    <h3 class="chart-card__title">各药房药品库存</h3>
                    <div ref="storageChart" class="chart-container"></div>
                </div>
                <div class="chart-card">
                    <h3 class="chart-card__title">药品分类分布</h3>
                    <div ref="typeChart" class="chart-container"></div>
                </div>
            </div>

            <div class="chart-card" style="margin-top: 20px;">
                <h3 class="chart-card__title">
                    库存预警列表
                    <el-tag type="danger" size="small" style="margin-left: 8px;">数量 &le; 10</el-tag>
                </h3>
                <el-table :data="lowStockList" border stripe size="small"
                    :header-cell-style="{ background: '#F7F8FC', color: '#4A5568', fontWeight: '600' }"
                    v-if="lowStockList.length > 0"
                    max-height="300">
                    <el-table-column prop="name" label="药品名" width="200"></el-table-column>
                    <el-table-column label="药房" width="180">
                        <template slot-scope="scope">
                            {{ getStorageName(scope.row.storage) }}
                        </template>
                    </el-table-column>
                    <el-table-column label="分类" width="180">
                        <template slot-scope="scope">
                            {{ getGoodstypeName(scope.row.goodstype) }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="count" label="库存数量" width="120">
                        <template slot-scope="scope">
                            <el-tag :type="scope.row.count <= 5 ? 'danger' : 'warning'" size="small">
                                {{ scope.row.count }}
                            </el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="remark" label="备注"></el-table-column>
                </el-table>
                <el-empty v-else description="暂无库存预警" :image-size="60"></el-empty>
            </div>
        </div>
    </div>
</template>

<script>
    import DateUtils from "./DateUtils";
    import * as echarts from 'echarts';

    export default {
        name: "Home",
        components: {DateUtils},
        data() {
            return {
                user: {},
                goodsData: [],
                storageData: [],
                goodstypeData: [],
                storageChart: null,
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
                this.user = JSON.parse(sessionStorage.getItem('CurUser'))
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
                            this.renderStorageChart();
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
            renderStorageChart() {
                if (this.storageChart) {
                    this.storageChart.dispose();
                }
                this.storageChart = echarts.init(this.$refs.storageChart);
                let data = this.storageChartData;
                this.storageChart.setOption({
                    tooltip: {
                        trigger: 'axis',
                        axisPointer: { type: 'shadow' }
                    },
                    grid: {
                        left: '3%',
                        right: '4%',
                        bottom: '3%',
                        top: '12%',
                        containLabel: true
                    },
                    xAxis: {
                        type: 'category',
                        data: data.map(d => d.name),
                        axisLabel: {
                            color: '#4A5568',
                            fontSize: 12
                        },
                        axisLine: { lineStyle: { color: '#E2E8F0' } },
                        axisTick: { show: false }
                    },
                    yAxis: {
                        type: 'value',
                        axisLabel: { color: '#4A5568' },
                        splitLine: { lineStyle: { color: '#EDF2F7', type: 'dashed' } },
                        axisLine: { show: false },
                        axisTick: { show: false }
                    },
                    series: [{
                        type: 'bar',
                        data: data.map(d => d.value),
                        barWidth: '40%',
                        itemStyle: {
                            borderRadius: [6, 6, 0, 0],
                            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                                { offset: 0, color: '#4A6CF7' },
                                { offset: 1, color: '#6B8AFF' }
                            ])
                        },
                        emphasis: {
                            itemStyle: {
                                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                                    { offset: 0, color: '#3B5DE7' },
                                    { offset: 1, color: '#5A79F0' }
                                ])
                            }
                        }
                    }]
                });
            },
            renderTypeChart() {
                if (this.typeChart) {
                    this.typeChart.dispose();
                }
                this.typeChart = echarts.init(this.$refs.typeChart);
                let data = this.typeChartData;
                let colorPalette = ['#4A6CF7', '#48BB78', '#9F7AEA', '#ED8936', '#38B2AC', '#FC8181', '#667EEA', '#F6AD55', '#68D391', '#F687B3'];
                this.typeChart.setOption({
                    tooltip: {
                        trigger: 'item',
                        formatter: '{b}: {c} ({d}%)'
                    },
                    legend: {
                        orient: 'vertical',
                        right: '5%',
                        top: 'center',
                        textStyle: { color: '#4A5568', fontSize: 12 },
                        itemWidth: 12,
                        itemHeight: 12,
                        itemGap: 12
                    },
                    series: [{
                        type: 'pie',
                        radius: ['40%', '70%'],
                        center: ['35%', '50%'],
                        avoidLabelOverlap: false,
                        itemStyle: {
                            borderRadius: 6,
                            borderColor: '#fff',
                            borderWidth: 2
                        },
                        label: { show: false },
                        emphasis: {
                            label: {
                                show: true,
                                fontSize: 14,
                                fontWeight: 'bold'
                            }
                        },
                        labelLine: { show: false },
                        data: data.map((d, i) => ({
                            name: d.name,
                            value: d.value,
                            itemStyle: { color: colorPalette[i % colorPalette.length] }
                        }))
                    }]
                });
            },
            handleResize() {
                if (this.storageChart) this.storageChart.resize();
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
        beforeDestroy() {
            window.removeEventListener('resize', this.handleResize);
            if (this.storageChart) {
                this.storageChart.dispose();
                this.storageChart = null;
            }
            if (this.typeChart) {
                this.typeChart.dispose();
                this.typeChart = null;
            }
        }
    }
</script>

<style scoped>
    .home-container {
        height: 100%;
        padding: 0;
        margin: 0;
        background-color: #F0F2F5;
    }
    .home-welcome {
        background: linear-gradient(135deg, #4A6CF7, #6B8AFF);
        padding: 40px 32px 32px;
        border-radius: 12px;
        margin-bottom: 20px;
        box-shadow: 0 4px 16px rgba(74, 108, 247, 0.25);
    }
    .welcome-title {
        font-size: 28px;
        font-weight: 700;
        color: #FFFFFF;
        margin: 0 0 8px;
        letter-spacing: 1px;
    }
    .welcome-subtitle {
        font-size: 14px;
        color: rgba(255, 255, 255, 0.85);
        margin: 0;
    }
    .home-card {
        background: #FFFFFF;
        border-radius: 12px;
        padding: 24px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    }
    .user-descriptions {
        width: 100%;
    }

    .dashboard-section {
        margin-top: 24px;
    }
    .dashboard-title {
        font-size: 20px;
        font-weight: 700;
        color: #1A202C;
        margin: 0 0 20px;
        display: flex;
        align-items: center;
        gap: 8px;
    }
    .dashboard-title i {
        color: #4A6CF7;
        font-size: 22px;
    }

    .stat-cards {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 16px;
        margin-bottom: 20px;
    }
    .stat-card {
        background: #FFFFFF;
        border-radius: 12px;
        padding: 20px 24px;
        display: flex;
        align-items: center;
        gap: 16px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
        transition: transform 0.2s, box-shadow 0.2s;
    }
    .stat-card:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
    }
    .stat-card__icon {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
        color: #FFFFFF;
        flex-shrink: 0;
    }
    .stat-card--blue .stat-card__icon {
        background: linear-gradient(135deg, #4A6CF7, #6B8AFF);
    }
    .stat-card--green .stat-card__icon {
        background: linear-gradient(135deg, #48BB78, #68D391);
    }
    .stat-card--purple .stat-card__icon {
        background: linear-gradient(135deg, #9F7AEA, #B794F4);
    }
    .stat-card--orange .stat-card__icon {
        background: linear-gradient(135deg, #ED8936, #F6AD55);
    }
    .stat-card__info {
        display: flex;
        flex-direction: column;
    }
    .stat-card__value {
        font-size: 28px;
        font-weight: 700;
        color: #1A202C;
        line-height: 1.2;
    }
    .stat-card__label {
        font-size: 13px;
        color: #718096;
        margin-top: 4px;
    }

    .chart-row {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 20px;
    }
    .chart-card {
        background: #FFFFFF;
        border-radius: 12px;
        padding: 20px 24px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    }
    .chart-card__title {
        font-size: 16px;
        font-weight: 600;
        color: #1A202C;
        margin: 0 0 16px;
        display: flex;
        align-items: center;
    }
    .chart-container {
        width: 100%;
        height: 300px;
    }

    @media (max-width: 1024px) {
        .stat-cards {
            grid-template-columns: repeat(2, 1fr);
        }
        .chart-row {
            grid-template-columns: 1fr;
        }
    }
    @media (max-width: 600px) {
        .stat-cards {
            grid-template-columns: 1fr;
        }
    }
</style>