<template>
	<div>
		<!-- <el-form ref="elForm" :model="formData" :rules="rules" size="medium" label-width="100px">
			<el-row>
				<el-col :span="6">
					<el-form-item label="文件ID" prop="fileId">
						<el-input v-model="formData.fileId" placeholder="请输入文件ID" :maxlength="64" show-word-limit
							clearable prefix-icon='el-icon-mobile'></el-input>
					</el-form-item>
				</el-col>
				<el-col :span="6">
					<el-form-item label="访问地址" prop="url">
						<el-input v-model="formData.url" placeholder="访问地址" prefix-icon='el-icon-mobile'></el-input>
					</el-form-item>
				</el-col>
				<el-col :span="12">
					<el-form-item size="large">
						<el-button type="primary" @click="submitForm">提交</el-button>
					</el-form-item>
				</el-col>
			</el-row>
		</el-form> -->
		<div class='qualityManual-container-office'>
			<jjt-only-office :option='option' />
		</div>
	</div>


</template>
<script>
	import jjtOnlyOffice from '@/components/OnlyOffice/index.vue'
	import useUserStore from '@/store/modules/user'
	
	export default {
		components: {
			jjtOnlyOffice
		},
		data() {
			return {
				//参考jjtOnlyOffice组件参数配置
				option: {
					key: '',
					url: '', //在线文档地址
					isEdit: '', //是否允许编辑
					fileType: '', //文件扩展名
					title: '', //文件标题
					user: {
						id: null, //用户ID
						name: '' //用户姓名
					},
					userdata: "",
					editUrl: '' //回调地址
				},
				show: true //是否显示
			}
		},
		props: [],
		computed: {},
		watch: {},
		created() {
		},
		mounted() {
			const route = useRoute();
			let row=JSON.parse(decodeURI(route.query.data))
			console.log(row,'9999999');
			console.log(111221,row.fileType.replace(".",""));
			this.option.isEdit = row.isEdit;
			this.option.url = `${window.location.origin}${import.meta.env.VITE_APP_BASE_API}/api/access/${row.fileId}`;
			this.option.title = row.fileName;
			this.option.key=row.fileId;
			this.option.fileType = row.fileType.replace(".","");
			this.option.editUrl = `${window.location.origin}${import.meta.env.VITE_APP_BASE_API}/only-office/callback/${row.docId}?name=${useUserStore().uname}`;
			this.option.user = {
				id: useUserStore().uid, //用户ID
				name: useUserStore().uname //用户姓名
			};
			console.log(2222,this.option);
			this.show = true;
		},
		methods: {
			submitForm() {
				this.$refs['elForm'].validate(valid => {
					if (!valid) return
					this.formData.url = `${window.location.origin}${import.meta.env.VITE_APP_BASE_API}/api/access/${this.formData.fileId}`;
					this.show = true;
					this.option.isEdit = true;
					this.option.url = this.formData.url;
					this.option.title = '测试word';
					this.option.key=this.formData.fileId;
					this.option.fileType = "docx";
					this.option.editUrl = `${window.location.origin}${import.meta.env.VITE_APP_BASE_API}/only-office/callback/${this.formData.fileId}`;

					this.option.user = {
						id: useUserStore().uid, //用户ID
						name: useUserStore().uname //用户姓名
					};
					console.log(this.option.user);
				})
			},
			resetForm() {
				this.$refs['elForm'].resetFields()
			},
		}
	}
</script>
<style>
	.qualityManual-container-office {
		width: 100%;
		height: '650px';
	}
</style>