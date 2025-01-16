<template>
  <el-dialog v-model="addFile" title="新建文件" width="30%" @close="backTo">
    <el-form
      :model="createFile"
      :label-position="labelPosition"
      label-width="80px"
      ref="labelRef"
      :rules="rules"
    >
      <!-- <el-form-item label="文件目录">
                <el-tree :data="treeData" :props="defaultProps" @node-click="handleNodeClick" />
            </el-form-item> -->
      <el-form-item label="目录" class="title-name">
        <!-- <p>{{ nowFolder }}</p> -->
        <span>{{ clickFileData.dirName }}</span>
      </el-form-item>
      <el-form-item label="文件名">
          <el-input v-model="createFile.name" maxlength="32" placeholder="请输入不多于32位字符" type="textarea" />
      </el-form-item>
      <el-form-item
        label="文件类型"
        prop="fileType"
      >
        <el-select
          v-model="createFile.fileType"
          clearable
          placeholder="请选择文件类型"
          @change="optChange"
        >
          <el-option
            v-for="item in typeF"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="back">取消</el-button>
        <el-button type="primary" @click="sureAddFile"> 确定 </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script>
import { ref, onMounted, toRef } from "vue";
import fileCount from "../../../api/fileCount/fileCount";
import documents from "../../../api/document/document";
import { ElMessage } from "element-plus";
export default {
  props: {
    newAdd: {
      type: Boolean,
      required: true,
    },
    clickFileData: {
      type: Object,
      required: true,
    },
  },
  setup(props, { emit }) {
    const rules = {
        fileType:{ required: true, message: '请选择文件类型', trigger: 'change' }
    }
    const { proxy } = getCurrentInstance();
    const labelPosition = ref("right");
    let { sureAddFile, back } = toRef(props);
    let result = props.newAdd;
    let createFile = ref({
      name: "",
      fileType: "",
    });
    let treeData = ref([]);
    let defaultProps = ref({
      label: "label",
      value: "id",
      disable: false,
    });
    let typeF = ref([
      {
        label: "word",
        value: "word",
      },
      {
        label: "excel",
        value: "excel",
      },
      {
        label: "ppt",
        value: "ppt",
      },
      {
        label: "txt",
        value: "txt",
      },
    ]);
    let noChangeSpaceId = ref(0);
    let dirIds = ref(0);
    let addFile = ref(false);
    let nowFolder = ref("");
    function inlineList() {
      addFile.value = result;
    }
    //获取文件树
    function getFileTree() {
      documents.fileTree(3).then((res) => {
        treeData.value = [res];
      });
      documents.getTop(3).then((res) => {
        noChangeSpaceId.value = res.spaceId;
      });
    }
    function handleNodeClick(data) {
      console.log(data, "data");
      dirIds.value = data.id;
      nowFolder.value = data.label;
    }
    function backTo() {
      addFile.value = false;
      emit("closeAddFile");
    }
    function everyDunk() {
      // fileCount.createFiles({
      //     dirId: dirIds.value - 0,
      //     spaceId: noChangeSpaceId.value - 0,
      //     type: createFile.value.fileType + ""
      // }).then(res => {
      //     console.log(res, 'rrr');
      //     addFile.value = false
      //     ElMessage({ message: "新增成功", type: "success" })
      //     emit('threeBe', addFile.value)
      // })
      proxy.$refs.labelRef.validate(valid => {
        if (valid) {
        fileCount
        .createFiles({
          dirId: props.clickFileData.dirId,
          spaceId: props.clickFileData.spaceId,
          type: createFile.value.fileType,
		  name: createFile.value.name
        })
        .then((res) => {
          console.log('addRes',res);
          addFile.value = false;
          ElMessage({ message: "新增成功", type: "success" });
          emit("threeBe", addFile.value,res.data);
        });
      }
    })
    }
    function optChange(e) {
      createFile.fileType = e;
    }
    onMounted(() => {
      inlineList();
      getFileTree();
    });
    return {
      createFile,
      getFileTree,
      treeData,
      defaultProps,
      handleNodeClick,
      typeF,
      addFile,
      backTo,
      noChangeSpaceId,
      dirIds,
      back: backTo,
      sureAddFile: everyDunk,
      nowFolder,
      optChange,
      labelPosition,
      rules
    };
  },
};
</script>

<style lang="scss" scoped>
.el-form-item__label {
  color: #505870;
}
:deep(.el-select) {
  width: 100% !important;
}
.el-dialog__title {
  color: #000;
}
</style>