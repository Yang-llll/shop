<template>
  <div class="register-page">
    <div class="register-card">
      <div class="register-header">
        <h2>创建账号</h2>
        <p>加入腊制品商铺，品味正宗腊味</p>
      </div>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="0"
        size="large"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请确认密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入11位手机号"
            prefix-icon="Phone"
          />
        </el-form-item>
        <el-form-item prop="captcha">
          <div class="captcha-row">
            <el-input
              v-model="form.captcha"
              placeholder="请输入验证码"
              prefix-icon="Key"
              class="captcha-input"
            />
            <div class="captcha-img" @click="refreshCaptcha">
              <canvas ref="captchaCanvas" width="120" height="40"></canvas>
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="submitting"
            class="register-btn"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login" class="link">去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "../stores/user";
import { ElMessage } from "element-plus";

const router = useRouter();
const userStore = useUserStore();

const formRef = ref(null);
const submitting = ref(false);

const form = reactive({
  username: "",
  password: "",
  confirmPassword: "",
  phone: "",
  captcha: "",
});

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error("两次输入的密码不一致"));
  } else {
    callback();
  }
};

const validateCaptcha = (rule, value, callback) => {
  if (!value) {
    callback(new Error("请输入验证码"));
  } else if (value.toLowerCase() !== captchaCode.value.toLowerCase()) {
    callback(new Error("验证码不正确"));
  } else {
    callback();
  }
};

const rules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 2, max: 20, message: "用户名长度为 2-20 个字符", trigger: "blur" },
    {
      pattern: /^[a-zA-Z0-9\u4e00-\u9fa5_]+$/,
      message: "用户名只能包含字母、数字、中文和下划线",
      trigger: "blur",
    },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, max: 20, message: "密码长度为 6-20 个字符", trigger: "blur" },
    {
      pattern: /^(?=.*[a-zA-Z])(?=.*\d).+$/,
      message: "密码必须包含字母和数字",
      trigger: "blur",
    },
  ],
  confirmPassword: [
    { required: true, message: "请确认密码", trigger: "blur" },
    { validator: validateConfirmPassword, trigger: "blur" },
  ],

  phone: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: "请输入正确的手机号",
      trigger: "blur",
    },
  ],
  captcha: [
    { required: true, message: "请输入验证码", trigger: "blur" },
    { validator: validateCaptcha, trigger: "blur" },
  ],
};

// 图形验证码
const captchaCanvas = ref(null);
const captchaCode = ref("");

function generateCaptcha() {
  const canvas = captchaCanvas.value;
  if (!canvas) return;
  const ctx = canvas.getContext("2d");
  ctx.clearRect(0, 0, 120, 40);
  // 背景
  ctx.fillStyle = "#f0f0f0";
  ctx.fillRect(0, 0, 120, 40);
  // 生成随机字符串
  const chars = "ABCDEFGHJKMNPQRSTWXYZabcdefhjkmnprstwxyz2345678";
  let code = "";
  for (let i = 0; i < 4; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  captchaCode.value = code;
  // 绘制文字
  const colors = ["#E64A19", "#FF8F00", "#4CAF50", "#2196F3", "#9C27B0"];
  for (let i = 0; i < code.length; i++) {
    ctx.font = Math.random() * 6 + 18 + "px bold Arial";
    ctx.fillStyle = colors[Math.floor(Math.random() * colors.length)];
    ctx.save();
    ctx.translate(18 + i * 25, 28);
    ctx.rotate((Math.random() - 0.5) * 0.4);
    ctx.fillText(code[i], 0, 0);
    ctx.restore();
  }
  // 干扰线
  for (let i = 0; i < 4; i++) {
    ctx.strokeStyle = "rgba(0,0,0,0.15)";
    ctx.beginPath();
    ctx.moveTo(Math.random() * 120, Math.random() * 40);
    ctx.lineTo(Math.random() * 120, Math.random() * 40);
    ctx.stroke();
  }
  // 干扰点
  for (let i = 0; i < 30; i++) {
    ctx.fillStyle = "rgba(0,0,0,0.1)";
    ctx.beginPath();
    ctx.arc(Math.random() * 120, Math.random() * 40, 1, 0, Math.PI * 2);
    ctx.fill();
  }
}

function refreshCaptcha() {
  generateCaptcha();
}

onMounted(() => {
  nextTick(() => generateCaptcha());
});

const handleRegister = async () => {
  // 手动校验验证码
  if (!form.captcha) {
    ElMessage.warning("请输入验证码");
    return;
  }
  if (form.captcha.toLowerCase() !== captchaCode.value.toLowerCase()) {
    ElMessage.warning("验证码不正确");
    refreshCaptcha();
    form.captcha = "";
    return;
  }
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;

  submitting.value = true;
  try {
    const success = await userStore.register({
      username: form.username,
      password: form.password,
      phone: form.phone,
    });
    if (success) {
      ElMessage.success("注册成功，请登录");
      router.push("/login");
    } else {
      refreshCaptcha();
      form.captcha = "";
    }
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.register-page {
  min-height: calc(100vh - 64px - 200px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.register-card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 16px;
  padding: 40px 36px 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.register-header h2 {
  font-size: 1.6em;
  font-weight: 700;
  color: #e64a19;
  margin-bottom: 6px;
}

.register-header p {
  color: #999;
  font-size: 14px;
}

.register-btn {
  width: 100%;
  border-radius: 8px;
  font-size: 16px;
  letter-spacing: 4px;
}

.register-footer {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #999;
}

.register-footer .link {
  color: #e64a19;
  font-weight: 500;
}

.register-footer .link:hover {
  text-decoration: underline;
}
.captcha-row {
  display: flex;
  gap: 10px;
  width: 100%;
}
.captcha-input {
  flex: 1;
}
.captcha-img {
  cursor: pointer;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  height: 40px;
  flex-shrink: 0;
}
.captcha-img canvas {
  display: block;
}
</style>
