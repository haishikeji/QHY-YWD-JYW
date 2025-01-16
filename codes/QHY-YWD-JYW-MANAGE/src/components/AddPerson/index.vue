<template>
  <el-dialog
    v-model="isOpen"
    append-to-body
    width="342"
  >
  <template #header>
      <div class="my-header">
        <div class="title">新建聊天</div>
      </div>
    </template>
  <div class="main-cont">
    <div class="main-cont-left">
        <div class="main-cont-item">  <el-input
          v-model="searchText"
          class="w-50 m-2"
          size="default"
          placeholder="搜索人员"
          suffix-icon="Search"
          @change="SearchPerson"
        /></div>
        <el-tree-v2 ref="treeRef" :data="treeData.data" :show-checkbox="false" :height="283" 
        :filter-method="filterMethod"
        :props="defaultProps"  @node-click="handleNodeClick" />
    </div>
  </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="isOpen = false" class="cancel-button">取消</el-button>
        <el-button type="primary" @click="confirm" class="sure-button">
          确认
        </el-button>
      </span>
    </template>
  </el-dialog>

</template>
<script setup>
import { reactive,ref,toRaw,toRefs,watch} from "vue";
import {  ElMessage} from 'element-plus'
import useUserStore from "@/store/modules/user";
import { defineEmits } from "vue";
const opent = ref(true);
const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  userTreeData:{
    type: Object,
    default: ()=>{},
  }
});
const isOpen = ref(props.open);
watch(() => props.open,
  (newValue) => {
    isOpen.value = newValue;
  }
);

const treeData = reactive({data:[]
})
watchEffect(()=>{
  treeData.data[0]=props.userTreeData
  toRaw(treeData.data)
})

const searchText=ref('')
const treeRef = ref()
const SearchPerson=(query)=>{
  treeRef.value?.filter(query)
}
const filterMethod = (query, node) => {
  return node.label?.includes(query)
}
//树
const clickData=ref({})
const handleNodeClick = (data,node) => {
    clickData.value=data
}

const defaultProps = {
  children: 'children',
  label: 'label',
  value:'id'
}
//确定按钮
const emit = defineEmits(["changeMsg"]);
const confirm=()=>{
  if(!clickData.value.id)return ElMessage({ message: '请选择人员', type: 'error' })
  const {disabled,label}=toRefs(clickData.value)
  if(disabled.value){
    return ElMessage({ message: '当前选中的非人员,请重新选择', type: 'error' })
  }else if(clickData.value.id==useUserStore().uid){
    return ElMessage({ message: '当前选择为自己,请重新选择', type: 'error' })
  }
    emit("changeMsg",  clickData.value);
    isOpen.value  = false
}
onMounted(() => {
});
</script>
<style lang="scss" scoped>
@import "@/assets/styles/tree-common.scss";
</style>