<template>
  <div class="login">
    <!-- 右侧登录盒子 -->
    <div class="login_box">
      <el-form
        ref="loginRef"
        :model="loginForm"
        :rules="loginRules"
        class="center_box"
      >
        <span class="title">云文档系统</span>
        <el-form-item prop="username">
          <span class="ipt_title">用户名</span>
          <el-input
            v-model="loginForm.username"
            type="text"
            size="large"
            auto-complete="off"
            placeholder="请输入用户名"
          >
          </el-input>
        </el-form-item>
        <!-- <el-form-item prop="username">
          <span class="ipt_title">真实姓名</span>
          <el-input
            v-model="loginForm.username"
            type="text"
            size="large"
            auto-complete="off"
            placeholder="请输入真实姓名"
          >
          </el-input>
        </el-form-item> -->
        <el-form-item prop="password">
          <span class="ipt_title">密码</span>
          <el-input
            v-model="loginForm.password"
            type="password"
            size="large"
            auto-complete="off"
            placeholder="请输入密码"
            @keyup.enter="handleLogin"
          >
          </el-input>
        </el-form-item>
        <!-- <el-checkbox v-model="loginForm.rememberMe" class="re_checkout"
          >记住密码</el-checkbox
        > -->
        <el-form-item style="width: 100%">
          <el-button
            :loading="loading"
            size="large"
            type="primary"
            class="login_btn"
            @click.prevent="handleLogin"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登 录 中...</span>
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-dialog
      v-if="showEdit"
      v-model="showEdit"
      title="修改密码"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      width="30%"
      @close="closeRepass"
    >
      <div class="repassBox">
        <div class="msgText">
          <span>由于长时间未登录，账号信息已失效，请您修改密码重新登录</span>
        </div>
        <el-form
          :model="formData"
          label-width="120px"
          ref="ruleFormRef"
          :rules="rePassRules"
        >
          <el-form-item label="旧密码" prop="oldpss">
            <el-input
              type="password"
              placeholder="请输入旧密码"
              v-model="formData.oldpss"
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newpass">
            <el-input
              type="password"
              placeholder="密码长度最少为8位，包含大小写字母、数字、特殊字符"
              v-model="formData.newpass"
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="rePass">
            <el-input
              type="password"
              placeholder="密码长度最少为8位，包含大小写字母、数字、特殊字符"
              v-model="formData.rePass"
            />
          </el-form-item>
          <el-form-item>
            <el-button @click="closeRepass"> 取消 </el-button>
            <el-button type="primary" @click="submitForm(ruleFormRef)"
              >确认</el-button
            >
          </el-form-item>
        </el-form>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { getCodeImg, getInfo } from "@/api/login";
import { updateUserPwd } from "@/api/system/user";
import { ElMessage } from "element-plus";
import Cookies from "js-cookie";
import { encrypt, decrypt } from "@/utils/jsencrypt";
import useUserStore from "@/store/modules/user";
import { sm3, sm2 } from "sm-crypto";

const userStore = useUserStore();
const route = useRoute();
const router = useRouter();
const { proxy } = getCurrentInstance();
const useOld = ref(false); //是否用的旧密码
const oldUseName = ref(); //保存记录的用户名称
const oldUsePass = ref(); //保存记录的用户密码
const loginForm = ref({
  username: "",
  password: "",
  rememberMe: false,
  code: "",
  uuid: "",
});
const ruleFormRef = ref();
const formData = ref({ oldpss: "", newpass: "", rePass: "" });

const repassRule = (rule, value, callback) => {
  if (value === "") {
    callback(new Error("请确认密码"));
  } else if (value !== formData.value.newpass) {
    callback(new Error("请与新密码保持一致"));
  } else {
    callback();
  }
};
const newPassRule = (rule, value, callback) => {
  var pPattern = new RegExp(
    /^.*(?=.{8,})(?=.*\d)(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%^&*?,.?/|!@#$%^&*()~ ]).*$/
  );
  if (value === "") {
    callback(new Error("请输入密码"));
  } else if (!pPattern.test(value)) {
    callback(new Error("密码长度最少为8位，包含大小写字母、数字、特殊字符"));
  } else {
    callback();
  }
};
const loginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }],
};

const rePassRules = {
  oldpss: [{ required: true, trigger: "blur", message: "请输入旧密码" }],
  newpass: [{ required: true, validator: newPassRule, trigger: "change" }],
  rePass: [{ required: true, validator: repassRule, trigger: "change" }],
};

const codeUrl = ref("");
const loading = ref(false);
// 验证码开关
const captchaEnabled = ref(false);
// 注册开关
const register = ref(false);
const redirect = ref(undefined);

//显示强制修改密码
const showEdit = ref(false);

watch(
  route,
  (newRoute) => {
    redirect.value = newRoute.query && newRoute.query.redirect;
  },
  { immediate: true }
);

async function handleLogin() {
  proxy.$refs.loginRef.validate((valid) => {
    const usePass = JSON.parse(JSON.stringify(loginForm.value.password));
    if (valid) {
      // console.log('loginvalid',valid);
      loading.value = true;
      // console.log("pass",loginForm.value
      // console.log("sm3",sm3('123456'));
      // 勾选了需要记住密码设置在 cookie 中设置记住用户名和密码
      // Cookies.set("pass", sm3(loginForm.value.password), {
      //   expires: 30,
      // });
      if (loginForm.value.rememberMe) {
        Cookies.set("username", loginForm.value.username, { expires: 30 });
        Cookies.set("password", sm3(usePass), {
          expires: 30,
        });
        Cookies.set("rememberMe", loginForm.value.rememberMe, { expires: 30 });
      } else {
        // 否则移除
        Cookies.remove("username");
        Cookies.remove("password");
        Cookies.remove("rememberMe");
      }
      // 调用action的登录方法
      // const query = loginForm.value;
      const query = {
        username: loginForm.value.username,
        password: JSON.parse(JSON.stringify(loginForm.value.password)),
        rememberMe: loginForm.value.rememberMe,
        code: loginForm.value.code,
        uuid: loginForm.value.uuid,
      };
      if (
        oldUseName.value != loginForm.value.username ||
        oldUsePass.value != query.password
      ) {
        //如果登录的和记录的用户不是同一个, 或者同一个用户但是重新输入了密码，那就需要sm3加密
        Cookies.set("pass", sm3(usePass), {
          expires: 30,
        });
        query.password = sm3(query.password);
      }
      userStore
        .login(query)
        .then(async (res) => {
          const infoRes = await getInfo();
          // console.log("infoRes", infoRes);
          // console.log('loginres',res);
          if (infoRes.repass) {
            // Cookies.remove('Admin-Token')
            //需要该密
            showEdit.value = true;
            loading.value = false;
            // userStore.logOut();
          } else {
            // console.log('redirect',redirect.value);
            // router.push({ path: redirect.value || "/home" });
            router.push({ path: "/home" });
          }
        })
        .catch(() => {
          loading.value = false;
          // 重新获取验证码
          if (captchaEnabled.value) {
            getCode();
          }
        });
    }
  });
}

function getCode() {
  getCodeImg().then((res) => {
    captchaEnabled.value =
      res.captchaEnabled === undefined ? true : res.captchaEnabled;
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img;
      loginForm.value.uuid = res.uuid;
    }
  });
}

function getCookie() {
  const username = Cookies.get("username");
  // oldUseName.value = username;
  // const password = Cookies.get("password");
  // oldUsePass.value = password;
  // const rememberMe = Cookies.get("rememberMe");
  loginForm.value = {
    username: username === undefined ? loginForm.value.username : username,
    // password: password === undefined ? loginForm.value.password : password,
    // rememberMe: rememberMe === undefined ? false : Boolean(rememberMe),
  };
}
const closeRepass = () => {
  // userStore.logOut();
  formData.value = { oldpss: "", newpass: "", rePass: "" };
  showEdit.value = false;
};

const submitForm = async (formEl) => {
  // const passwordCookies = Cookies.get("pass");
  // const oldPass = decrypt(passwordCookies);
  // if (formData.value.oldpss != oldPass) {
  //   userStore.logOut();
  //   return ElMessage({ message: "旧密码输入错误", type: "error" });
  // }
  if (!formEl) return;
  // console.log("formEl", formEl);
  await formEl.validate((valid) => {
    if (valid) {
      updateUserPwd(
        sm3(formData.value.oldpss),
        sm3(formData.value.newpass)
      ).then((response) => {
        console.log("response", response);
        ElMessage({ message: "修改成功,请重新登录", type: "success" });
        formData.value = { oldpss: "", newpass: "", rePass: "" };
        showEdit.value = false;
      });
    } else {
      ElMessage({ message: "请输入符合规则的密码", type: "error" });
      // userStore.logOut();
      return false;
    }
  });
};

getCode();
// getCookie();
</script>

<style lang="scss" scoped>
.login {
  display: flex;
  // justify-content: center;
  // align-items: center;
  width: 100vw;
  height: 100%;
  background: #88b3f3;
}

.login_box {
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  .center_box {
    width: 500px;
    text-align: center;
    border-radius: 20px;
    padding: 30px;
    // height: 400px;
    // margin-top: 180px;
    // margin-left: 200px;
    background: #fff;
    .title {
      // color: #081f4c;
      display: inline-block;
      font-size: 24px;
      font-weight: bold;
      line-height: 32px;
      font-family: Inter-Bold;
      margin-bottom: 40px;
    }
    .ipt_title {
      color: #081f4c;
      display: inline-block;
      margin-bottom: 4px;
      font-size: 16px;
      line-height: 24px;
      font-weight: bold;
      font-family: Inter-SemiBold;
    }
    .re_checkout {
      color: #505870;
      font-size: 14px;
      margin: 0px 0px 16px 0px;
    }
    .login_btn {
      margin-top: 20px;
      width: 100%;
      height: 40px;
      background-color: #2e6bc8;
      border-radius: 4px;
    }
  }
}
.repassBox {
  width: 100%;
  // border: 1px solid #000;
}
.msgText {
  width: 100%;
  display: flex;
  justify-content: center;
  color: #dd2025;
  font-size: 13px;
  font-weight: 400;
  line-height: 22px;
  margin-bottom: 16px;
}
// 输入框样式
::v-deep .el-input__wrapper {
  background-color: #f5f6f7 !important;
  border-radius: 4px;
  box-shadow: inset 0px 2px 4px 0px rgba(188, 188, 188, 0.2);
}
::v-deep .el-checkbox__inner {
  border-color: #2e6bc8;
}
::v-deep .is-checked .el-checkbox__inner {
  background-color: #2e6bc8;
}
::v-deep .is-checked .el-checkbox__label {
  color: #505870;
}
</style>
