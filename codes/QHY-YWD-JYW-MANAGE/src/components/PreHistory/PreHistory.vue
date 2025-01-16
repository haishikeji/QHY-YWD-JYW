<template>
  <div>
    <jjtOnlyOffice :option="option" :usersArr="usersArr" :hisArr="hisArr" />
  </div>
</template>

<script setup>
import jjtOnlyOffice from "@/components/OnlyOffice/index.vue";
import { ref, toRaw, onMounted, inject, onActivated, watch } from "vue";
import { useRouter, useRoute, onBeforeRouteUpdate } from "vue-router";
import { getTemplateUsers } from "@/api/biz/template.js";
import { ElMessage } from "element-plus";
import useUserStore from "@/store/modules/user";
import { getInfo } from "@/api/biz/info";
const props = defineProps({
  showPreview: {
    type: Boolean,
    default: false,
  },
  historyDocId: {
    type: Number,
    default: 0,
  },
  historyFileId: {
    type: Number,
    default: 0,
  },
});
const usersArr = ref([]);
const option = ref({
  key: "",
  url: "", //在线文档地址
  isEdit: true, //是否允许编辑
  fileType: "", //文件扩展名
  title: "", //文件标题
  user: {
    id: null, //用户ID
    name: "", //用户姓名
  },
  userdata: "",
  editUrl: "", //回调地址
});
const close = () => {};
function oo(docId, fileId) {
  getInfo(docId).then((response) => {
    const obj = response.data;
    option.value.isEdit = false;
    option.value.isCopy = false;
    option.value.url = `${window.location.origin}${
      import.meta.env.VITE_APP_BASE_API
    }/api/history/${fileId}`;
    console.log("url = ", option.value.url);
    option.value.title = obj.fileName;
    option.value.key = fileId;
    option.value.fileType = obj.fileType.replace(".", "");
    option.value.editUrl = `${window.location.origin}${
      import.meta.env.VITE_APP_BASE_API
    }/only-office/callback/${props.docId}?name=${useUserStore().uname}`;
    option.value.user = {
      id: useUserStore().uid, //用户ID
      name: useUserStore().uname, //用户姓名
    };
  });
}
const getUsers = async () => {
  const res = await getTemplateUsers();
  console.log("res", res.data);
  usersArr.value = res.data;
};
onMounted(() => {
  if(props.historyFileId){
    oo(props.historyDocId, props.historyFileId);
    return
  }
  let route = useRoute();
  getUsers();
  console.log("route", route.query);
  let docId = route.query.clickRowId;
  let fileId = route.query.fileId;
  oo(docId, fileId);
});
</script>

<style lang="scss" scoped>
</style>
