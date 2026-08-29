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

            <el-button type="primary" style="margin-left: 8px;" @click="add" v-if="user.roleId!=2">新增</el-button>
            <el-button type="primary" style="margin-left: 8px;" @click="inGoods" v-if="user.roleId!=2">采购</el-button>
            <el-button type="primary" style="margin-left: 8px;" @click="outGoods" v-if="user.roleId!=2">发药</el-button>
        </div>
        <div class="table-card">
            <el-table :data="tableData"
                  :header-cell-style="{ background: '#F7F8FC', color: '#4A5568', fontWeight: '600' }"
                  border
                  stripe
                  highlight-current-row
                  @current-change="selectCurrentChange"
            >
                <el-table-column prop="id" label="ID" width="60">
                </el-table-column>
                <el-table-column prop="name" label="药品名" width="180">
                </el-table-column>
                <el-table-column prop="storage" label="药房" width="180" :formatter="formatStorage">
                </el-table-column>
                <el-table-column prop="goodstype" label="药品分类" width="180" :formatter="formatGoodstype">
                </el-table-column>
                <el-table-column prop="count" label="数量" width="180">
                </el-table-column>
                <el-table-column prop="remark" label="备注">
                </el-table-column>
                <el-table-column prop="operate" label="操作" v-if="user.roleId!=2">
                    <template slot-scope="scope">
                        <el-button size="small" type="success" @click="mod(scope.row)">编辑</el-button>
                        <el-popconfirm
                                title="确定删除吗？"
                                @confirm="del(scope.row.id)"
                                style="margin-left: 8px;"
                        >
                            <el-button slot="reference" size="small" type="danger" >删除</el-button>
                        </el-popconfirm>
                    </template>
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

        <el-dialog
                title="药品维护"
                :visible.sync="centerDialogVisible"
                width="30%"
                center>

            <el-form ref="form" :rules="rules" :model="form" label-width="80px">
                <el-form-item label="药品名" prop="name">
                    <el-col :span="20">
                        <el-input v-model="form.name"></el-input>
                    </el-col>
                </el-form-item>
                <el-form-item label="药房" prop="storage">
                    <el-col :span="20">
                        <el-select v-model="form.storage" placeholder="请选择药房" style="margin-left: 0px;">
                            <el-option
                                    v-for="item in storageData"
                                    :key="item.id"
                                    :label="item.name"
                                    :value="item.id">
                            </el-option>
                        </el-select>

                    </el-col>
                </el-form-item>
                <el-form-item label="药品分类" prop="goodstype">
                    <el-col :span="20">
                        <el-select v-model="form.goodstype" placeholder="请选择药品分类" style="margin-left: 0px;">
                            <el-option
                                    v-for="item in goodstypeData"
                                    :key="item.id"
                                    :label="item.name"
                                    :value="item.id">
                            </el-option>
                        </el-select>
                    </el-col>
                </el-form-item>
                <el-form-item label="数量" prop="count">
                    <el-col :span="20">
                        <el-input v-model="form.count"></el-input>
                    </el-col>
                </el-form-item>
                <el-form-item label="备注" prop="remark">
                    <el-col :span="20">
                        <el-input type="textarea" v-model="form.remark"></el-input>
                    </el-col>
                </el-form-item>
            </el-form>
            <span slot="footer" class="dialog-footer">
    <el-button @click="centerDialogVisible = false">取 消</el-button>
    <el-button type="primary" @click="save">确 定</el-button>
  </span>
        </el-dialog>

        <el-dialog
                title="药品流转"
                :visible.sync="inDialogVisible"
                width="30%"
                center>

            <el-dialog
                    width="75%"
                    title="用户选择"
                    :visible.sync="innerVisible"
                    append-to-body>
                <SelectUser @doSelectUser="doSelectUser"></SelectUser>
                <span slot="footer" class="dialog-footer">
                    <el-button @click="innerVisible = false">取 消</el-button>
                    <el-button type="primary" @click="confirmUser">确 定</el-button>
                  </span>
            </el-dialog>

            <el-form ref="form1" :rules="rules1" :model="form1" label-width="80px">
                <el-form-item label="药品名">
                    <el-col :span="20">
                        <el-input v-model="form1.goodsname" readonly></el-input>
                    </el-col>
                </el-form-item>
                <el-form-item :label="form1.action==='1'?'经办人':'取药人'">
                    <el-col :span="20">
                        <el-input v-model="form1.username" readonly @click.native="form1.action==='2'?selectUser():null"></el-input>
                    </el-col>
                    <el-col :span="4" v-if="form1.action==='2'">
                        <el-button type="primary" @click="selectUser" style="margin-left: 8px;">选择用户</el-button>
                    </el-col>
                </el-form-item>
                <el-form-item label="数量" prop="count">
                    <el-col :span="20">
                        <el-input v-model="form1.count"></el-input>
                    </el-col>
                </el-form-item>
                <el-form-item label="备注" prop="remark">
                    <el-col :span="20">
                        <el-input type="textarea" v-model="form1.remark"></el-input>
                    </el-col>
                </el-form-item>
            </el-form>
            <span slot="footer" class="dialog-footer">
    <el-button @click="inDialogVisible = false">取 消</el-button>
    <el-button type="primary" @click="doInGoods">确 定</el-button>
  </span>
        </el-dialog>
    </div>
</template>


<script>
    import SelectUser from "../user/SelectUser";
    export default {
        name: "GoodsManage",
        components: {SelectUser},
        data() {
            let checkCount = (rule, value, callback) => {
                if(value>9999){
                    callback(new Error('数量输入过大'));
                }else{
                    callback();
                }
            };

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
                inDialogVisible:false,
                innerVisible:false,
                currentRow:{},
                tempUser:{},
                form:{
                    id:'',
                    name:'',
                    storage:'',
                    goodstype:'',
                    count:'',
                    remark:''
                },
                form1:{
                    goods:'',
                    goodsname:'',
                    count:'',
                    username:'',
                    userid:'',
                    adminId:'',
                    remark:'',
                    action:'1'
                },
                rules1: {

                },
                rules: {
                    name: [
                        {required: true, message: '请输入药品名', trigger: 'blur'}
                    ],
                    storage:[
                        {required: true, message: '请选择药房', trigger: 'blur'}
                    ],
                    goodstype:[
                        {required: true, message: '请选择药品分类', trigger: 'blur'}
                    ],
                    count: [
                        {required: true, message: '请输入数量', trigger: 'blur'},
                        {pattern: /^([1-9][0-9]*){1,4}$/,message: '数量必须为正整数字',trigger: "blur"},
                        {validator:checkCount,trigger: 'blur'}
                    ],
                }
            }
        },
        methods:{
            doSelectUser(val){
                console.log(val)
                this.tempUser = val
            },
            confirmUser(){
                this.form1.username = this.tempUser.name
                this.form1.userid = this.tempUser.id

                this.innerVisible = false
            },
            selectCurrentChange(val) {
                this.currentRow = val;
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
            resetInForm(){
                this.$refs.form1.resetFields();
            },
            del(id){
                console.log(id)

                this.$axios.get(this.$httpUrl+'/goods/del?id='+id).then(res=>res.data).then(res=>{
                    console.log(res)
                    if(res.code==200){

                        this.$message({
                            message: '操作成功！',
                            type: 'success'
                        });
                        this.loadPost()
                    }else{
                        this.$message({
                            message: '操作失败！',
                            type: 'error'
                        });
                    }

                })
            },
            mod(row){
                this.centerDialogVisible = true
                this.$nextTick(()=>{
                    //赋值到表单
                    this.form.id = row.id
                    this.form.name = row.name
                    this.form.storage = row.storage
                    this.form.goodstype = row.goodstype
                    this.form.count = row.count
                    this.form.remark = row.remark
                })
            },
            add(){

                this.centerDialogVisible = true
                this.$nextTick(()=>{
                    this.resetForm()

                    this.form.id = ''
                })

            },
            inGoods(){
                if(!this.currentRow.id){
                    alert('请选择记录');
                    return;
                }
                this.inDialogVisible = true
                this.$nextTick(()=>{
                    this.resetInForm()
                })

                this.form1.goodsname = this.currentRow.name
                this.form1.goods = this.currentRow.id
                this.form1.adminId=this.user.id
                this.form1.action='1'
                this.form1.username = this.user.name
                this.form1.userid = this.user.id
            },
            outGoods(){
                if(!this.currentRow.id){
                    alert('请选择记录');
                    return;
                }
                this.inDialogVisible = true
                this.$nextTick(()=>{
                    this.resetInForm()
                })

                this.form1.goodsname = this.currentRow.name
                this.form1.goods = this.currentRow.id
                this.form1.adminId=this.user.id
                this.form1.action='2'
                this.form1.username = ''
                this.form1.userid = ''

            },
            selectUser(){
                this.innerVisible=true
            },
            doSave(){
                this.$axios.post(this.$httpUrl+'/goods/save',this.form).then(res=>res.data).then(res=>{
                    console.log(res)
                    if(res.code==200){

                        this.$message({
                            message: '操作成功！',
                            type: 'success'
                        });
                        this.centerDialogVisible = false
                        this.loadPost()
                        this. resetForm()
                    }else{
                        this.$message({
                            message: '操作失败！',
                            type: 'error'
                        });
                    }

                })
            },
            doMod(){
                this.$axios.post(this.$httpUrl+'/goods/update',this.form).then(res=>res.data).then(res=>{
                    console.log(res)
                    if(res.code==200){

                        this.$message({
                            message: '操作成功！',
                            type: 'success'
                        });
                        this.centerDialogVisible = false
                        this.loadPost()
                        this. resetForm()
                    }else{
                        this.$message({
                            message: '操作失败！',
                            type: 'error'
                        });
                    }

                })
            },
            save(){
                this.$refs.form.validate((valid) => {
                    if (valid) {
                        if(this.form.id){
                            this.doMod();
                        }else{
                            this.doSave();
                        }
                    } else {
                        console.log('error submit!!');
                        return false;
                    }
                });

            },
            doInGoods(){
                this.$axios.post(this.$httpUrl+'/record/save',this.form1).then(res=>res.data).then(res=>{
                    console.log(res)
                    if(res.code==200){

                        this.$message({
                            message: '操作成功！',
                            type: 'success'
                        });
                        this.inDialogVisible = false
                        this.loadPost()
                        this. resetInForm()
                    }else{
                        this.$message({
                            message: '操作失败！',
                            type: 'error'
                        });
                    }

                })
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
            loadPost(){
                this.$axios.post(this.$httpUrl+'/goods/listPage',{
                    pageSize:this.pageSize,
                    pageNum:this.pageNum,
                    param:{
                        name:this.name,
                        goodstype:this.goodstype+'',
                        storage:this.storage+''
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
            }
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
        flex-wrap: wrap;
        gap: 0;
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
    .table-card >>> .el-table__body tr.current-row > td {
        background-color: #4A6CF7 !important;
        color: #FFFFFF !important;
    }
    .table-card >>> .el-table__body tr.current-row > td .el-tag {
        background-color: rgba(255, 255, 255, 0.2) !important;
        color: #FFFFFF !important;
        border-color: rgba(255, 255, 255, 0.3) !important;
    }
</style>