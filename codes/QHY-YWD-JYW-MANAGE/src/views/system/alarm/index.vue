<template>
<div>
    <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="告警级别" prop="alarmLevel">
        <el-select v-model="queryParams.alarmLevel" placeholder="请选择告警级别" clearable>
          <el-option v-for="dict in event_level" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="告警类型" prop="alarmType">
        <el-select v-model="queryParams.alarmType" placeholder="请选择告警类型" clearable>
          <el-option v-for="dict in alarm_type" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="告警时间" style="width: 308px">
        <el-date-picker
          v-model="daterangeAlarmTime"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Edit" @click="handleAdd" v-hasPermi="['alarm:alarm:config']">
          告警设置
        </el-button>
      </el-col>
      <!-- <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['alarm:alarm:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['alarm:alarm:edit']"
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
          v-hasPermi="['alarm:alarm:remove']"
        >
          删除
        </el-button>
      </el-col> -->
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['alarm:alarm:export']">
          导出
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table
      v-loading="loading"
      :data="alarmList"
      @selection-change="handleSelectionChange"
      :default-sort="defaultSort"
      @sort-change="handleSortChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="告警主键" align="center" prop="alarmId" />
      <el-table-column label="告警内容" align="left" prop="alarmContent" />
      <el-table-column label="告警级别" align="center" prop="alarmLevel">
        <template #default="scope">
          <dict-tag :options="event_level" :value="scope.row.alarmLevel" />
        </template>
      </el-table-column>
      <el-table-column label="告警类型" align="center" prop="alarmType">
        <template #default="scope">
          <dict-tag :options="alarm_type" :value="scope.row.alarmType" />
        </template>
      </el-table-column>
      <el-table-column
        label="告警时间"
        align="center"
        prop="alarmTime"
        width="180"
        sortable="custom"
        :sort-orders="['descending', 'ascending']"
      >
        <template #default="scope">
          <span>{{ scope.row.alarmTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="View"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['alarm:alarm:query']"
          >
            详细
          </el-button>
          <!-- <el-button
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['alarm:alarm:remove']"
          >
            删除
          </el-button> -->
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

    <!-- 添加或修改系统告警对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="alarmRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="告警内容">
          {{ form.alarmContent }}
        </el-form-item>
        <el-form-item label="告警级别" prop="alarmLevel">
          <dict-tag :options="event_level" :value="form.alarmLevel" />
        </el-form-item>
        <el-form-item label="告警类型" prop="alarmType">
          <dict-tag :options="alarm_type" :value="form.alarmType" />
        </el-form-item>
        <el-form-item label="告警时间" prop="alarmTime">
          {{ form.alarmTime }}
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <!-- <el-button type="primary" @click="submitForm">确 定</el-button> -->
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>

  <!-- 添加或修改系统告警对话框 -->
  <el-dialog title="告警设置" v-model="config" width="500px" append-to-body>
    <el-form ref="alarmConf" :model="cf" label-width="80px">
      开启短信告警：
      <el-switch v-model="cf.status" active-value="1" inactive-value="0"></el-switch>
	  <br/>
	  <br/>
      短信接收号码：
      <el-input v-model="cf.phone" @input="phoneNumFn" style="width: 150px" />
      <br />
      <br />
      当日同一IP非法访问超过
      <el-input v-model="cf.ip_times" type="number" style="width: 50px" />
      次,则发送短信告警。
      <br />
      <br />
      硬盘使用空间超过
      <el-input v-model="cf.hdd_used" type="number" :max="100" :min="0" style="width: 50px" />
      %，发送短信告警。
      <br />
      <br />
      当日连续
      <el-input v-model="cf.cpu_times" type="number" style="width: 50px" />
      个周期,CPU超过
      <el-input v-model="cf.cpu_used" type="number" style="width: 50px" />
      %，发送短信告警。
      <br />
      <br />
      当日连续
      <el-input v-model="cf.mem_times" type="number" style="width: 50px" />
      个周期,内存超过
      <el-input v-model="cf.mem_used" type="number" style="width: 50px" />
      %，发送短信告警。
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="submitConf">确 定</el-button>
        <el-button @click="cancelConf">取 消</el-button>
      </div>
    </template>
  </el-dialog>
</div>

</template>

<script setup name="Alarm">
import { listAlarm, getAlarm, delAlarm, addAlarm, updateAlarm, confAlarm, getConf } from '@/api/system/alarm'

const { proxy } = getCurrentInstance()
const { event_level, alarm_type } = proxy.useDict('event_level', 'alarm_type')

const alarmList = ref([])
const open = ref(false)
const config = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const daterangeAlarmTime = ref([])
const defaultSort = ref({ prop: 'alarmTime', order: 'descending' })
const data = reactive({
  form: {},
  cf: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    alarmContent: null,
    alarmLevel: null,
    alarmType: null,
    alarmTime: null
  },
  rules: {}
})

const { queryParams, form, cf, rules } = toRefs(data)
/** 排序触发事件 */
function handleSortChange(column, prop, order) {
  queryParams.value.orderByColumn = column.prop
  queryParams.value.isAsc = column.order
  getList()
}
/** 查询系统告警列表 */
function getList() {
  loading.value = true
  queryParams.value.params = {}
  if (null != daterangeAlarmTime && '' != daterangeAlarmTime) {
    queryParams.value.params['beginAlarmTime'] = daterangeAlarmTime.value[0]
    queryParams.value.params['endAlarmTime'] = daterangeAlarmTime.value[1]
  }
  listAlarm(queryParams.value).then((response) => {
    alarmList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 取消按钮
function cancelConf() {
  config.value = false
}

// 表单重置
function reset() {
  form.value = {
    alarmId: null,
    alarmContent: null,
    alarmLevel: null,
    alarmType: null,
    alarmTime: null
  }
  proxy.resetForm('alarmRef')
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  daterangeAlarmTime.value = []
  proxy.resetForm('queryRef')
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.alarmId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  getConf().then((response) => {
    cf.value = response.data
    config.value = true
    title.value = '配置系统告警'
  })
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _alarmId = row.alarmId || ids.value
  getAlarm(_alarmId).then((response) => {
    form.value = response.data
    open.value = true
    title.value = '查看系统告警'
  })
}
const phoneNumFn = (data)=>{
  const rge = /^1[3|4|5|6|7|8|9][0-9]\d{8}$/
  console.log('data',data);
}
/** 提交按钮 */
function submitForm() {
  proxy.$refs['alarmRef'].validate((valid) => {
    if (valid) {
      if (form.value.alarmId != null) {
        updateAlarm(form.value).then((response) => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addAlarm(form.value).then((response) => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}
/** 提交按钮 */
function submitConf() {
  confAlarm(cf.value).then((response) => {
    proxy.$modal.msgSuccess('修改成功')
    config.value = false
    getList()
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _alarmIds = row.alarmId || ids.value
  proxy.$modal
    .confirm('是否确认删除系统告警编号为"' + _alarmIds + '"的数据项？')
    .then(function () {
      return delAlarm(_alarmIds)
    })
    .then(() => {
      getList()
      proxy.$modal.msgSuccess('删除成功')
    })
    .catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download(
    'system/alarm/export',
    {
      ...queryParams.value
    },
    `alarm_${new Date().getTime()}.xlsx`
  )
}

getList()
</script>
