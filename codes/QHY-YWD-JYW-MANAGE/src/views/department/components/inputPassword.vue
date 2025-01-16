<template>
  <div>
    <div>
      <el-dialog v-model="isOpen" :title="inputTitle" width="30%">
        <el-input v-model="password" @keyup.enter="sure" type="password"></el-input>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="close">取消</el-button>
            <el-button type="primary" @click="sure">确认</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, toRef, watch } from "vue";
import { sm3 } from "sm-crypto";
const props = defineProps({
  showInputPassword: {
    type: Boolean,
    default: false,
  },
  isCode: {
    type: Boolean,
    default: false,
  },
  inputTitle: {
    type: String,
    default: '',
  },
});
const emit = defineEmits(["closeIptPass", "surePass"]);
watch(
  () => props.showInputPassword,
  (newValue) => {
    // console.log(111);
    isOpen.value = newValue;
  }
);

const password = ref();
const isOpen = ref(props.showInputPassword);
const close = () => {
  password.value = "";
  emit("closeIptPass");
};
const sure = () => {
    if(props.isCode){ //验证私有码
    emit("sureCode", sm3(password.value));
  }else{//验证密码
    emit("surePass", sm3(password.value));
  }
  password.value = "";
};
</script>

<style lang="scss" scoped>
</style>