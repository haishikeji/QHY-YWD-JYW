<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="扫描仪名称" prop="scannerName">
        <el-input
          v-model="queryParams.scannerName"
          placeholder="请输入扫描仪名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="扫描仪IP" prop="scannerIp">
        <el-input v-model="queryParams.scannerIp" placeholder="请输入扫描仪IP" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="是否公用" prop="isPublic">
        <el-select v-model="queryParams.isPublic" placeholder="请选择是否公用" clearable>
          <el-option v-for="dict in sys_yes_no" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="所属部门" prop="deptId">
		  <el-tree-select v-model="queryParams.deptId" :data="deptOptions"
		  								:props="{ value: 'id', label: 'label', children: 'children' }" value-key="id"
		  								placeholder="请选择归属部门" check-strictly @keyup.enter="handleQuery"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['scanner:info:add']">
          新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['scanner:info:edit']"
        >
          修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['scanner:info:remove']"
        >
          删除
        </el-button>
      </el-col>
      <!-- <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['scanner:info:export']">
          导出
        </el-button>
      </el-col> -->
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="View"
          :disabled="single"
          @click="handleSee"
        >
          查看FTP信息
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="infoList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="扫描仪编号" align="center" prop="scannerId" />
      <el-table-column label="扫描仪名称" align="center" prop="scannerName" />
      <el-table-column label="扫描仪IP" align="center" prop="scannerIp" />
      <el-table-column label="是否公用" align="center" prop="isPublic">
        <template #default="scope">
          <dict-tag :options="sys_yes_no" :value="scope.row.isPublic" />
        </template>
      </el-table-column>
      <el-table-column label="FTP用户ID" align="center" prop="userId" />
      <el-table-column label="所属部门" align="center" prop="deptName" />
      <el-table-column label="创建人" align="center" prop="createBy" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新人" align="center" prop="updateBy" />
      <el-table-column label="更新时间" align="center" prop="updateTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.updateTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['scanner:info:edit']"
          >
            修改
          </el-button>
          <el-button
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['scanner:info:remove']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改扫描仪管理对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="infoRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="扫描仪名称" prop="scannerName">
          <el-input v-model="form.scannerName" placeholder="请输入扫描仪名称;" />
        </el-form-item>
        <el-form-item label="扫描仪IP" prop="scannerIp">
          <el-input v-model="form.scannerIp" placeholder="请输入扫描仪IP" />
        </el-form-item>
        <el-form-item label="是否公用" prop="isPublic">
          <el-radio-group v-model="form.isPublic">
            <el-radio v-for="dict in sys_yes_no" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <!-- <el-form-item label="FTP用户ID" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入FTP用户ID" />
        </el-form-item> -->
        <el-form-item label="所属部门" v-if="form.isPublic === 'N'" prop="deptId">
          <!-- <el-input v-model="form.deptId" placeholder="请输入所属部门" /> -->
          <el-tree-select v-model="form.deptId" :data="deptOptions"
								:props="{ value: 'id', label: 'label', children: 'children' }" value-key="id"
								placeholder="请选择归属部门" check-strictly />
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

<script setup name="Info">
import { addInfo, updateInfo, delInfo, getInfo, listInfo } from '@/api/scanner/info'
import { getInfo as getUserInfoApi} from "@/api/login";
import { encode, decode } from '@/utils/sm2util'
import {
  deptTreeSelect,
} from "@/api/system/user";
import { ElMessageBox } from 'element-plus'
import { onMounted } from 'vue';
const { proxy } = getCurrentInstance()
const { sys_yes_no } = proxy.useDict('sys_yes_no')

const infoList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const deptOptions = ref(undefined);
const userDeptId = ref()
//ip验证
const scannerIpRule = (rule, value, callback) => {
  var pPattern = new RegExp(
    /(\d{1,3}\.){3}\d{1,3}/
  );
  if (value === "") {
    callback(new Error("请输入扫描仪IP"));
  } else if (!pPattern.test(value)) {
    callback(new Error("请输入正确的IP地址"));
  } else {
    callback();
  }
};
const data = reactive({
  form: {
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    scannerName: null,
    scannerIp: null,
    isPublic: null,
    userId: null,
    deptId: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null
  },
  rules: {
    scannerIp: [{ required: true, validator: scannerIpRule, trigger: "change" }],
  }
})

const { queryParams, form, rules } = toRefs(data)


/** 查询扫描仪管理列表 */
function getList() {
  loading.value = true
  listInfo(queryParams.value).then((response) => {
    infoList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 表单重置
function reset() {
  form.value = {
    scannerId: null,
    scannerName: null,
    scannerIp: null,
    isPublic: "N",
    userId: null,
    deptId: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null
  }
  form.value.deptId = userDeptId.value
  proxy.resetForm('infoRef')
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.scannerId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = '添加扫描仪管理'
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _scannerId = row.scannerId || ids.value
  getInfo(_scannerId).then((response) => {
    form.value = response.data
    open.value = true
    title.value = '修改扫描仪管理'
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs['infoRef'].validate((valid) => {
    if (valid) {
      if(form.value.isPublic === "Y"){
        form.value.deptId = null
      }
      if (form.value.scannerId != null) {
        updateInfo(form.value).then((response) => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        // const query = JSON.parse(JSON.stringify(form.value))
        //  console.log('form.value',query);
            addInfo(form.value).then((response) => {
              const data = response.data
              const pass=decode(data.pass);
              // const msg1=`新增扫描仪成功，请在扫描仪中配置ftp信息! \u000a 端口号：${data.port} \r 账号：${data.user} \u000a 密码：${pass}`
              // proxy.$modal.alertSuccess("新增扫描仪成功，请在扫描仪中配置ftp信息!"+`(端口号：${data.port}`)
               ElMessageBox.alert(
                `新增扫描仪成功，请在扫描仪中配置ftp信息!<br>端口号：${data.port}<br>账号：${data.user}<br>密码：${pass}`,
                '系统提示',
                {
                  dangerouslyUseHTMLString: true,
                }
              )
              open.value = false
              getList()
            })
      }
    }else{
      proxy.$modal.msgError('请输入正确的信息')
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _scannerIds = row.scannerId || ids.value
  proxy.$modal
    .confirm('是否确认删除扫描仪管理编号为"' + _scannerIds + '"的数据项？')
    .then(function () {
      return delInfo(_scannerIds)
    })
    .then(() => {
      getList()
      proxy.$modal.msgSuccess('删除成功')
    })
    .catch(() => {})
}
	/** 查询部门下拉树结构 */
	function getDeptTree() {
		deptTreeSelect().then(response => {
			deptOptions.value = response.data;
		});
	};
/** 导出按钮操作 */
function handleExport() {
  proxy.download(
    'scanner/info/export',
    {
      ...queryParams.value
    },
    `info_${new Date().getTime()}.xlsx`
  )
}
/** 查看按钮操作 */
function handleSee() {
  const url = location.href
  const arr = url.split('/')
console.log('arr',arr);
  const row = infoList.value.find(item=>item.scannerId === ids.value[0])
  const pass=decode(row.pass);
   ElMessageBox.alert(
      `端口号：${row.port}<br>FTPIP：${arr[2]}<br>账号：${row.user}<br>密码：${pass}`,
      '扫描仪信息',
      {
        dangerouslyUseHTMLString: true,
      }
    )     
  console.log('row',row);
}
const getUserInfo =async ()=>{
  const infoRes = await getUserInfoApi();
  userDeptId.value = infoRes.user.deptId
  console.log('infoRes',infoRes);
}
onMounted(()=>{

  getUserInfo()
})
getDeptTree();
getList()
</script>
