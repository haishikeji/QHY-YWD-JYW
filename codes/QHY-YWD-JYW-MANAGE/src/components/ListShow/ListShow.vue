<template>
  <div>
    <div class="topMenu">
      <div class="left_search">
        <div class="search_box">
          <el-input
            v-model="searchFire"
            @keyup.enter="fileBlur"
            @input="fileBlur"
            class="searchFire"
            size="large"
            placeholder="搜索文件"
          />
          <el-icon class="SearchIcon" @click="fileBlur">
            <Search />
          </el-icon>
        </div>
        <div class="line">|</div>
        <div class="searchFor">
          <div class="search_title">筛选：</div>
          <el-tabs
            v-model="searchType"
            class="demo-tabs"
            @tab-change="changeSearchType"
          >
            <el-tab-pane label="全部" name="all"></el-tab-pane>
            <el-tab-pane label="文档" name="doc"></el-tab-pane>
            <el-tab-pane label="图片" name="img"></el-tab-pane>
            <el-tab-pane label="音频" name="audio"></el-tab-pane>
            <el-tab-pane label="视频" name="video"></el-tab-pane>
            <el-tab-pane label="压缩包" name="zip"></el-tab-pane>
          </el-tabs>
        </div>
      </div>
      <div class="changeIcon">
        <img
          src="@/assets/images/sort.png"
          @click="changeSort"
          v-if="isAsc == 'asc'"
          alt=""
        />
        <img
          src="@/assets/images/Frame_188.png"
          @click="changeSort"
          v-else
          alt=""
        />
      </div>
    </div>
    <div class="table_box">
      <el-table
        :data="tableData"
        style="width: 100%; height: calc(88vh - 48px)"
        ref="container"
        @row-contextmenu="rightClick"
        @selection-change="SelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="名称" :show-overflow-tooltip="true">
          <template #default="scope">
            <div class="flie_name">
              <el-icon
                v-if="scope.row.isFavorite == 'N'"
                @click.stop="collect(scope.row, false)"
                style="margin-right: 7px"
              >
                <Star />
              </el-icon>
              <img
                v-else
                @click.stop="dCollect(scope.row, false)"
                src="../../assets/images/yellowstar.png"
                alt=""
                style="margin-right: 5px"
              />
              <img
                class="table_icon"
                :src="setIcon(scope.row.fileType)"
                alt=""
                style=""
              />
              {{ scope.row.fileName }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="200" />
        <el-table-column prop="fileType" label="类型" width="200" />
        <el-table-column prop="fileSize" label="大小" width="200">
          <template #default="scope">
            <div>{{ formatFileSize(scope.row.fileSize) }}</div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch, nextTick, inject } from "vue";
//---------------导入图片--------------------------
import file_DOC from "@/assets/images/fileType/file_DOC.png";
import file_pdf from "@/assets/images/fileType/file_pdf.png";
import file_PPT from "@/assets/images/fileType/file_PPT.png";
import file_TXT from "@/assets/images/fileType/file_TXT.png";
import file_XLSX from "@/assets/images/fileType/file_XLSX.png";
import file_pic from "@/assets/images/fileType/file_pic.png";
import file_audio from "@/assets/images/fileType/file_audio.png";
import file_video from "@/assets/images/fileType/file_video.png";
import file_zip from "@/assets/images/fileType/file_zip.png";
//----------------------------------------
const searchType = ref("all");
const baseData = ref(); //拿到的全部数据
const typeData = ref(); //当前分类下的数据
const tableData = ref(); // 表格展示的数据
const searchFire = ref();
const copyFileType = ref();
const isAsc = ref("asc");

const props = defineProps({
  fileList: {
    type: Array,
    default: () => [],
  },
});
const emit = defineEmits([
  "handleRowClick",
  "delCollect",
  "collectByStar",
  "changeListSort",
  "handleSelectionChange",
]);
// 排序
const changeSort = async () => {
  isAsc.value == "asc" ? (isAsc.value = "desc") : (isAsc.value = "asc");
  // refreshFile();
  emit("changeListSort", isAsc.value);
};
// 切换搜索分类
const changeSearchType = (name) => {
  let baseDataObj = props.fileList;
  // console.log("baseDataObj", baseDataObj);
  if (name == "all") {
    if (searchFire.value) {
      tableData.value = baseDataObj.filter((item) => {
        if (item.fileName.includes(searchFire.value)) {
          return item;
        }
      });
    }else{
      tableData.value = baseDataObj;
    }
  } else {
    // 获取分类具体数据
    // console.log("name", name);
    const typeArr = setType(name);
    // console.log(typeArr, "类型");
    typeData.value = toRaw(baseDataObj).filter((item) => {
      return typeArr.includes(item.fileType);
    });
    if (searchFire.value) {
      tableData.value = typeData.value.filter((item) => {
      if (item.fileName.includes(searchFire.value)) {
        return item;
      }
    });
    }else{
      tableData.value = typeData.value;
    }
    
    // console.log(tableData.value, "数组");
  }
};
//搜索框模糊搜索事件
const fileBlur = () => {
  const arr = searchType.value != "all"?typeData.value: props.fileList;
  console.log("props.fileList", props.fileList);
  console.log("typeData.value", typeData.value);
  if(searchFire.value){
    tableData.value = arr?.filter((item) => {
      if (item.fileName.includes(searchFire.value)) {
        return item;
      }
    });
  }else{
    tableData.value = arr;
  }
};
// 勾选框发生改变
const SelectionChange = (val) => {
  emit("handleSelectionChange", val);
};

const rightClick = (row, col, e) => {
  emit("handleRowClick", row, null, e);
};
const collect = (row, bool) => {
  emit("collectByStar", row, bool);
};
const dCollect = (row, bool) => {
  emit("delCollect", row, bool);
};

//工具函数
//对mouseCli数组进行筛选，实现菜单的区分显示
const filterMouseCli = () => {
  const canPreviewArray = [
    ".doc",
    ".dps",
    ".et",
    ".wps",
    ".docm",
    ".docx",
    ".dot",
    ".dotm",
    ".dotx",
    ".epub",
    ".fodt",
    ".htm",
    ".html",
    ".mht",
    ".odt",
    ".ott",
    ".pdf",
    ".rtf",
    ".txt",
    ".djvu",
    ".xps",
    ".csv",
    ".fods",
    ".ods",
    ".ots",
    ".xls",
    ".xlsm",
    ".xlsx",
    ".xlt",
    ".xltm",
    ".xltx",
    ".fodp",
    ".odp",
    ".otp",
    ".pot",
    ".potm",
    ".potx",
    ".pps",
    ".ppsm",
    ".ppsx",
    ".ppt",
    ".pptm",
    ".pptx",
  ];
  const typeArr = [".png", ".jpg", ".jpeg", ".JPG", ".PNG", ".mp3", ".mp4"];
  const imgTypeArr = [".png", ".jpg", ".jpeg", ".JPG", ".PNG"];
  const canEditArr = [
    ".doc",
    ".docm",
    ".docx",
    ".dot",
    ".dotm",
    ".dotx",
    ".txt",
    ".djvu",
    ".xps",
    ".csv",
    ".fods",
    ".ods",
    ".ots",
    ".xls",
    ".xlsm",
    ".xlsx",
    ".xlt",
    ".xltm",
    ".xltx",
    ".fodp",
    ".odp",
    ".otp",
    ".doc",
    ".docm",
    ".docx",
    ".dot",
    ".dotm",
    ".dotx",
    ".epub",
    ".fodt",
    ".htm",
    ".html",
    ".mht",
    ".odt",
    ".ott",
    ".rtf",
    ".txt",
    ".djvu",
    ".xps",
    ".wps",
    ".dps",
    ".et",
    ".pptx",
  ];
  let arr = [];
  if (
    !(
      typeArr.includes(copyFileType.value) ||
      canPreviewArray.includes(copyFileType.value)
    )
  ) {
    arr = mouseCli.value.filter((item) => item.name !== "预览");
  } else {
    arr = toRaw(mouseCli.value);
  }
  if (!canEditArr.includes(copyFileType.value)) {
    arr = arr.filter(
      (item) =>
        item.name !== "在线编辑" &&
        item.name !== "协作" &&
        item.name !== "历史版本"
    );
  }
  if (clickRow.value.isFiled === "Y") {
    arr = arr.filter(
      (item) => item.name !== "在线编辑" && item.name !== "协作"
    );
  }
  if (!imgTypeArr.includes(copyFileType.value)) {
    arr = arr.filter((item) => item.name !== "文字识别");
  }
  // console.log('arr',arr);
  if (thisFolderRole.value) {
    arr = rightMenuRole(toRaw(thisFolderRole.value), arr);
  }
  return arr;
};
// 设置图标
const setIcon = (fileType) => {
  switch (fileType) {
    case ".docx":
      return file_DOC;
      break;
    case ".pdf":
      return file_pdf;
      break;
    case ".ppt":
      return file_PPT;
      break;
    case ".txt":
      return file_TXT;
      break;
    case ".xlsx":
      return file_XLSX;
      break;
    case ".csv":
      return file_XLSX;
      break;
    case ".png":
      return file_pic;
      break;
    case ".mp3":
      return file_audio;
      break;
    case ".mp4":
      return file_video;
      break;
    case ".zip":
      return file_zip;
      break;
    default:
      return file_DOC;
      break;
  }
};
// 设置分类
const setType = (fileType) => {
  switch (fileType) {
    case "doc":
      return [
        ".txt",
        ".ppt",
        ".pdf",
        ".docx",
        ".csv",
        ".wps",
        ".xls",
        ".pptxs",
        ".dll",
        ".pptx",
        ".doc",
      ];
      break;
    case "img":
      return [".png", ".PNG", ".jpg", ".jpeg"];
      break;
    case "audio":
      return [".mp3"];
      break;
    case "video":
      return [".mp4", ".qlv"];
      break;
    case "zip":
      return [".zip", ".rar", ".7z"];
      break;
    default:
      return [""];
      break;
  }
};
function formatFileSize(fileSize) {
  if (fileSize >= 1024 * 1024 * 1024) {
    // 大于等于1GB，显示GB
    return (fileSize / (1024 * 1024 * 1024)).toFixed(2) + "GB";
  } else if (fileSize >= 1024 * 1024) {
    // 大于等于1MB，显示MB
    return (fileSize / (1024 * 1024)).toFixed(2) + "MB";
  } else if (fileSize >= 1024) {
    // 大于等于1KB，显示KB
    return (fileSize / 1024).toFixed(2) + "KB";
  } else {
    // 小于1KB，显示字节
    return fileSize + "B";
  }
}
watch(
  () => props.fileList,
  (newValue, oldValue) => {
    tableData.value = newValue;
    changeSearchType(searchType.value);
    // console.log("watchtableData", tableData.value);
  },
  {
    immediate: true,
    deep: true,
  }
);
</script>

<style lang="scss" scoped>
.topMenu {
  width: 100%;
  height: 48px;
  border: 1px solid #c1cce3;
  background: #f5f7f9;
  padding: 0 16px;
  //   box-sizing: border-box;
  display: flex;
  align-items: center;
  .left_search {
    width: 100%;
    height: 48px;
    display: flex;
    align-items: center;
  }
  .search_box {
    width: 320px;
    height: 48px;
    display: flex;
    align-items: center;
    position: relative;
    .SearchIcon {
      color: gray;
      position: absolute;
      right: 22px;
    }
  }
  .line {
    margin: 0 16px;
    color: #c1cce3;
  }
  .searchFor {
    height: 100%;
    display: flex;

    // align-items: center;
    .search_title {
      height: 100%;
      line-height: 48px;
      font-size: 14px;
      font-weight: 500;
      font-family: Inter-Medium;
    }
  }
}

.setCli {
  width: 156px;
  max-height: auto;
  position: absolute;
  top: -70px;
  left: 300px;
  flex-wrap: 400;
  background-color: white;
  border: 1px solid gray;
  border-radius: 4px;
  z-index: 3;
  overflow-y: auto;
  // font-size: 16px;
}

.setCli1 {
  width: 156px;
  height: auto;
  position: absolute;
  background-color: white;
  border: 1px solid gray;
  border-radius: 4px;
  z-index: 100;
  overflow-y: auto;
}

.chooseSet {
  width: 140px;
  height: 30px;
  line-height: 30px;
  margin: 5px auto;
  font-size: 13px;
  cursor: pointer;
  // display: flex;
  // align-items: center;
  // position: relative;
  img {
    margin-right: 4px;
    vertical-align: middle;
  }
}

.chooseSet:hover {
  width: 140px;
  height: 30px;
  line-height: 30px;
  margin: 5px auto;
  font-size: 13px;
  background-color: #f5f7f9;
  /* color: white; */
}

.table_box {
  height: calc(88vh - 48px);
  // border: 1px solid #000;
}
// tag间距
:deep(.el-tabs) {
  --el-tabs-header-height: 48px !important;
}
:deep(.el-tabs__item) {
  padding: 0 16px !important;
  color: #505870 !important;
  font-weight: 400 !important;
}

// tag选中颜色
:deep(.el-tabs__item.is-active) {
  color: #2e6bc8 !important;
  font-weight: normal;
  font-family: Inter-SemiBold;
}

:deep(.el-tabs__active-bar) {
  background-color: #2e6bc8;
}

:deep(.el-table__inner-wrapper::before) {
  background-color: #fff !important;
}

:deep(.el-collapse, .el-collapse-item__wrap) {
  border: none;
}

.table_icon {
  height: 27px;
  width: 27px;
  vertical-align: middle;
}

:deep(.el-collapse-item__content) {
  padding-bottom: 0;
}

:deep(.el-collapse-item__header) {
  background-color: #ebeff6 !important;
  width: 100% !important;
  height: 24px !important;
}

:deep(.el-collapse-item__arrow) {
  position: relative;
  color: #2e6bc8;
  right: 97%;
}

:deep(.el-table td.el-table__cell) {
  border: none;
  font-size: 14px !important;
  font-weight: 400 !important;
  color: #000 !important;
}

:deep(.el-table__row) {
  height: 35px !important;
}

:deep(.el-table .el-table__header-wrapper th) {
  border-bottom: none;
  border-right: 1px solid #c1cce3;
  background-color: #fff !important;
  color: #505870;
  font-size: 14px;
}
</style>