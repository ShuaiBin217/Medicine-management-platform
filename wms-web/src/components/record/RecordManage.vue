<template>
    <div class="manage-container">
        <div class="search-bar">
            <el-input v-model="name" placeholder="请输入药品名" suffix-icon="el-icon-search" style="width: 200px;"
                      @keyup.enter.native="loadPost"></el-input>
            <el-select v-model="storage" placeholder="请选择药房" style="margin-left: 8px;">
                <el-option
                        v-for="item in storageData"
                        :key="item.id"
                        :label="item.name"
                        :value="item.id">
                </el-option>
            </el-select>
            <el-select v-model="goodstype" placeholder="请选择药品分类" style="margin-left: 8px;">
                <el-option
                        v-for="item in goodstypeData"
                        :key="item.id"
                        :label="item.name"
                        :value="item.id">
                </el-option>
            </el-select>

            <el-button type="primary" style="margin-left: 8px;" @click="loadPost">查询</el-button>
            <el-button type="success" @click="resetParam">重置</el-button>


        </div>
        <div class="table-card">
            <el-table :data="tableData"
                  :header-cell-style="{ background: '#F7F8FC', color: '#4A5568', fontWeight: '600' }"
                  border
                  stripe
            >
                <el-table-column prop="id" label="ID" width="60">
                </el-table-column>
                <el-table-column prop="goodsname" label="药品名" width="180">
                </el-table-column>
                <el-table-column prop="storagename" label="药房" width="180">
                </el-table-column>
                <el-table-column prop="goodstypename" label="药品分类" width="180">
                </el-table-column>
                <el-table-column prop="adminname" label="操作人" width="180">
                </el-table-column>
                <el-table-column prop="username" label="取药人" width="180">
                </el-table-column>
                <el-table-column prop="count" label="数量" width="180">
                </el-table-column>
                <el-table-column prop="createtime" label="操作时间" width="180">
                </el-table-column>
                <el-table-column prop="remark" label="备注">
                </el-table-column>
            </el-table>
            <el-pagination
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
                    :current-page="pageNum"
                    :page-sizes="[5, 10, 20,30]"
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
                console.log(`每页 ${val} 条`);
                this.pageNum=1
                this.pageSize=val
                this.loadPost()
            },
            handleCurrentChange(val) {
                console.log(`当前页: ${val}`);
                this.pageNum=val
                this.loadPost()
            },
            resetParam(){
                this.name=''
                this.storage=''
                this.goodstype=''
            },
            loadStorage(){
                this.$axios.get(this.$httpUrl+'/storage/list').then(res=>res.data).then(res=>{
                    console.log(res)
                    if(res.code==200){
                        this.storageData=res.data
                    }else{
                        alert('获取数据失败')
                    }

                })
            },
            loadGoodstype(){
                this.$axios.get(this.$httpUrl+'/goodstype/list').then(res=>res.data).then(res=>{
                    console.log(res)
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
                        roleId:this.user.roleId+'',
                        userId:this.user.id+''
                    }
                }).then(res=>res.data).then(res=>{
                    console.log(res)
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
        background: #FFFFFF;
        padding: 16px 20px;
        border-radius: 10px;
        margin-bottom: 16px;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
        display: flex;
        align-items: center;
    }
    .table-card {
        background: #FFFFFF;
        padding: 20px;
        border-radius: 10px;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
    }
    .table-card >>> .el-pagination {
        display: flex;
        justify-content: center;
        margin-top: 16px;
    }
</style>