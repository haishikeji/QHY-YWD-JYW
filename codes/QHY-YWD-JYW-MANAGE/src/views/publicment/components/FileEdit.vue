<template>
  <div>
    <div class="qualityManual-container-office">
      <jjtOnlyOffice :option="option" />
    </div>
  </div>
</template>

<script>
import { ref, onMounted, defineEmits } from "vue";
import { useRouter, useRoute } from "vue-router";
import jjtOnlyOffice from "@/components/OnlyOffice/index.vue";
import useUserStore from "@/store/modules/user";
// import ClassicEditor from '@ckeditor/ckeditor5-build-classic';
import {
  listInfo,
  getInfo,
  delInfo,
  addInfo,
  updateInfo,
} from "@/api/biz/info";
export default {
  props: {
    docId: {
      type: Number, // 这里的类型应该匹配你要传递的数据类型
    //   required: true, // 如果这个属性是必须的，请添加此项
    },
    copyRow: {
      //点击外层列表的哪项数据
      type: Object,
    //   required: true,
    },
    historyPrew: {
      Boolean,
      default: false,
    },
    historycopyRow: {
      //点击历史预览的哪项数据
      type: Object,
      default: () => {},
    },
    onlyView: {
      Boolean,
      default: false,
    },
  },
  setup(props) {
    let docId = props.docId || '';
    let copyRow = props.copyRow || '';
    const router = useRouter(); //注册路由
    const route = useRoute();
    const uid = useUserStore().uid
    let option = ref({
      key: "",
      url: "", //在线文档地址
      isEdit: props.onlyView, //是否允许编辑
      fileType: "", //文件扩展名
      title: "", //文件标题
      user: {
        id: null, //用户ID
        name: "", //用户姓名
      },
      userdata: "",
      editUrl: "", //回调地址
    });
    let show = ref(true);
    let newObj = ref({});
    function oo(docId, isEdit) {
      if (docId && !props.historyPrew) {
        getInfo(docId).then((response) => {
          const obj = response.data;
          obj.isEdit = isEdit;
          newObj.value = obj;
          option.value.isEdit = obj.isEdit;
          option.value.url = `${window.location.origin}${
            import.meta.env.VITE_APP_BASE_API
          }/api/access/${obj.docId}?uid=${uid}`;
          option.value.title = obj.fileName;
          option.value.key = obj.fileId;
          option.value.fileType = obj.fileType.replace(".", "");
          option.value.editUrl = `${window.location.origin}${
            import.meta.env.VITE_APP_BASE_API
          }/only-office/callback/${obj.docId}?name=${useUserStore().uname}`;
          option.value.user = {
            id: useUserStore().uid, //用户ID
            name: useUserStore().uname, //用户姓名
          };
        });
      } else if (docId && props.historyPrew) {
        option.value.isEdit = false;
        option.value.url = `${window.location.origin}${
          import.meta.env.VITE_APP_BASE_API
        }/api/access/${props.historycopyRow.docId}?uid=${uid}`;
        option.value.title = props.copyRow.fileName;
        option.value.key = props.historycopyRow.fileId;
        option.value.fileType = props.copyRow.fileType.replace(".", "");
        option.value.editUrl = `${window.location.origin}${
          import.meta.env.VITE_APP_BASE_API
        }/only-office/callback/${props.docId}?name=${useUserStore().uname}`;
        option.value.user = {
          id: useUserStore().uid, //用户ID
          name: useUserStore().uname, //用户姓名
        };
        // const emit = defineEmits(["cancleHistoryPrew"]);
        // emit("cancleHistoryPrew",false);
        // console.log(option.value,'option3===');
      }
    }
    /** 修改按钮操作 */
    function edit() {
      oo(docId, true);
    }
    onMounted(() => {
      let route = useRoute(); 
      console.log('route',route.query);
      if(route.query.clickRowId){
          docId = route.query.clickRowId
          copyRow = JSON.parse(route.query.copyRow)
      }
      console.log(docId, "doc");
      console.log(copyRow, "row");
      edit();
      // let row = JSON.parse(newObj.value)
      // console.log(row, '852');
    });
    return {
      option,
      show,
      newObj,
      route,
      uid
    };
  },
  components: {
    jjtOnlyOffice,
  },
};
</script>

<style scoped></style>