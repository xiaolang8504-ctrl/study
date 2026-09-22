<template>
  <div class="login-page">
    <el-card class="login-card" shadow="never">
      <h1>错题系统</h1>
      <el-form ref="loginForm" :model="form" :rules="rules" label-position="top" @submit.native.prevent>
        <el-form-item label="登录账号" prop="userName">
          <el-input
            v-model="form.userName"
            placeholder="请输入登录账号"
            prefix-icon="el-icon-user"
          />
        </el-form-item>
        <el-form-item label="登录密码" prop="passWord">
          <el-input
            v-model="form.passWord"
            type="password"
            placeholder="请输入登录密码"
            prefix-icon="el-icon-lock"
            show-password
          />
        </el-form-item>
        <el-form-item label="安全验证" prop="sliderCaptchaToken">
          <div class="slider-captcha">
            <div class="captcha-canvas">
              <img v-if="sliderCaptcha.background" :src="sliderCaptcha.background" class="captcha-background" alt="滑块验证码">
              <img
                v-if="sliderCaptcha.sliderImage"
                :src="sliderCaptcha.sliderImage"
                :style="{ left: slideOffset + 'px' }"
                class="captcha-piece"
                alt="可拖动拼图块"
                draggable="false"
              >
            </div>
            <div class="slider-track" @mousedown.prevent="startSlide" @touchstart.prevent="startSlide">
              <div class="slider-progress" :style="{ width: slideOffset + 'px' }"></div>
              <div class="slider-handle" :class="{ verified: sliderVerified }" :style="{ left: slideOffset + 'px' }">
                <i :class="sliderVerified ? 'el-icon-check' : 'el-icon-d-arrow-right'"></i>
              </div>
              <span class="slider-tip">{{ sliderVerified ? '验证通过' : '拖动滑块完成验证' }}</span>
            </div>
          </div>
        </el-form-item>
        <el-button type="primary" class="login-button" :loading="loading" @click="handleLogin">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { getSliderCaptcha, login, verifySliderCaptcha } from '@/api/system/login'

export default {
  name: 'LoginPage',
  data() {
    return {
      loading: false,
      form: {
        userName: localStorage.getItem('study_username') || '',
        passWord: '',
        sliderCaptchaToken: ''
      },
      sliderCaptcha: {},
      slideOffset: 0,
      slideStartX: 0,
      slideStartOffset: 0,
      sliderVerified: false,
      sliderVerifying: false,
      rules: {
        userName: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
        passWord: [{ required: true, message: '请输入登录密码', trigger: 'blur' }],
        sliderCaptchaToken: [{ required: true, message: '请完成滑块验证', trigger: 'change' }]
      }
    }
  },
  created() {
    this.refreshSliderCaptcha()
  },
  methods: {
    refreshSliderCaptcha() {
      getSliderCaptcha().then(data => {
        this.sliderCaptcha = data
        this.form.sliderCaptchaToken = ''
        this.slideOffset = 0
        this.sliderVerified = false
        this.sliderVerifying = false
      })
    },
    startSlide(event) {
      if (this.sliderVerified || this.sliderVerifying) return
      this.slideStartX = event.touches ? event.touches[0].clientX : event.clientX
      this.slideStartOffset = this.slideOffset
      window.addEventListener('mousemove', this.moveSlide)
      window.addEventListener('mouseup', this.endSlide)
      window.addEventListener('touchmove', this.moveSlide, { passive: false })
      window.addEventListener('touchend', this.endSlide)
    },
    moveSlide(event) {
      if (event.cancelable) event.preventDefault()
      const clientX = event.touches ? event.touches[0].clientX : event.clientX
      this.slideOffset = Math.max(0, Math.min(278, this.slideStartOffset + clientX - this.slideStartX))
    },
    endSlide() {
      window.removeEventListener('mousemove', this.moveSlide)
      window.removeEventListener('mouseup', this.endSlide)
      window.removeEventListener('touchmove', this.moveSlide)
      window.removeEventListener('touchend', this.endSlide)
      if (this.slideOffset > 0) {
        this.sliderVerifying = true
        verifySliderCaptcha({
          sliderCaptchaToken: this.sliderCaptcha.sliderCaptchaToken,
          sliderCaptchaOffset: String(this.slideOffset)
        }).then(() => {
          this.sliderVerified = true
          this.form.sliderCaptchaToken = this.sliderCaptcha.sliderCaptchaToken
          this.$refs.loginForm.clearValidate('sliderCaptchaToken')
        }).catch(() => {
          this.sliderVerified = false
          this.form.sliderCaptchaToken = ''
          this.slideOffset = 0
        }).finally(() => {
          this.sliderVerifying = false
        })
      }
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (!valid) {
          return
        }
        this.loading = true
        login(this.form).then(data => {
          localStorage.setItem('study_username', this.form.userName.trim())
          localStorage.setItem('study_access_token', data.token)
          this.$message.success('登录成功')
          this.$router.replace('/')
        }).catch(() => {
          this.refreshSliderCaptcha()
        }).finally(() => {
          this.loading = false
        })
      })
    }
  }
}
</script>

<style scoped>
.slider-captcha { width: 320px; }
.captcha-canvas { position: relative; width: 320px; height: 150px; overflow: hidden; border-radius: 8px; }
.captcha-background { display: block; width: 320px; height: 150px; }
.captcha-piece { position: absolute; top: 50px; width: 36px; height: 38px; filter: drop-shadow(0 2px 2px rgba(15, 23, 42, .28)); pointer-events: none; transition: left .08s linear; }
.slider-track { position: relative; height: 42px; margin-top: 10px; overflow: hidden; border: 1px solid #dcdfe6; border-radius: 5px; background: #f5f7fa; cursor: pointer; user-select: none; }
.slider-progress { height: 100%; background: #d9ecff; transition: width .08s linear; }
.slider-handle { position: absolute; top: -1px; width: 42px; height: 42px; color: #409eff; border: 1px solid #409eff; border-radius: 5px; background: #fff; text-align: center; line-height: 42px; transition: left .08s linear; }
.slider-handle.verified { color: #67c23a; border-color: #67c23a; }
.slider-tip { position: absolute; top: 0; right: 0; left: 0; color: #909399; text-align: center; line-height: 40px; pointer-events: none; }
</style>
