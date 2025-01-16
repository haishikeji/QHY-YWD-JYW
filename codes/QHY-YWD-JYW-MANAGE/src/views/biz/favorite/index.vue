<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="是否文件夹" prop="isFolder">
        <el-select v-model="queryParams.isFolder" placeholder="请选择是否文件夹" clearable>
          <el-option
            v-for="dict in sys_yes_no"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
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
          v-hasPermi="['biz:favorite:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['biz:favorite:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['biz:favorite:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['biz:favorite:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="favoriteList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="收藏ID" align="center" prop="favoriteId" />
      <el-table-column label="是否文件夹" align="center" prop="isFolder">
        <template #default="scope">
          <dict-tag :options="sys_yes_no" :value="scope.row.isFolder"/>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:favorite:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:favorite:remove']">删除</el-button>
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

    <!-- 添加或修改文件收藏对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="favoriteRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标签ID" prop="labelId">
          <el-input v-model="form.labelId" placeholder="请输入标签ID" />
        </el-form-item>
        <el-form-item label="是否文件夹" prop="isFolder">
          <el-select v-model="form.isFolder" placeholder="请选择是否文件夹">
            <el-option
              v-for="dict in sys_yes_no"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="文件夹/文档ID" prop="relaId">
          <el-input v-model="form.relaId" placeholder="请输入文件夹/文档ID" />
        </el-form-item>
        <el-form-item label="所有人" prop="owner">
          <el-input v-model="form.owner" placeholder="请输入所有人" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Favorite">
import { listFavorite, getFavorite, delFavorite, addFavorite, updateFavorite } from "@/api/biz/favorite";

const { proxy } = getCurrentInstance();
const { sys_yes_no } = proxy.useDict('sys_yes_no');

const favoriteList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    isFolder: null,
  },
  rules: {
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询文件收藏列表 */
function getList() {
  loading.value = true;
  listFavorite(queryParams.value).then(response => {
    favoriteList.value = response.rows;
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
    favoriteId: null,
    labelId: null,
    isFolder: null,
    relaId: null,
    owner: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  };
  proxy.resetForm("favoriteRef");
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
  ids.value = selection.map(item => item.favoriteId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加文件收藏";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _favoriteId = row.favoriteId || ids.value
  getFavorite(_favoriteId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改文件收藏";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["favoriteRef"].validate(valid => {
    if (valid) {
      if (form.value.favoriteId != null) {
        updateFavorite(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addFavorite(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _favoriteIds = row.favoriteId || ids.value;
  proxy.$modal.confirm('是否确认删除文件收藏编号为"' + _favoriteIds + '"的数据项？').then(function() {
    return delFavorite(_favoriteIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('biz/favorite/export', {
    ...queryParams.value
  }, `favorite_${new Date().getTime()}.xlsx`)
}

getList();
</script>
