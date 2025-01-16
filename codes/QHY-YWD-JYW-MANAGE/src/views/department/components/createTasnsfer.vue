<template>
  <div class="transBox" v-if="open">
    <div class="allBag">
      <el-input style="width: calc(100% - 30px); margin-left: 18px; margin-top: 5px" v-model="allBag" placeholder="搜索"></el-input>
      <div class="allLog">
        <el-tree ref="treeRef" :filter-node-method="filterNode" :data="allTreeData" :props="allTreeProps" show-checkbox
          node-key="id" :default-expanded-keys="openTree" @check="allTreeChange" :default-checked-keys="backScreen"
          :check-strictly="false" >
            <template #default="{ node, data }">
              <span class="custom-tree-node">
                <img v-if="data.disabled" src="@/assets/images/yellowUsers.png" alt="" />
                <img v-else src="@/assets/images/oneUser.png" alt="" />
                <span>{{ node.label }}</span>
              </span>
            </template>
          </el-tree>
      </div>
    </div>
    <div class="needBag">
      <div style="border-bottom: 1px solid gray" class="needBag_top">
        <span>已选择{{ chooseTagData.length }}人</span>
        <span style="color: red; font-size: 14px; float: right" @click="cleanPeople">清空</span>
      </div>
      <div class="needLog">
        <el-scrollbar>
          <el-tag v-for="(item) in chooseTagData" :key="item.userId" class="tagtag" :closable="true"
            @close="handleClose(item)">
            {{ item.userName ? item.userName : item.name }}
          </el-tag>
        </el-scrollbar>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, toRef, watch, defineEmits, toRaw } from "vue";
import userTree from "../../../api/user/userTree";
import fileShare from "../../../api/fileShare/fileShare";
import fileCount from "../../../api/fileCount/fileCount";
import { ElMessage } from "element-plus";
const open = ref(true);
const allTreeData = ref([]);
const chooseTagData = ref([]);
const lastPeople = ref(false);
const openTree = ref([]);
const backScreen = ref(props.checkUser);
const thisFoler = ref(props.upFolderData);
const allCheck = ref(true);
const allTreeProps = {
  label: "label",
  children: "children",
  disabled: false,
  id: "id",
};
let treeRef = ref(null)
const emit = defineEmits(["setUsers"]);
let allBag = ref('')
const props = defineProps({
  openTrans: {
    type: Boolean,
    default: true,
  },
  checkUser: {
    type: Array,
    required: true,
    default: () => [],
  },
  upFolderData: {
    type: Object,
    default: () => {},
  },
});
watch(
  () => props.openTrans,
  (newValue) => {
    console.log(111);
    open.value = newValue;
  }
);
watch(() => allBag.value, (val) => {
  treeRef.value?.filter(val)
})
function filterNode(value, data) {
  if (!value) return true
  return data.label.includes(value)
}
function cleanPeople() {
  treeRef.value.setCheckedKeys([]);
  chooseTagData.value = [];
  emit("setUsers", toRaw(chooseTagData.value));
  setTimeout(() => {
    // antherClean()
  })
}
function antherClean() {
  treeRef.value.setCheckedKeys([]);
  chooseTagData.value = [];
}
function handleClose(tag) {
  chooseTagData.value.splice(chooseTagData.value.indexOf(tag), 1);
  let result = toRaw(chooseTagData.value).map((item) => {
    return item.userId
  })
  // console.log('result',result);
  // console.log('chooseTagData',chooseTagData.value);
  treeRef.value.setCheckedKeys(result);
  emit("setUsers", toRaw(chooseTagData.value));
  setTimeout(() => {
    // antherClose(tag)
  }, 10)
}
function antherClose(tag) {
  chooseTagData.value.splice(chooseTagData.value.indexOf(tag), 1);
  let result = chooseTagData.value.map((item) => {
    return item.userId
  })
  treeRef.value.setCheckedKeys(result);
}
function allTreeChange(e,node,noOne) {
  // console.log('e',e.disabled);
  // console.log('e',e.id);
  if(!noOne){
    allCheck.value = true
  }
  let checkedKeysArr
  let hasThis
  if(node){
   checkedKeysArr = JSON.parse(JSON.stringify(node));
  // console.log("chooseTagData", checkedKeysArr.checkedKeys?.find(item => item == e.id));
  if(e.disabled && checkedKeysArr.checkedKeys?.find(item => item == e.id)){ // 判断是全选还是全不选
    allCheck.value = true
  }else if(e.disabled && !checkedKeysArr.checkedKeys?.find(item => item == e.id)){
    allCheck.value = false
  }
  // 判断当前是勾选还是取消勾选
  if(checkedKeysArr.checkedKeys.find(item => item == e.id)){
    hasThis = true
  }
  }
  if(e.children){
    e.children.forEach(par=>{
      allTreeChange(par,null,true)
    })
  }
  const id = e.id;
  const label = e.label;
  const disabled = e.disabled;
  // 查找是否已经存在相同 userId 的数据的索引
  const existingIndex = chooseTagData.value.findIndex(
    (item) => item.userId === id
  );
    console.log('allCheck.value',allCheck.value);
  if (existingIndex !== -1) {
    // 如果已存在，删除该项
    chooseTagData.value.splice(existingIndex, 1);
  } 
  if(allCheck.value){
    // 选择操作，将数据添加到 needTagData 中
    chooseTagData.value.push({
      userId: id,
      userName: label,
      disabled: disabled,
    });
    if(!noOne && !hasThis){
      const existingIndex = chooseTagData.value.findIndex(
          (item) => item.userId === id
        );
        if (existingIndex !== -1) {
          chooseTagData.value.splice(existingIndex, 1);
        } 
    }
  }
  if (chooseTagData.value.length === 1) {
    lastPeople.value = true;
  } else {
    lastPeople.value = false;
  }
  chooseTagData.value = chooseTagData.value.filter((item) => !item.disabled);
  emit("setUsers", toRaw(chooseTagData.value));
  // console.log("chooseTagData", chooseTagData.value);
}
// 获取用户树
function getAllUser() {
  if(thisFoler.value){
    userTree.getMentUserTreeById(thisFoler.value.dirId).then((res) => {
        allTreeData.value = [res];
        userTree.getMentUserTreeById(thisFoler.value.dirId).then((res) => {
          allTreeData.value = [res];
          // 递归函数来获取所有节点的 id
          function getAllNodeIds(nodes) {
            nodes.forEach((node) => {
              openTree.value.push(node.id);
              if (node.children && node.children.length > 0) {
                getAllNodeIds(node.children);
              }
            });
          }
          // 调用递归函数获取所有节点的 id
          getAllNodeIds(allTreeData.value);
        });
      })
  }else{
    userTree.getMentUserTree().then((res) => {
        allTreeData.value = [res];
        userTree.getMentUserTree().then((res) => {
          allTreeData.value = [res];
          // 递归函数来获取所有节点的 id
          function getAllNodeIds(nodes) {
            nodes.forEach((node) => {
              openTree.value.push(node.id);
              if (node.children && node.children.length > 0) {
                getAllNodeIds(node.children);
              }
            });
          }
          // 调用递归函数获取所有节点的 id
          getAllNodeIds(allTreeData.value);
        });
      })
  }
  ;
}
onMounted(async () => {
  getAllUser();
  const checkUser = toRaw(props.checkUser)
  // console.log('checkUser',checkUser);
  // console.log('allTreeData.value',allTreeData.value);
  //设置勾选项
  let arr = []
  if(checkUser){
    arr = checkUser.map(item => {
       return item.userId
     })
  }
  backScreen.value = arr
  // 设置右侧列表数据
  chooseTagData.value = checkUser.map(item => {
    return {
      userId: item.userId,
      userName: item.userName,
      disabled: false
    }
  })
  // console.log('arr',arr);
});
</script>

<style lang="scss" scoped>
.transBox {
  width: 100%;
  height: 400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-around;
  align-items: center;
  /* border: 1px solid black; */
}

.tagtag {
  display: flex;
  justify-content: space-between;
  align-items: center;
  text-align: start;
  width: calc(100% - 20px);
  margin: 0 auto;
}

.allBag {
  width: 50%;
  height: 380px;
  border: 1px solid green;
}

.needBag {
  width: 50%;
  height: 380px;
  border: 1px solid green;
  overflow-y: auto;
}

.allLog {
  width: calc(100% - 50px);
  height: 330px;
  margin: 5px auto;
  /* border: 1px solid red; */
  overflow-y: auto;
}
.custom-tree-node{
  display: flex;
  align-items: center;
  img{
    width: 22px;
    height: 22px;
  }
}

.needBag_top {
  padding: 0 10px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.needLog {
  height: calc(100% - 30px);
}

:deep(.el-tag__content) {
  display: block;
}

:deep(.el-icon el-tag__close) {
  display: block;
}
</style>