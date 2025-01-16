<template>
	<div class="app-container">
		<div class="text_box">
			<span>功能说明：</span>
			<span>预览：在线预览</span>
			<span>全文搜索：是否允许对文件内容解析，并加入高级搜索</span>
			<span>允许转存：分享文件时，是否允许接收者转存和下载文件</span>
			<span>内容加密：对文档本身进行私有算法加密混淆，仅本软件能解密打开</span>
			<span>安全访问二次认证：访问文档时需要再次输入登录密码</span>
			<span>私有安全访问码：访问文档时需要输入自定义的访问密码，创建文档时定义</span>
			<span>内容防复制：预览状态下不允许复制文档内容到剪贴板</span>
			<span>防拍照：预留功能，实现难度大</span>
		</div>
		<el-table v-loading="loading" :data="levelList" @selection-change="handleSelectionChange">
			<el-table-column label="名称" align="center">
				<template #default="scope">
					<el-input v-model="scope.row.levelName"></el-input>
				</template>
			</el-table-column>
			<el-table-column label="编码" align="center" prop="levelCode" />
			<el-table-column label="预览" align="center" width="100">
				<template #default="scope">
					<el-checkbox true-label="1" false-label="0" v-model="scope.row.roles.l0100"
						:checked="scope.row.roles.l0100"></el-checkbox>
				</template>
			</el-table-column>
			<el-table-column label="下载" align="center" width="100">
				<template #default="scope">
					<el-checkbox true-label="1" false-label="0" v-model="scope.row.roles.l0200"
						:checked="scope.row.roles.l0200"></el-checkbox>
				</template>
			</el-table-column>
			<el-table-column label="全文搜索" align="center" width="100">
				<template #default="scope">
					<el-checkbox true-label="1" false-label="0" v-model="scope.row.roles.l0300"
						:checked="scope.row.roles.l0300"></el-checkbox>
				</template>
			</el-table-column>
			<el-table-column label="允许分享" align="center" width="100">
				<template #default="scope">
					<el-checkbox true-label="1" false-label="0" v-model="scope.row.roles.l0350"
						:checked="scope.row.roles.l0350"></el-checkbox>
				</template>
			</el-table-column>
			<el-table-column label="允许转存" align="center" width="100">
				<template #default="scope">
					<el-checkbox true-label="1" false-label="0" v-model="scope.row.roles.l0400"
						:checked="scope.row.roles.l0400"></el-checkbox>
				</template>
			</el-table-column>
			<el-table-column label="内容加密" align="center" width="100">
				<template #default="scope">
					<el-checkbox true-label="1" false-label="0" v-model="scope.row.roles.l0500"
						:checked="scope.row.roles.l0500"></el-checkbox>
				</template>
			</el-table-column>
			<el-table-column label="二次认证" align="center" width="100">
				<template #default="scope">
					<el-checkbox true-label="1" false-label="0" v-model="scope.row.roles.l0600"
						:checked="scope.row.roles.l0600"></el-checkbox>
				</template>
			</el-table-column>
			<el-table-column label="私有安全访问码" align="center" width="100">
				<template #default="scope">
					<el-checkbox true-label="1" false-label="0" v-model="scope.row.roles.l0700"
						:checked="scope.row.roles.l0700"></el-checkbox>
				</template>
			</el-table-column>
			<el-table-column label="内容防复制" align="center" width="100">
				<template #default="scope">
					<el-checkbox true-label="1" false-label="0" v-model="scope.row.roles.l0800"
						:checked="scope.row.roles.l0800"></el-checkbox>
				</template>
			</el-table-column>
			<el-table-column label="防拍照" align="center" width="100">
				<template #default="scope">
					<el-checkbox true-label="1" false-label="0" v-model="scope.row.roles.l0900"
						:checked="scope.row.roles.l0900"></el-checkbox>
				</template>
			</el-table-column>
			<el-table-column label="删除" align="center" width="100">
				<template #default="scope">
					<el-checkbox true-label="1" false-label="0" v-model="scope.row.roles.l1000"
						:checked="scope.row.roles.l1000"></el-checkbox>
				</template>
			</el-table-column>
			<el-table-column label="创建" align="center" width="100">
				<template #default="scope">
					<el-checkbox true-label="1" false-label="0" v-model="scope.row.roles.l1100"
						:checked="scope.row.roles.l1100"></el-checkbox>
				</template>
			</el-table-column>
			<el-table-column label="移动/复制" align="center" width="100">
				<template #default="scope">
					<el-checkbox true-label="1" false-label="0" v-model="scope.row.roles.l1200"
						:checked="scope.row.roles.l1200"></el-checkbox>
				</template>
			</el-table-column>
			<el-table-column label="打印" align="center" width="100">
				<template #default="scope">
					<el-checkbox true-label="1" false-label="0" v-model="scope.row.roles.l1300"
						:checked="scope.row.roles.l1300"></el-checkbox>
				</template>
			</el-table-column>
			<!-- <el-table-column label="等级对应权限" align="center" prop="levelRoles">
				<template #default="scope">
					<dict-tag :options="doc_roles" :value="scope.row.roles ? scope.row.roles : []" />
				</template>
			</el-table-column>
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
						v-hasPermi="['biz:level:edit']">修改</el-button>
					<el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
						v-hasPermi="['biz:level:remove']">删除</el-button>
				</template>
			</el-table-column>
			 -->
		</el-table>
		<el-form label-width="100px">
			<div style="text-align: center;margin-left:-100px;margin-top:10px;">
				<el-button type="primary" @click="submitForm()">保存</el-button>
			</div>
		</el-form>
		<!-- <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum"
			v-model:limit="queryParams.pageSize" @pagination="getList" /> -->
	</div>
</template>

<script setup name="Level">
	import {
		getLevel,
		listLevel,
		updateLevel
	} from "@/api/biz/level";
	const {
		proxy
	} = getCurrentInstance();
	const {
		doc_roles
	} = proxy.useDict('doc_roles');

	const levelList = ref([]);
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
			levelName: null,
			levelCode: null,
			levelRoles: null,
			createBy: null,
			createTime: null,
			updateBy: null,
			updateTime: null,
			remark: null
		},
		rules: {
			levelCode: [{
				required: true,
				message: "等级编码不能为空",
				trigger: "blur"
			}],
			levelRoles: [{
				required: true,
				message: "等级对应权限不能为空",
				trigger: "blur"
			}],
		}
	});

	const {
		queryParams,
		form,
		rules
	} = toRefs(data);

	/** 查询文档目录加密级别列表 */
	function getList() {
		loading.value = true;
		listLevel(queryParams.value).then(response => {
			levelList.value = response.rows;
			console.log(response.rows[0].roles.l0100)
			console.log(levelList);
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
			levelId: null,
			levelName: null,
			levelCode: null,
			roles: [],
			createBy: null,
			createTime: null,
			updateBy: null,
			updateTime: null,
			remark: null
		};
		proxy.resetForm("levelRef");
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
		ids.value = selection.map(item => item.levelId);
		single.value = selection.length != 1;
		multiple.value = !selection.length;
	}

	/** 新增按钮操作 */
	function handleAdd() {
		reset();
		open.value = true;
		title.value = "添加文档目录加密级别";
	}

	/** 修改按钮操作 */
	function handleUpdate(row) {
		reset();
		const _levelId = row.levelId || ids.value
		getLevel(_levelId).then(response => {
			form.value = response.data;
			// form.value.roles = form.value.roles.split(",");
			open.value = true;
			title.value = "修改文档目录加密级别";
		});
	}

	/** 提交按钮 */
	function submitForm() {
		updateLevel(levelList.value).then(response => {
			proxy.$modal.msgSuccess("修改成功");
		});
		console.log(levelList.value);
		// proxy.$refs["levelRef"].validate(valid => {
		// 	if (valid) {
		// 		form.value.roles = form.value.roles.join(",");
		// 		if (form.value.levelId != null) {
		// 			updateLevel(form.value).then(response => {
		// 				proxy.$modal.msgSuccess("修改成功");
		// 				open.value = false;
		// 				getList();
		// 			});
		// 		} else {
		// 			addLevel(form.value).then(response => {
		// 				proxy.$modal.msgSuccess("新增成功");
		// 				open.value = false;
		// 				getList();
		// 			});
		// 		}
		// 	}
		// });
	}

	/** 删除按钮操作 */
	function handleDelete(row) {
		const _levelIds = row.levelId || ids.value;
		proxy.$modal.confirm('是否确认删除文档目录加密级别编号为"' + _levelIds + '"的数据项？').then(function() {
			return delLevel(_levelIds);
		}).then(() => {
			getList();
			proxy.$modal.msgSuccess("删除成功");
		}).catch(() => {});
	}

	/** 导出按钮操作 */
	function handleExport() {
		proxy.download('biz/level/export', {
			...queryParams.value
		}, `level_${new Date().getTime()}.xlsx`)
	}

	getList();
</script>
<style lang="scss" scoped>
.text_box{
	width: 100%;
	height: 200px;
	margin: 10px auto;
	padding: 10px 0;
	// border: 1px solid #000;
	display: flex;
	flex-direction: column;
}
</style>