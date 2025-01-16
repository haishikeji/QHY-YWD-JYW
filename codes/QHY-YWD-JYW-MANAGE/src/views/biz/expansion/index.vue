<template>
	<div class="app-container">
		<el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
			<el-form-item label="空间名称" prop="spaceName">
				<el-input v-model="queryParams.spaceName" placeholder="请输入空间名称" clearable @keyup.enter="handleQuery" />
			</el-form-item>
			<el-form-item label="扩容状态" prop="expandStatus">
				<el-select v-model="queryParams.expandStatus" placeholder="请选择扩容状态" clearable>
					<el-option v-for="dict in expansion_status" :key="dict.value" :label="dict.label"
						:value="dict.value" />
				</el-select>
			</el-form-item>
			<el-form-item label="申请人" prop="createBy">
				<el-input v-model="queryParams.createBy" placeholder="请输入申请人" clearable @keyup.enter="handleQuery" />
			</el-form-item>
			<el-form-item>
				<el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
				<el-button icon="Refresh" @click="resetQuery">重置</el-button>
			</el-form-item>
		</el-form>


		<el-table v-loading="loading" :data="expansionList" @selection-change="handleSelectionChange">
			<el-table-column type="selection" width="55" align="center" />
			<!-- <el-table-column label="自增ID" align="center" prop="autoId" /> -->
			<!-- <el-table-column label="空间ID" align="center" prop="spaceId" /> -->
			<el-table-column label="空间名称" align="center" prop="spaceName" />
			<el-table-column label="当前容量" align="center" prop="currentCap" />
			<el-table-column label="申请扩充容量" align="center" prop="expandCap" />
			<el-table-column label="扩容原因" align="center" prop="expandReason" />
			<el-table-column label="扩容状态" align="center" prop="expandStatus">
				<template #default="scope">
					<dict-tag :options="expansion_status" :value="scope.row.expandStatus" />
				</template>
			</el-table-column>
			<el-table-column label="审核说明" align="center" prop="description" />
			<el-table-column label="申请人" align="center" prop="createBy" />
			<el-table-column label="操作" align="center" class-name="small-padding fixed-width">
				<template #default="scope">
					<el-button type="success" icon="Select" @click="handAgree(scope.row)"
						v-if="scope.row.expandStatus=='1'">通过</el-button>
					<el-button type="info" icon="CloseBold" @click="handRefuse(scope.row)"
						v-if="scope.row.expandStatus=='1'">拒绝</el-button>
				</template>
			</el-table-column>
		</el-table>

		<pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum"
			v-model:limit="queryParams.pageSize" @pagination="getList" />

		<!-- 添加或修改空间扩容管理对话框 -->
		<el-dialog :title="title" v-model="open" width="500px" append-to-body>
			<el-form ref="expansionRef" :model="form" :rules="rules" label-width="80px">
				<el-form-item label="空间ID" prop="spaceId">
					<el-input v-model="form.spaceId" placeholder="请输入空间ID" />
				</el-form-item>
				<el-form-item label="空间名称" prop="spaceName">
					<el-input v-model="form.spaceName" placeholder="请输入空间名称" />
				</el-form-item>
				<el-form-item label="当前容量" prop="currentCap">
					<el-input v-model="form.currentCap" placeholder="请输入当前容量" />
				</el-form-item>
				<el-form-item label="申请扩充容量" prop="expandCap">
					<el-input v-model="form.expandCap" placeholder="请输入申请扩充容量" />
				</el-form-item>
				<el-form-item label="扩容原因" prop="expandReason">
					<el-input v-model="form.expandReason" type="textarea" placeholder="请输入内容" />
				</el-form-item>
				<el-form-item label="扩容状态" prop="expandStatus">
					<el-radio-group v-model="form.expandStatus">
						<el-radio v-for="dict in expansion_status" :key="dict.value"
							:label="dict.value">{{dict.label}}</el-radio>
					</el-radio-group>
				</el-form-item>
				<el-form-item label="审核说明" prop="description">
					<el-input v-model="form.description" type="textarea" placeholder="请输入内容" />
				</el-form-item>
				<el-form-item label="申请人ID" prop="created">
					<el-input v-model="form.created" placeholder="请输入申请人ID" />
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

<script setup name="Expansion">
	import {
		getExpansion,
		listExpansion,
		refuse,
		agree
	} from "@/api/biz/expansion";

	const {
		proxy
	} = getCurrentInstance();
	const {
		expansion_status
	} = proxy.useDict('expansion_status');

	const expansionList = ref([]);
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
			spaceId: null,
			spaceName: null,
			currentCap: null,
			expandCap: null,
			expandReason: null,
			expandStatus: '1',
			description: null,
			created: null,
			createBy: null,
		},
		rules: {}
	});

	const {
		queryParams,
		form,
		rules
	} = toRefs(data);

	/** 查询空间扩容管理列表 */
	function getList() {
		loading.value = true;
		listExpansion(queryParams.value).then(response => {
			expansionList.value = response.rows;
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
			autoId: null,
			spaceId: null,
			spaceName: null,
			currentCap: null,
			expandCap: null,
			expandReason: null,
			expandStatus: null,
			description: null,
			created: null,
			createBy: null,
			createTime: null,
			updateBy: null,
			updateTime: null,
			remark: null
		};
		proxy.resetForm("expansionRef");
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
		ids.value = selection.map(item => item.autoId);
		single.value = selection.length != 1;
		multiple.value = !selection.length;
	}

	/** 新增按钮操作 */
	function handleAdd() {
		reset();
		open.value = true;
		title.value = "添加空间扩容管理";
	}

	/** 修改按钮操作 */
	function handleUpdate(row) {
		reset();
		const _autoId = row.autoId || ids.value
		getExpansion(_autoId).then(response => {
			form.value = response.data;
			open.value = true;
			title.value = "修改空间扩容管理";
		});
	}

	/** 提交按钮 */
	function submitForm() {
		proxy.$refs["expansionRef"].validate(valid => {
			if (valid) {
				if (form.value.autoId != null) {
					updateExpansion(form.value).then(response => {
						proxy.$modal.msgSuccess("修改成功");
						open.value = false;
						getList();
					});
				} else {
					addExpansion(form.value).then(response => {
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
		const _autoIds = row.autoId || ids.value;
		proxy.$modal.confirm('是否确认删除空间扩容管理编号为"' + _autoIds + '"的数据项？').then(function() {
			return delExpansion(_autoIds);
		}).then(() => {
			getList();
			proxy.$modal.msgSuccess("删除成功");
		}).catch(() => {});
	}

	/** 通过按钮操作 */
	function handAgree(row) {
		proxy.$modal.confirm('是否确认通过"' + row.spaceName + '"的扩容申请？').then(function() {
			return agree(row.autoId);
		}).then(() => {
			getList();
			proxy.$modal.msgSuccess("操作成功！");
		}).catch(() => {});
	}
	/** 拒绝按钮操作 */
	function handRefuse(row) {
		proxy.$prompt('请输入拒绝"' + row.spaceName + '"的扩容申请理由', "拒绝申请", {
			confirmButtonText: "确定",
			cancelButtonText: "取消",
			closeOnClickModal: false,
			inputPattern: /^.{1,400}$/,
			inputErrorMessage: " 拒绝理由不能为空且小于400字",
		}).then(({
			value
		}) => {
			refuse(row.autoId, value).then(response => {
				getList();
				proxy.$modal.msgSuccess("操作成功！");
			});
		}).catch(() => {});
	}

	/** 导出按钮操作 */
	function handleExport() {
		proxy.download('biz/expansion/export', {
			...queryParams.value
		}, `expansion_${new Date().getTime()}.xlsx`)
	}

	getList();
</script>