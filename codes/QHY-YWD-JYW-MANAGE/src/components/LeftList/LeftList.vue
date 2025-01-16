<template>
  <div>
    <el-tree
      class="treebox"
      :data="treeData"
      :props="defaultProps"
      :accordion="false"
      ref="folderTreeRef"
      :highlight-current="true"
      :current-node-key="curNode"
      :filter-node-method="filterNode"
      node-key="id"
      :default-expand-all="false"
      :default-expanded-keys="curArr"
    >
      <template #default="{ node, data }">
        <span
          class="custom-tree-node row-context"
          :class="{ alive: data.id == collectDir }"
          @click="checkoutNode(node, data)"
          @click.right="rightFn(node, data, $event)"
        >
          <img :src="setImg(data.remark)" alt="" />
          <span class="node_title">{{ node.label }}</span>
          <span class="fileNum">{{
            data.remark.fileNum > 99 ? "99+" : data.remark.fileNum
          }}</span>
        </span>
      </template>
    </el-tree>
  </div>
</template>

<script setup>
import {
  ref,
  computed,
  reactive,
  defineComponent,
  watch,
  toRaw,
  onMounted,
  defineExpose,
  onDeactivated,
  onBeforeMount,
} from "vue";
import { ElMessage } from "element-plus";
import documents from "@/api/document/document";
import { getDir } from "@/api/biz/dir.js";
import { setListImg } from "@/utils/index.js";
const { proxy } = getCurrentInstance();
const props = defineProps({
  spaceType: {
    type: Number,
    default: 0,
  },
  ListtreeData: {
    type: Array,
    default: () => [],
  },
  searchFolderTree: {
    type: String,
    default: "",
  },
  collectId: {
    type: Number,
    default: 0,
  },
});
const emit = defineEmits(["listfolderClick", "treeRClick"]);
const treeData = ref();
console.log("treeData", treeData.value);
const curArr = ref([]); //默认展开一级目录
const folderTreeRef = ref();
const curNode = ref();
const topId = ref(); //顶层的id
const collectDir = ref();
const defaultProps = ref({
  label: "label",
  value: "id",
  remark: "remark",
  disable: false,
});
//获取文件树
function getFileTree() {
  documents.fileTree(props.spaceType).then((res) => {
    res.disabled = true;
    treeData.value = [res];
    function getAllNodeIds(nodes) {
      nodes.forEach((node) => {
        if (node.remark) {
          node.remark = JSON.parse(node.remark);
        }
        if (node.children && node.children.length > 0) {
          getAllNodeIds(node.children);
        }
      });
      return nodes;
    }
    // 调用递归函数获取所有节点的 id
    getAllNodeIds(treeData.value);
    // console.log("treedataarr", arr);
    // console.log("treeData", treeData.value);
  });
}
const checkoutNode = async (node, data) => {
  // console.log("treeData", treeData.value);
  // console.log("node", node);
  collectDir.value = null;
  curNode.value = node.data.id;
  // console.log("curNode.value", curNode.value);
  // console.log("data", data);
  let thisNode;
  function getLevel2(nodes) {
    if (nodes?.level === 2) {
      // console.log(2);
      // console.log("nodes", nodes);
      thisNode = toRaw(nodes);
    } else {
      if (!nodes.parent) {
        return;
      }
      // console.log("level", nodes.level);
      getLevel2(nodes.parent);
    }
  }
  getLevel2(node);
  // console.log("thisNode", thisNode);
  // console.log("node", node);
  const res = await getDir(data.id);
  let resThis;
  if (thisNode) {
    resThis = await getDir(thisNode.data.id);
  }
  emit("listfolderClick", res.data, resThis?.data);
  // console.log("res", res);
  // console.log("resThis", resThis);
};
const setImg = (remark) => {
  return setListImg(remark);
};
const getTopDir = async () => {
  curArr.value = [];
  const res = await documents.getTop(props.spaceType);
  // console.log("topres", res);
  curArr.value.push(res.dirId);
  curArr.value.push(props.collectId);
  console.log("curArr", curArr.value);
  topId.value = res.dirId;
  return;
};
const rightFn = async (node, data, e) => {
  // console.log("node", node);
  // console.log("data", data);
  // console.log("e", e);
  //如果是二级及以后(node.level >= 3)的节点 需要判断父级是不是组织目录来确定是否显示编辑
  if (node.level === 1) return;
  if (node.level == 2) {
    const res = await getDir(data.id);
    emit("treeRClick", res.data, null, e);
  } else {
    const res = await getDir(data.id);
    const fRes = await getDir(node.parent.data.id);
    if (fRes.data.dirType == "2") {
      // 父级是组织 不可以编辑
      emit("treeRClick", res.data, null, e);
    } else {
      emit("treeRClick", res.data, null, e);
    }
    // console.log("fRes", fRes);
  }
};
/** 通过条件过滤节点  */
const filterNode = (value, data) => {
  if (!value) return true;
  return data.label.indexOf(value) !== -1;
};
watch(
  () => props.ListtreeData,
  (newValue, oldValue) => {
    treeData.value = newValue;
    // 记录当前展开的节点
    let nodesMap = folderTreeRef.value?.store.nodesMap;
    // console.log("nodesMap", nodesMap);
    // console.log("curNode.value", curNode.value);
    let expandedNodes = [];
    if (nodesMap) {
      expandedNodes = Object.keys(nodesMap).filter(
        (key) => nodesMap[key].expanded
      );
    } else {
      expandedNodes.push(topId.value);
    }
    // console.log("expandedNodes", expandedNodes);
    // 把这当从服务器拉取数据
    treeData.value = JSON.parse(JSON.stringify(newValue));
    // 更新数据后, 有过滤条件的执行过滤, 没有的还原之前的展开状态
    setTimeout(() => {
      if (props.searchFolderTree !== "") {
        folderTreeRef.value?.filter(props.searchFolderTree);
      } else {
        expandedNodes.forEach((key) => (nodesMap[key].expanded = true));
      }
      folderTreeRef.value?.setCurrentKey(curNode.value);
    }, 0);
  },
  {
    immediate: true,
    deep: true,
  }
);
/** 根据名称筛选部门树 */
watch(
  () => props.searchFolderTree,
  (newValue, oldValue) => {
    // console.log("watch", newValue);
    folderTreeRef.value?.filter(newValue);
    // proxy.$refs["folderTreeRef"].filter(newValue);
  },
  {
    immediate: true,
    deep: true,
  }
);
watch(
  () => props.collectId,
  (newValue, oldValue) => {
    // console.log("watch", newValue);
    collectDir.value = newValue;
    curArr.value.push(newValue);
    // proxy.$refs["folderTreeRef"].filter(newValue);
  },
  {
    immediate: true,
    deep: true,
  }
);
onMounted(() => {
  getTopDir();
  // collectDir.value = props.collectId;

  // getFileTree();
});
</script>

<style lang="scss" scoped>
.treebox {
  width: 100%;
  overflow-y: auto;
  position: relative;
}
// .row-context {
//    background: #f4f9ff;
//    width: auto;
//    border: 1px solid #4a97eb;
//    padding: 5px 5px 5px 0;
//  }
.custom-tree-node {
  width: 100%;
  height: 36px;
  display: flex;
  align-items: center;

  padding: 16px 0;
  img {
    width: 26px;
    height: 26px;
    object-fit: cover;
  }
  .node_title {
    font-size: 14px;
    line-height: 22px;
    margin-left: 5px;
    max-width: 150px;
    /*第一步： 溢出隐藏 */
    overflow: hidden;
    /* 第二步：让文本不会换行， 在同一行继续 */
    white-space: nowrap;
    /* 第三步：用省略号来代表未显示完的文本 */
    text-overflow: ellipsis;
    font-family: Inter-Medium;
  }
  .fileNum {
    position: absolute;
    right: 10%;
    font-size: 14px;
    line-height: 22px;
    font-weight: 500;
    color: #06286c;
  }
}
.alive {
  background-color: #f5f7f9;
}
:deep(.el-tree-node__content) {
  height: 36px;
}
// 箭头

:deep(.el-tree .el-icon svg) {
  //原有的箭头 去掉
  display: none !important;
  height: 0;
  width: 0;
}
:deep(.el-tree-node__expand-icon) {
  //引入的图标（图片）size大小 => 树节点前的预留空间大小
  font-size: 16px;
}

//图标是否旋转，如果是箭头类型的，可以设置旋转90度。如果是两张图片，则设为0
:deep(.el-tree .el-tree-node__expand-icon.expanded) {
  -webkit-transform: rotate(0deg);
  transform: rotate(0deg);
}

:deep(.el-tree .el-tree-node__expand-icon:before) {
  // 未展开的节点
  background: url("@/assets/images/listShow/arrow_right.png") no-repeat 0 1px !important;
  content: "";
  display: block;
  width: 18px;
  height: 18px;
}

:deep(.el-tree .el-tree-node__expand-icon.expanded:before) {
  //展开的节点
  background: url("@/assets/images/listShow/row_btm.png") no-repeat 0 0 !important;
  content: "";
  display: block;
  width: 18px;
  height: 18px;
  margin-top: 4px;
}

:deep(.el-tree .is-leaf.el-tree-node__expand-icon::before) {
  //叶子节点（不显示图标）
  display: block;
  background: none !important;
  content: "";
  width: 18px;
  height: 18px;
}
:deep(.el-tree-node) {
  min-width: 100% !important;
  width: fit-content !important;
}
</style>