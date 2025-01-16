<template>
<div>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="模板名称" prop="tmplName">
        <el-input
            v-model="queryParams.tmplName"
            placeholder="请输入模板名称"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <!-- <el-form-item label="创建人" prop="createBy">
        <el-input
            v-model="queryParams.createBy"
            placeholder="请输入创建人"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item> -->
      <!-- <el-form-item label="创建时间" prop="createTime">
        <el-date-picker clearable
                        v-model="queryParams.createTime"
                        type="date"
                        value-format="YYYY-MM-DD"
                        placeholder="请选择创建时间">
        </el-date-picker>
      </el-form-item> -->
      <!-- <el-form-item label="更新人" prop="updateBy">
        <el-input
            v-model="queryParams.updateBy"
            placeholder="请输入更新人"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item> -->
      <!-- <el-form-item label="更新时间" prop="updateTime">
        <el-date-picker clearable
                        v-model="queryParams.updateTime"
                        type="date"
                        value-format="YYYY-MM-DD"
                        placeholder="请选择更新时间">
        </el-date-picker>
      </el-form-item> -->
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
            type="primary"
            plain
            icon="Plus"
            @click="handleAdd"
            v-hasPermi="['biz:dirTemplate:add']"
        >新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="success"
            plain
            icon="Edit"
            :disabled="single"
            @click="handleUpdate"
            v-hasPermi="['biz:dirTemplate:edit']"
        >修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="danger"
            plain
            icon="Delete"
            :disabled="multiple"
            @click="handleDelete"
            v-hasPermi="['biz:dirTemplate:remove']"
        >删除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="warning"
            plain
            icon="Download"
            @click="handleExport"
            v-hasPermi="['biz:dirTemplate:export']"
        >导出
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="dirTemplateList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="模板ID" align="center" prop="tmplId"/>
      <el-table-column label="模板名称" align="center" prop="tmplName"/>
      <el-table-column label="创建人" align="center" prop="createBy"/>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新人" align="center" prop="updateBy"/>
      <el-table-column label="更新时间" align="center" prop="updateTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.updateTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark"/>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['biz:dirTemplate:edit']">修改
          </el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['biz:dirTemplate:remove']">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
        v-show="total>0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
    />

    <!-- 添加或修改目录模板对话框 -->
    <el-dialog :title="title" :close-on-click-modal="false"  v-model="open" width="500px" append-to-body @close="cancelAddTemp">
      <el-form ref="dirTemplateRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="模板名称" prop="tmplName">
          <el-input v-model="tempTitle" placeholder="请输入模板名称"/>
        </el-form-item>
        <el-form-item label="模板内容" prop="tmplContent">
           <el-tree
            :data="tempData"
            default-expand-all
            node-key="id"
            style="width:100%;height:300px;overflow-y: auto;"
            :expand-on-click-node="false"
          >
            <template #default="{ node, data }">
              <span class="custom-tree-node">
                <span>{{ node.label }}</span>
                <span>
                  <a class="btm" @click="append(data)"> 增加 </a>
                  <a class="up_btn" v-if="data.id != 1" @click="updataNode(data)"> 修改 </a>
                  <a class="del_btn" v-if="data.id != 1" style="margin-left: 8px" @click="remove(node, data)"> 删除 </a>
                </span>
              </span>
            </template>
          </el-tree>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm" :disabled="addDisabled">确 定</el-button>
          <el-button @click="cancelAddTemp">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog :close-on-click-modal="false"  title="目录名称" v-model="showMenuName" width="500px">
       <el-input v-model="newName" maxlength="32" placeholder="请输入目录名称"/>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitMenuName">确 定</el-button>
          <el-button @click="cancelMenuName">取 消</el-button>
        </div>
      </template>

    </el-dialog>
  </div>
</div>
</template>

<script setup name="DirTemplate">
import {
  addDirTemplate,
  delDirTemplate,
  getDirTemplate,
  listDirTemplate,
  updateDirTemplate
} from "@/api/biz/dirTemplate";
import { ElMessage} from 'element-plus'

const {proxy} = getCurrentInstance();

const dirTemplateList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const showMenuName = ref(false)
const newName = ref()
const tempId = ref(1000)
const upNodeData = ref()
const tempTitle = ref('')
const addDisabled = ref(false);
const tempData = ref([
  {id:1,label:tempTitle || '请输入模板名称',children:[

  ]}
])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    tmplName: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
  },
  rules: {}
});

const {queryParams, form, rules} = toRefs(data);

/** 查询目录模板列表 */
function getList() {
  loading.value = true;
  listDirTemplate(queryParams.value).then(response => {
    dirTemplateList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 表单重置
function reset() {
  form.value = {
    tmplId: null,
    tmplName: null,
    tmplContent: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  };
  proxy.resetForm("dirTemplateRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.tmplId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加目录模板";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _tmplId = row.tmplId || ids.value
  getDirTemplate(_tmplId).then(response => {
    console.log('response',response);
    form.value = response.data;
    const arr = JSON.parse(response.data.tmplContent)
    const getNewtempId = (arr)=>{
      arr.map(item=>{
        if(item.id >= tempId.value){
          console.log('1',item.id);
          tempId.value = item.id +1
        }
        if(item.children?.length>0){
          getNewtempId(item.children)
        }
      })
    }
    getNewtempId(arr)
    tempTitle.value = response.data.tmplName
    
    tempData.value = [{id:1,label:tempTitle,children:[]}]
    tempData.value[0].children = arr
    open.value = true;
    title.value = "修改目录模板";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["dirTemplateRef"].validate(valid => {
    if(!tempTitle.value){
      return ElMessage({
                message: "模板名不能为空",
                type: "error"
            })
    }
    const str = JSON.stringify(tempData.value[0].children)
    form.value.tmplContent = str
    form.value.tmplName = tempTitle.value
    if (valid) {
      addDisabled.value = true
      if (form.value.tmplId != null) {
        updateDirTemplate(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          addDisabled.value = false
          getList();
        });
      } else {
        addDirTemplate(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          addDisabled.value = false
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _tmplIds = row.tmplId || ids.value;
  proxy.$modal.confirm('是否确认删除目录模板编号为"' + _tmplIds + '"的数据项？').then(function () {
    return delDirTemplate(_tmplIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {
  });
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('biz/dirTemplate/export', {
    ...queryParams.value
  }, `dirTemplate_${new Date().getTime()}.xlsx`)
}
// 新增目录节点
const append = (data)=>{
  // console.log('data',data);

  const newChild = {id: tempId.value, label: 'newMenu', children: [] }
  tempId.value = tempId.value+1
  if (!data.children) {
    data.children = []
  }
  data.children.push(newChild)
  tempData.value = [...tempData.value]
}
// 移除目录节点
const remove = (node, data)=>{
  const parent = node.parent
  const children = parent.data.children || parent.data
  const index = children.findIndex((d) => d.id === data.id)
  children.splice(index, 1)
  tempData.value = [...tempData.value]
}
// 修改目录节点
const updataNode = (data)=>{
  newName.value = ''
  newName.value = data.label
  showMenuName.value = true
  upNodeData.value = data
}
const submitMenuName = ()=>{
  if(!newName.value){
   return ElMessage({
                message: "目录名不能为空",
                type: "error"
            })
  }
const oldArr = tempData.value
  const getNewData = (data)=>{
    data.map(item=>{
      if(item.id == upNodeData.value.id){
        // console.log(1);
        item.label = newName.value
        return
      }
      if(item.children?.length>0){
        getNewData(item.children)
      }
    })
  }
  getNewData(oldArr)
  tempData.value= JSON.parse(JSON.stringify(oldArr))
  setTimeout(() => {
    showMenuName.value = false
    // console.log('tempData.value',tempData.value);
    // console.log('upNodeData.value',upNodeData.value);
  }, 200);
}
const submitAddTemp = ()=>{

}
const cancelMenuName = ()=>{
  newName.value = ''
  showMenuName.value = false
}
const cancelAddTemp = ()=>{
  open.value = false
  newName.value = ''
  tempTitle.value = ''
  tempData.value = [
  {id:1,label:tempTitle || '请输入模板名称',children:[

  ]}
]
}

getList();
</script>
<style lang="scss" scoped>
.custom-tree-node {
  width: 100%;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}
.btm{
  color: #409eff;
}
.up_btn{
  color: #E6a23c;
}
.del_btn{
  color: #F56c6c;
}
</style>
