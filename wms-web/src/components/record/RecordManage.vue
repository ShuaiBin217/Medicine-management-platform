<template>
    <div class="manage-container">
        <div class="search-bar">
            <el-input v-model="name" placeholder="请输入药品名" suffix-icon="Search" style="width: 240px;"
                      @keyup.enter="loadPost"></el-input>
            <el-select v-model="storage" placeholder="请选择药房" style="margin-left: 8px; width: 180px;">
                <el-option
                        v-for="item in storageData"
                        :key="item.id"
                        :label="item.name"
                        :value="item.id">
                </el-option>
            </el-select>
            <el-select v-model="goodstype" placeholder="请选择药品分类" style="margin-left: 8px; width: 180px;">
                <el-option
                        v-for="item in goodstypeData"
                        :key="item.id"
                        :label="item.name"
                        :value="item.id">
                </el-option>
            </el-select>
            <el-select v-model="status" placeholder="状态" clearable style="margin-left: 8px; width: 130px;">
                <el-option label="待确认" value="0"></el-option>
                <el-option label="已确认" value="1"></el-option>
            </el-select>

            <el-button type="primary" style="margin-left: 8px;" @click="loadPost">查询</el-button>
            <el-button @click="resetParam">重置</el-button>

            <el-button type="danger" style="margin-left: 8px;" :loading="submitting" @click="confirmBatchDelete"
                       :disabled="selectedIds.length === 0" v-if="user.roleId!=2">
                批量删除（{{ selectedIds.length }}）
            </el-button>
        </div>
        <div class="table-card">
            <el-table :data="tableData"
                  
                  border
                  stripe
                  @selection-change="handleSelectionChange"
            >
                <el-table-column type="selection" width="48" fixed="left" v-if="user.roleId!=2">
                </el-table-column>
                <el-table-column prop="id" label="ID" width="54" align="center">
                </el-table-column>
                <el-table-column prop="goodsname" label="药品名" width="130" show-overflow-tooltip>
                </el-table-column>
                <el-table-column prop="storagename" label="药房" width="90">
                </el-table-column>
                <el-table-column prop="goodstypename" label="药品分类" width="100">
                </el-table-column>
                <el-table-column prop="adminname" label="操作人" width="88">
                </el-table-column>
                <el-table-column prop="username" label="取药人" width="88">
                </el-table-column>
                <el-table-column prop="count" label="数量" width="66" align="center">
                    <template #default="scope">
                        <span class="qty mono" :class="{ 'is-out': scope.row.count < 0 }">
                            {{ scope.row.count >= 0 ? '+' : '−' }}{{ Math.abs(scope.row.count) }}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column label="状态" width="92" align="center">
                    <template #default="scope">
                        <span class="flow-status" :class="scope.row.status === 0 ? 'is-pending' : 'is-done'">
                            <span class="status-dot" :class="scope.row.status === 0 ? 'is-warn' : 'is-ok'"></span>{{ scope.row.status === 0 ? '待确认' : '已确认' }}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column prop="createtime" label="操作时间" width="158">
                </el-table-column>
                <el-table-column prop="remark" label="备注" min-width="80" show-overflow-tooltip>
                </el-table-column>
                <el-table-column label="操作" width="150" fixed="right" align="center" v-if="user.roleId!=2">
                    <template #default="scope">
                        <div v-if="scope.row.status === 0" class="row-actions">
                            <el-button size="small" type="primary" :loading="submitting"
                                       @click="confirmDeduct(scope.row)">确认</el-button>
                            <el-popconfirm title="回滚后库存恢复、该单据删除，确定吗？"
                                           @confirm="rollbackDeduct(scope.row)">
                                <template #reference>
                                    <el-button size="small">回滚</el-button>
                                </template>
                            </el-popconfirm>
                        </div>
                        <span v-else class="muted">—</span>
                    </template>
                </el-table-column>
            </el-table>
            <el-pagination
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
                    :current-page="pageNum"
                    :page-sizes="[10, 20, 30]"
                    :page-size="pageSize"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="total">
            </el-pagination>
        </div>
    </div>
</template>

<script>
    export default {
        name: "RecordManage",
        data() {

            return {
                user : JSON.parse(sessionStorage.getItem('CurUser')),
                storageData:[],
                goodstypeData:[],
                tableData: [],
                pageSize:10,
                pageNum:1,
                total:0,
                name:'',
                storage:'',
                goodstype:'',
                status:'',
                selectedIds:[],
                submitting:false,
                centerDialogVisible:false,
                form:{
                    id:'',
                    name:'',
                    storage:'',
                    goodstype:'',
                    count:'',
                    remark:''
                },
            }
        },
        methods:{
            handleSelectionChange(val) {
                this.selectedIds = val.map(item => item.id)
            },
            confirmBatchDelete() {
                if (this.submitting) return
                if (this.selectedIds.length === 0) {
                    this.$message.warning('请先选择要删除的记录')
                    return
                }
                this.$confirm(
                    `确定要删除选中的 ${this.selectedIds.length} 条流转记录吗？删除后不可恢复。`,
                    '批量删除确认',
                    {
                        confirmButtonText: '确定删除',
                        cancelButtonText: '取消',
                        type: 'warning'
                    }
                ).then(() => {
                    this.submitting = true
                    this.batchDelete()
                }).catch(() => {})
            },
            batchDelete() {
                this.$axios.post(this.$httpUrl+'/record/batchDelete', {
                    ids: this.selectedIds,
                    operatorId: this.user.id,
                    operatorRole: this.user.roleId
                }).then(res => res.data).then(res => {
                    if (res.code == 200) {
                        this.$message.success('删除成功')
                        this.loadPost()
                    } else {
                        this.$message.error(res.msg || '删除失败')
                    }
                }).catch(() => {
                    this.$message.error('网络错误')
                }).finally(() => { this.submitting = false })
            },
            confirmDeduct(row){
                if(this.submitting) return
                this.submitting = true
                this.$axios.post(this.$httpUrl+'/record/confirmDeduct', null, {params:{id: row.id}}).then(res=>res.data).then(res=>{
                    if(res.code==200){
                        this.$message.success('已确认')
                        this.loadPost()
                    }else{
                        this.$message.error(res.msg || '确认失败')
                    }
                }).catch(()=>{
                    this.$message.error('网络错误')
                }).finally(()=>{ this.submitting = false })
            },
            rollbackDeduct(row){
                if(this.submitting) return
                this.submitting = true
                this.$axios.post(this.$httpUrl+'/record/rollbackDeduct', null, {params:{id: row.id}}).then(res=>res.data).then(res=>{
                    if(res.code==200){
                        this.$message.success('已回滚，库存已恢复')
                        this.loadPost()
                    }else{
                        this.$message.error(res.msg || '回滚失败')
                    }
                }).catch(()=>{
                    this.$message.error('网络错误')
                }).finally(()=>{ this.submitting = false })
            },
            formatStorage(row){
                let temp =  this.storageData.find(item=>{
                    return item.id == row.storage
                })

                return temp && temp.name
            },
            formatGoodstype(row){
                let temp =  this.goodstypeData.find(item=>{
                    return item.id == row.goodstype
                })

                return temp && temp.name
            },
            resetForm() {
                this.$refs.form.resetFields();
            },
            handleSizeChange(val) {
                this.pageNum=1
                this.pageSize=val
                this.loadPost()
            },
            handleCurrentChange(val) {
                this.pageNum=val
                this.loadPost()
            },
            resetParam(){
                this.name=''
                this.storage=''
                this.goodstype=''
                this.status=''
                this.pageNum=1
                this.loadPost()
            },
            loadStorage(){
                this.$axios.get(this.$httpUrl+'/storage/list').then(res=>res.data).then(res=>{
                    if(res.code==200){
                        this.storageData=res.data
                    }else{
                        alert('获取数据失败')
                    }
                })
            },
            loadGoodstype(){
                this.$axios.get(this.$httpUrl+'/goodstype/list').then(res=>res.data).then(res=>{
                    if(res.code==200){
                        this.goodstypeData=res.data
                    }else{
                        alert('获取数据失败')
                    }
                })
            },
            loadPost(){
                this.$axios.post(this.$httpUrl+'/record/listPage',{
                    pageSize:this.pageSize,
                    pageNum:this.pageNum,
                    param:{
                        name:this.name,
                        goodstype:this.goodstype+'',
                        storage:this.storage+'',
                        status:(this.status === '' || this.status == null) ? '' : this.status+'',
                        roleId:this.user.roleId+'',
                        userId:this.user.id+''
                    }
                }).then(res=>res.data).then(res=>{
                    if(res.code==200){
                        this.tableData=res.data
                        this.total=res.total
                    }else{
                        alert('获取数据失败')
                    }
                })
            },
        },
        beforeMount() {
            this.loadStorage()
            this.loadGoodstype()
            this.loadPost()
        }
    }
</script>

<style scoped>
    .manage-container {
        padding: 0;
    }
    .search-bar {
        background: var(--card);
        padding: 16px 20px;
        border-radius: 10px;
        margin-bottom: 16px;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
        display: flex;
        align-items: center;
    }
    .table-card {
        background: var(--card);
        padding: 20px;
        border-radius: 10px;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
    }
    .table-card :deep(.el-pagination) {
        display: flex;
        justify-content: center;
        margin-top: 16px;
    }
    .muted {
        color: var(--muted);
    }
    .row-actions {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
    }
    .row-actions :deep(.el-button + .el-button) {
        margin-left: 0;
    }
    .qty {
        font-weight: 500;
        color: var(--ink);
    }
    .qty.is-out {
        color: var(--muted);
    }
    .flow-status {
        display: inline-flex;
        align-items: center;
        font-size: 12px;
        color: var(--muted);
    }
    .flow-status.is-pending {
        color: var(--amber);
    }
</style>
