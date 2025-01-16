<template>
  <!-- 图片预览 -->
  <div class="demo-image__preview" v-if="canSeeImg(copyFileType)">
    <el-image-viewer
      v-if="showImageViewer"
      :url-list="[previewData]"
      @close="closeImgPreview"
    />
  </div>
  <!-- 视频预览 -->
  <el-dialog
    v-model="showImageViewer"
    width="40%"
    top="40vh"
    draggable
    title="预览"
    v-if="copyFileType === '.mp4'"
    :close-on-click-modal="false"
    :before-close="closeImgPreview"
  >
    <video
      :src="previewData"
      ref="video"
      controls
      :style="{ width: '100%', height: '100%' }"
    ></video>
  </el-dialog>
  <!-- 音频预览 -->
  <el-dialog
    v-model="showImageViewer"
    width="40%"
    title="预览"
    draggable
    :close-on-click-modal="false"
    v-if="copyFileType === '.mp3'"
    :before-close="closeImgPreview"
  >
    <audio :src="previewData" ref="audio" controls></audio>
  </el-dialog>
</template>

<script setup>
import {
  ref,
  computed,
  reactive,
  defineComponent,
  watch,
  onMounted,
  defineExpose,
  onDeactivated
} from "vue";
const props = defineProps({
  showPreview: {
    type: Boolean,
    default: false,
  },
  previewData: {
    type: String,
    default: "",
  },
  copyFileType: {
    type: String,
    default: "",
  },
});
watch(
  () => props.showPreview,
  (newValue) => {
    // console.log(111);
    showImageViewer.value = newValue;
    if(newValue == false){
      // console.log('watch',video.value);
    }
  },
  () => props.previewData,
  (newValue) => {
    // console.log("previewData", newValue);
    // showImageViewer.value = newValue;
  }
);
const previewImg = ref(null);
const showImageViewer = ref(false); //组件是否显示
const video = ref(null); 
const audio = ref(null); 
const emit = defineEmits(["closeImgPreview"]);
const showImg = () => {
  //预览大图
  showImageViewer.value = true;
};
const closeImgPreview = () => {
  //必须要这个事件 不然点击右上角关闭按钮没有反应
  console.log('sonClose');
  if(props.copyFileType === '.mp3'){
    const audioDom = document.getElementsByTagName("audio")[0];
    audioDom.pause()
  }
  if(props.copyFileType === '.mp4'){
    const videoDom = document.getElementsByTagName("video")[0];
    videoDom.pause()
  }

  // video.value.pause();
  // audio.value.pause();
  emit("closeImgPreview");
};
const canSeeImg = (copyFileType)=>{
  // console.log('copyFileType',props.copyFileType);
  const arr = ['.jpg','.JPG','.png','.PNG','.jpeg','JPEG','gif','GIF']
  if(arr.includes(copyFileType)){
    // console.log(1);
    return true
  }else{
    return false
  }
}
onMounted(()=>{
  // console.log('audio',audio.value);
})
onDeactivated(()=>{
  // console.log('audio',audio.value);
})
</script>

<style scoped>
.demo-image__error .image-slot {
  font-size: 30px;
}
.demo-image__error .image-slot .el-icon {
  font-size: 30px;
}
.demo-image__error .el-image {
  width: 100%;
  height: 200px;
}
.video_box {
  position: fixed;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #fff;
  z-index: 10000;
  /* top: 20%;
  left: 40%; */
}
</style>
