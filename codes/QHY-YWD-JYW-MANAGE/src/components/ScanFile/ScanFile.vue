<template>
  <div>
    <el-dialog
      v-model="props.openScan"
      width="964px"
      top="20vh"
      title="扫描文档"
      :close-on-click-modal="false"
      @close="closeOpen"
    >
      <div class="main">
        <!-- 头部的总计和移动按钮 -->
        <div class="statistics">
          <el-checkbox v-model="checkedAll" @change="checkAllChange"
            >共{{ props.scannerFiles.length }}项</el-checkbox
          >
          <div class="">
            <el-button color="#2E6BC8" @click="toSave">
              <img src="@/assets/images/folder_white.png" alt="" />&nbsp; 移动到
            </el-button>
          </div>
        </div>
        <!-- 盒子区域 -->
        <div class="bigBox" id="bigBox">
          <div v-for="(item, index) in copyList" :key="item" class="one_box">
            <img :src="wangzhi + item.path" alt="" />
            <div class="top_check">
              <el-checkbox
                :checked="fileArr.some((par) => par == item.q)"
                @change="checkChange(item, index)"
              ></el-checkbox>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
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
} from "vue";
import Sortable from "sortablejs";
import { ElMessage } from "element-plus";
const props = defineProps({
  openScan: {
    type: Boolean,
    default: false,
  },
  scannerFiles: {
    type: Array,
    default: () => [],
  },
});

const checkedAll = ref(false); //是否全选
const copyList = ref(props.scannerFiles);
const total = ref(333); //总计数量
const wangzhi = import.meta.env.VITE_APP_BASE_API;
const fileArr = ref([]);
const savaFileArr = ref([])
const emit = defineEmits(["closeOpen", "saveScanFile"]);
const closeOpen = () => {
  emit("closeOpen");
};
// const checkChange = (item,index) => {
//   console.log('item',item);
//   console.log('index',index);
//   let arr = fileArr.value;
//   if (arr.some((par) => par == item.q)) {
//     arr = arr.filter((par) => par != item.q);
//   } else {
//     arr.push(item.q);
//   }
//   // if (arr.some((par) => par == item.q)) {
//   //   arr = arr.filter((par) => par != item.q);
//   // } else {
//   //   arr.push({q:item.q,index:index});
//   // }
//   // 判断全选
//   if (arr.length == props.scannerFiles.length) {
//     checkedAll.value = true;
//   } else {
//     checkedAll.value = false;
//   }
//   fileArr.value = JSON.parse(JSON.stringify(arr));
//   console.log("fileArr", toRaw(fileArr.value));
// };
const checkChange = (item, index) => {
  // console.log("item", item);
  // console.log("index", index);
  let arr = copyList.value;
  copyList.value = arr.map((par) => {
    if (item.q == par.q) {
      if (par.checked) {
        par.checked = false;
      } else {
        par.checked = true;
      }
    }
    return par;
  });
  // console.log('chengeC',copyList.value);
};
const checkAllChange = () => {
  // console.log("checkedAll.value", checkedAll.value);
  fileArr.value = [];
  if (checkedAll.value) {
    //全选
    props.scannerFiles.forEach((item) => {
      fileArr.value.push(item.q);
    });
    const oldArr = JSON.parse(JSON.stringify(copyList.value));
    copyList.value = oldArr.map((item) => {
      item.checked = true;
      return item;
    });
  } else {
    const oldArr = JSON.parse(JSON.stringify(copyList.value));
    copyList.value = oldArr.map((item) => {
      item.checked = false;
      return item;
    });
  }
  // console.log("fileArr", fileArr.value);
};
const toSave = () => {
  // console.log("copyList", copyList.value);
  if (!copyList.value.some((item) => item.checked == true)) {
    return ElMessage({ message: "请先勾选文件", type: "error" });
  }
  const arr = [];
  copyList.value.forEach((item) => {
    if (item.checked) {
      arr.push(toRaw(item));
    }
  });
  // console.log('arr',arr);
  // console.log('chengeC',copyList.value);
  emit("saveScanFile", arr);
};
const rowDrop = () => {
  const el = document.getElementById("bigBox"); //找到想要拖拽的那一列
  Sortable.create(el, {
    //结束拖拽事件
    async onEnd({ newIndex, oldIndex }) {
      const newItem = copyList.value[newIndex];
      const oldItem = copyList.value[oldIndex];
      copyList.value[newIndex] = oldItem;
      copyList.value[oldIndex] = newItem;
      // console.log("2copyList.value", copyList.value);/
    },
  });
};
onMounted(async () => {
  setTimeout(() => {
    // rowDrop();
    const arr = copyList.value;
    copyList.value = arr.map((item) => {
      item.checked = false;
      return toRaw(item);
    });
    console.log("copyList.value", copyList.value);
  }, 500);
});
</script>

<style lang="scss" scoped>
.main {
  width: 100%;
}
.statistics {
  width: 100%;
  height: 50px;
  border-bottom: 1px solid #c1cce3;
  padding: 0 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.bigBox {
  margin-top: 16px;
  width: 100%;
  padding: 0 16px;
  max-height: 700px;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  overflow-y: auto;
  .one_box {
    width: 300px;
    height: 220px;
    border: 1px solid #c1cce3;
    box-sizing: border-box;
    margin-bottom: 16px;
    border-radius: 4px 4px 4px 4px;
    box-shadow: 0px 2px 10px 1px rgba(199, 203, 216, 0.4);
    position: relative;
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .top_check {
      position: absolute;
      top: -3%;
      right: 1%;
    }
  }
}
:deep(.el-dialog__body) {
  padding: 0 !important;
}
</style>