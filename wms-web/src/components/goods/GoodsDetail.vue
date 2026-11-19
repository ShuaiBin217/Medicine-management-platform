<template>
    <div class="detail-container">
        <!-- 未带 id 进入（例如从左侧菜单点进来）：先选药品 -->
        <div v-if="!goodsId" class="card empty-card">
            <el-empty description="请选择要查看的药品，也可以从「药品管理」点击药品名直接进入">
                <el-select v-model="pickedId" filterable placeholder="输入药品名搜索" style="width: 320px" @change="goPicked">
                    <el-option
                            v-for="item in goodsOptions"
                            :key="item.id"
                            :label="item.name + '（' + storageName(item.storage) + ' 库存 ' + item.count + '）'"
                            :value="item.id">
                    </el-option>
                </el-select>
            </el-empty>
        </div>

        <template v-else>
            <!-- 药品概要 -->
            <div class="card head-card">
                <div class="head-left">
                    <el-button text class="back-btn" @click="back">← 返回</el-button>
                    <div class="drug-name">{{ goods.name }}</div>
                    <div class="drug-meta">
                        <el-tag size="small" type="info">{{ goods.storagename || '未分配药房' }}</el-tag>
                        <el-tag size="small" type="info">{{ goods.goodstypename || '未分类' }}</el-tag>
                        <span v-if="goods.remark" class="remark">备注：{{ goods.remark }}</span>
                    </div>
                </div>
                <el-select v-model="pickedId" filterable placeholder="切换药品" size="small"
                           style="width: 260px" @change="goPicked">
                    <el-option
                            v-for="item in goodsOptions"
                            :key="item.id"
                            :label="item.name + '（' + storageName(item.storage) + '）'"
                            :value="item.id">
                    </el-option>
                </el-select>
            </div>

            <!-- 汇总 -->
            <div class="stat-row">
                <div class="card stat-card">
                    <div class="stat-label">本药房库存</div>
                    <div class="stat-value primary">{{ goods.count == null ? 0 : goods.count }}</div>
                    <div class="stat-sub">{{ goods.storagename }}</div>
                </div>
                <div class="card stat-card">
                    <div class="stat-label">同名药品总库存</div>
                    <div class="stat-value">{{ totalStock }}</div>
                    <div class="stat-sub">共 {{ sameName.length }} 个药房在库</div>
                </div>
                <div class="card stat-card">
                    <div class="stat-label">累计入库</div>
                    <div class="stat-value success">{{ totalIn }}</div>
                    <div class="stat-sub">按流水汇总</div>
                </div>
                <div class="card stat-card">
                    <div class="stat-label">累计出库</div>
                    <div class="stat-value warning">{{ totalOut }}</div>
                    <div class="stat-sub">
                        <el-tag v-if="pending > 0" size="small" type="warning">待确认 {{ pending }}</el-tag>
                        <span v-else>无待确认单</span>
                    </div>
                </div>
            </div>

            <div class="split">
                <!-- 同药名分布 -->
                <div class="card dist-card">
                    <div class="card-title">同名药品分布</div>
                    <el-table :data="sameName" size="small" border stripe empty-text="暂无数据">
                        <el-table-column prop="storagename" label="药房" min-width="120">
                        </el-table-column>
                        <el-table-column prop="count" label="库存" width="90">
                        </el-table-column>
                        <el-table-column label="占同名总量" width="120">
                            <template #default="scope">
                                {{ totalStock > 0 ? ((scope.row.count || 0) * 100 / totalStock).toFixed(1) + '%' : '-' }}
                            </template>
                        </el-table-column>
                        <el-table-column label="操作" width="90">
                            <template #default="scope">
                                <el-button v-if="scope.row.id != goods.id" class="view-btn"
                                           @click="goPicked(scope.row.id)">查看</el-button>
                                <el-tag v-else size="small" type="success">当前</el-tag>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>

                <!-- 流转时间线 -->
                <div class="card flow-card">
                    <div class="card-title">
                        出入库流水
                        <span class="count">共 {{ records.length }} 条</span>
                        <el-tag v-if="isReadOnly" size="small" type="info" class="readonly-tag">仅显示本人经手的记录</el-tag>
                    </div>
                    <div class="timeline-wrap">
                        <el-empty v-if="records.length === 0" description="该药品暂无出入库记录"></el-empty>
                        <el-timeline v-else>
                            <el-timeline-item
                                    v-for="item in records"
                                    :key="item.id"
                                    :timestamp="item.createtime"
                                    :type="item.count >= 0 ? 'success' : 'warning'"
                                    placement="top">
                                <div class="flow-line">
                                    <el-tag size="small" :type="item.count >= 0 ? 'success' : 'warning'">
                                        {{ item.count >= 0 ? '入库' : '出库' }}
                                    </el-tag>
                                    <span class="flow-count">{{ Math.abs(item.count || 0) }}</span>
                                    <span class="flow-detail">
                                        {{ item.storagename }} · 操作人：{{ item.adminname || '-' }}
                                        <template v-if="item.count < 0"> · 取药人：{{ item.username || '-' }}</template>
                                    </span>
                                    <el-tag v-if="item.status === 0" size="small" type="warning">待确认</el-tag>
                                    <el-tag v-else size="small" type="success">已确认</el-tag>
                                </div>
                                <div v-if="item.remark" class="flow-remark">备注：{{ item.remark }}</div>
                            </el-timeline-item>
                        </el-timeline>
                    </div>
                </div>
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
                    // 药房名取不到时退回“药房{id}”占位，不影响页面主流程
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
        background: #FFFFFF;
        border-radius: 10px;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
        padding: 20px;
    }
    .empty-card {
        min-height: 320px;
        display: flex;
        align-items: center;
        justify-content: center;
    }
    .head-card {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 16px;
    }
    .head-left {
        display: flex;
        flex-direction: column;
        gap: 6px;
    }
    .back-btn {
        align-self: flex-start;
        padding: 0;
        color: #4A6CF7;
    }
    /* 查看按钮：浅底深字，避免文字与底色相近看不清 */
    .view-btn {
        height: 24px;
        padding: 0 10px;
        border-radius: 5px;
        border: 1px solid #E3EBFF;
        background: #F2F6FF;
        color: #3B5BDB;
        font-size: 12px;
    }
    .view-btn:hover {
        background: #E6EEFF;
        border-color: #CBDCFF;
        color: #2B48C4;
    }
    .drug-name {
        font-size: 22px;
        font-weight: 700;
        color: #1E2A3A;
    }
    .drug-meta {
        display: flex;
        align-items: center;
        gap: 8px;
    }
    .remark {
        color: #718096;
        font-size: 12px;
    }
    .stat-row {
        display: grid;
        grid-template-columns: repeat(4, minmax(0, 1fr));
        gap: 16px;
        margin-bottom: 16px;
    }
    .stat-card {
        padding: 16px 20px;
    }
    .stat-label {
        color: #718096;
        font-size: 13px;
    }
    .stat-value {
        font-size: 26px;
        font-weight: 700;
        color: #1E2A3A;
        margin: 6px 0 4px;
    }
    .stat-value.primary { color: #4A6CF7; }
    .stat-value.success { color: #22A06B; }
    .stat-value.warning { color: #E6A23C; }
    .stat-sub {
        color: #A0AEC0;
        font-size: 12px;
    }
    .split {
        display: grid;
        grid-template-columns: 4fr 6fr;
        gap: 16px;
        align-items: start;
    }
    .card-title {
        font-size: 15px;
        font-weight: 600;
        color: #1E2A3A;
        margin-bottom: 14px;
    }
    .card-title .count {
        color: #A0AEC0;
        font-size: 12px;
        font-weight: 400;
        margin-left: 6px;
    }
    .readonly-tag {
        margin-left: 8px;
    }
    .timeline-wrap {
        max-height: 460px;
        overflow: auto;
        padding-right: 8px;
    }
    .flow-line {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-wrap: wrap;
    }
    .flow-count {
        font-weight: 700;
        font-size: 15px;
        color: #1E2A3A;
    }
    .flow-detail {
        color: #4A5568;
        font-size: 13px;
    }
    .flow-remark {
        color: #A0AEC0;
        font-size: 12px;
        margin-top: 4px;
    }
    @media (max-width: 1200px) {
        .stat-row { grid-template-columns: repeat(2, minmax(0, 1fr)); }
        .split { grid-template-columns: 1fr; }
    }
</style>
