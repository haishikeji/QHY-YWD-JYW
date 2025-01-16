<template>
	<div class="app-container">
		<el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
			<el-form-item label="标签名" prop="labelName">
				<el-input v-model="queryParams.labelName" placeholder="请输入标签名" clearable @keyup.enter="handleQuery" />
			</el-form-item>
			<el-form-item label="所有人" prop="owner">
				<el-input v-model="queryParams.owner" placeholder="请输入所有人" clearable @keyup.enter="handleQuery" />
			</el-form-item>
			<el-form-item label="显示顺序" prop="orderNum">
				<el-input v-model="queryParams.orderNum" placeholder="请输入显示顺序" clearable @keyup.enter="handleQuery" />
			</el-form-item>
			<el-form-item label="创建人" prop="createBy">
				<el-input v-model="queryParams.createBy" placeholder="请输入创建人" clearable @keyup.enter="handleQuery" />
			</el-form-item>
			<el-form-item label="创建时间" prop="createTime">
				<el-date-picker clearable v-model="queryParams.createTime" type="date" value-format="YYYY-MM-DD"
					placeholder="请选择创建时间">
				</el-date-picker>
			</el-form-item>
			<el-form-item label="更新人" prop="updateBy">
				<el-input v-model="queryParams.updateBy" placeholder="请输入更新人" clearable @keyup.enter="handleQuery" />
			</el-form-item>
			<el-form-item label="更新时间" prop="updateTime">
				<el-date-picker clearable v-model="queryParams.updateTime" type="date" value-format="YYYY-MM-DD"
					placeholder="请选择更新时间">
				</el-date-picker>
			</el-form-item>
			<el-form-item label="备注" prop="remark">
				<el-input v-model="queryParams.remark" placeholder="请输入备注" clearable @keyup.enter="handleQuery" />
			</el-form-item>
			<el-form-item>
				<el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
				<el-button icon="Refresh" @click="resetQuery">重置</el-button>
			</el-form-item>
		</el-form>

		<el-row :gutter="10" class="mb8">
			<el-col :span="1.5">
				<el-button type="primary" plain icon="Plus" @click="handleAdd"
					v-hasPermi="['biz:label:add']">新增</el-button>
			</el-col>
			<el-col :span="1.5">
				<el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate"
					v-hasPermi="['biz:label:edit']">修改</el-button>
			</el-col>
			<el-col :span="1.5">
				<el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete"
					v-hasPermi="['biz:label:remove']">删除</el-button>
			</el-col>
			<el-col :span="1.5">
				<el-button type="warning" plain icon="Download" @click="handleExport"
					v-hasPermi="['biz:label:export']">导出</el-button>
			</el-col>
			<right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
		</el-row>

		<el-table v-loading="loading" :data="labelList" @selection-change="handleSelectionChange">
			<el-table-column type="selection" width="55" align="center" />
			<el-table-column label="收藏标签ID" align="center" prop="labelId" />
			<el-table-column label="标签名" align="center" prop="labelName" />
			<el-table-column label="所有人" align="center" prop="owner" />
			<el-table-column label="显示顺序" align="center" prop="orderNum" />
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
			<el-table-column label="备注" align="center" prop="remark" />
			<el-table-column label="操作" align="center" class-name="small-padding fixed-width">
				<template #default="scope">
					<el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"
						v-hasPermi="['biz:label:edit']">修改</el-button>
					<el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
						v-hasPermi="['biz:label:remove']">删除</el-button>
				</template>
			</el-table-column>
		</el-table>

		<pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum"
			v-model:limit="queryParams.pageSize" @pagination="getList" />

		<!-- 添加或修改文件收藏标签对话框 -->
		<el-dialog :title="title" v-model="open" width="500px" append-to-body>
			<el-form ref="labelRef" :model="form" :rules="rules" label-width="80px">
				<el-form-item label="标签名" prop="labelName">
					<el-input v-model="form.labelName" placeholder="请输入标签名" />
				</el-form-item>
				<el-form-item label="所有人" prop="owner">
					<el-input v-model="form.owner" placeholder="请输入所有人" />
				</el-form-item>
				<el-form-item label="显示顺序" prop="orderNum">
					<el-input v-model="form.orderNum" placeholder="请输入显示顺序" />
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

<script setup name="Label">
	import {
		listLabel,
		getLabel,
		delLabel,
		addLabel,
		updateLabel
	} from "@/api/biz/label";

	const {
		proxy
	} = getCurrentInstance();

	const labelList = ref([]);
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
			labelName: null,
			owner: null,
			orderNum: null,
			createBy: null,
			createTime: null,
			updateBy: null,
			updateTime: null,
			remark: null
		},
		rules: {}
	});

	const {
		queryParams,
		form,
		rules
	} = toRefs(data);

	/** 查询文件收藏标签列表 */
	function getList() {
		loading.value = true;
		listLabel(queryParams.value).then(response => {
			labelList.value = response.rows;
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
			labelId: null,
			labelName: null,
			owner: null,
			orderNum: null,
			createBy: null,
			createTime: null,
			updateBy: null,
			updateTime: null,
			remark: null
		};
		proxy.resetForm("labelRef");
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
		ids.value = selection.map(item => item.labelId);
		single.value = selection.length != 1;
		multiple.value = !selection.length;
	}

	/** 新增按钮操作 */
	function handleAdd() {
		reset();
		open.value = true;
		title.value = "添加文件收藏标签";
	}

	/** 修改按钮操作 */
	function handleUpdate(row) {
		reset();
		const _labelId = row.labelId || ids.value
		getLabel(_labelId).then(response => {
			form.value = response.data;
			open.value = true;
			title.value = "修改文件收藏标签";
		});
	}

	/** 提交按钮 */
	function submitForm() {
		proxy.$refs["labelRef"].validate(valid => {
			if (valid) {
				if (form.value.labelId != null) {
					updateLabel(form.value).then(response => {
						proxy.$modal.msgSuccess("修改成功");
						open.value = false;
						getList();
					});
				} else {
					addLabel(form.value).then(response => {
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
		const _labelIds = row.labelId || ids.value;
		proxy.$modal.confirm('是否确认删除文件收藏标签编号为"' + _labelIds + '"的数据项？').then(function() {
			return delLabel(_labelIds);
		}).then(() => {
			getList();
			proxy.$modal.msgSuccess("删除成功");
		}).catch(() => {});
	}

	/** 导出按钮操作 */
	function handleExport() {
		proxy.download('biz/label/export', {
			...queryParams.value
		}, `label_${new Date().getTime()}.xlsx`)
	}

	getList();
</script>