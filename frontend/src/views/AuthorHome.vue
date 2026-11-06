<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { myManuscripts, submitManuscript, manuscriptLogs, reviseManuscript } from '../api'
import MediaPreview from '../components/MediaPreview.vue'
import { STATUS_CN, STATUS_TYPE } from '../utils/format'

const loading = ref(false)
const list = ref([])
const fileInput = ref(null)
let pickedFile = null

const CATEGORIES = ['短视频', '平面设计', '动画', '摄影', '交互设计', '数字音效']
const ALLOWED_EXT = ['mp4', 'mov', 'avi', 'png', 'jpg', 'jpeg', 'gif', 'zip', 'rar', 'pdf']

const form = reactive({ title: '', category: '', description: '' })

/* 我的稿件概览条 */
const mySummary = computed(() => {
  const cnt = (s) => list.value.filter((m) => m.status === s).length
  return [
    { label: '全部', value: list.value.length, color: '#409eff' },
    { label: '待初审', value: cnt('PENDING_INITIAL'), color: '#909399' },
    { label: '外审中', value: cnt('UNDER_REVIEW'), color: '#e6a23c' },
    { label: '已录用', value: cnt('ACCEPTED'), color: '#67c23a' },
    { label: '退修改', value: cnt('REVISE'), color: '#f56c6c' }
  ]
})

async function load() {
  loading.value = true
  try {
    list.value = await myManuscripts()
  } finally {
    loading.value = false
  }
}
onMounted(load)

/** 手动管理文件选择(不走 el-upload 自动上传, 由提交按钮统一发 FormData) */
function onPickFile(e) {
  const f = e.target.files?.[0]
  if (!f) return
  const ext = f.name.split('.').pop().toLowerCase()
  if (!ALLOWED_EXT.includes(ext)) {
    ElMessage.error(`不支持的文件类型: ${ext}`)
    e.target.value = ''
    return
  }
  pickedFile = f
}

async function doSubmit() {
  if (!form.title || !form.category) {
    ElMessage.warning('请填写标题并选择分类')
    return
  }
  if (!pickedFile) {
    ElMessage.warning('请选择作品文件')
    return
  }
  const fd = new FormData()
  fd.append('title', form.title)
  fd.append('category', form.category)
  fd.append('description', form.description)
  fd.append('file', pickedFile)
  try {
    await submitManuscript(fd)
    ElMessage.success('投稿成功, 等待编辑部初审')
    form.title = ''
    form.description = ''
    pickedFile = null
    if (fileInput.value) fileInput.value.value = ''
    load()
  } catch {
    /* 拦截器已弹错误 */
  }
}

/* ============ 流转记录抽屉 ============ */
const logsDrawer = ref(false)
const logs = ref([])
const logsLoading = ref(false)
const currentTitle = ref('')

async function showLogs(row) {
  currentTitle.value = row.title
  logsDrawer.value = true
  logsLoading.value = true
  try {
    logs.value = await manuscriptLogs(row.id)
  } finally {
    logsLoading.value = false
  }
}

/* ============ 修改重投 ============ */
const reviseDrawer = ref(false)
const reviseForm = reactive({ description: '', file: null })
let reviseFilePicked = null
let reviseRow = null
const reviseFileInput = ref(null)

function openRevise(row) {
  reviseRow = row
  reviseForm.description = row.description || ''
  reviseFilePicked = null
  reviseDrawer.value = true
}

function onReviseFile(e) {
  const f = e.target.files?.[0]
  if (!f) return
  const ext = f.name.split('.').pop().toLowerCase()
  if (!ALLOWED_EXT.includes(ext)) {
    ElMessage.error(`不支持的文件类型: ${ext}`)
    e.target.value = ''
    return
  }
  reviseFilePicked = f
}

async function doRevise() {
  if (!reviseForm.description.trim() && !reviseFilePicked) {
    ElMessage.warning('请至少修改简介或更换作品文件')
    return
  }
  const fd = new FormData()
  fd.append('description', reviseForm.description)
  if (reviseFilePicked) fd.append('file', reviseFilePicked)
  try {
    await reviseManuscript(reviseRow.id, fd)
    ElMessage.success('已重新送审')
    reviseDrawer.value = false
    load()
  } catch {
    /* 拦截器已弹错误 */
  }
}
</script>

<template>
  <!-- 概览条 -->
  <div class="summary-bar">
    <div v-for="s in mySummary" :key="s.label" class="summary-item">
      <span class="summary-value" :style="{ color: s.color }">{{ s.value }}</span>
      <span class="summary-label">{{ s.label }}</span>
    </div>
  </div>

  <el-row :gutter="20">
    <!-- 左侧: 投稿表单 -->
    <el-col :span="9">
      <el-card shadow="never">
        <template #header><b>提交新作品</b></template>
        <el-form label-position="top">
          <el-form-item label="作品标题" required>
            <el-input v-model="form.title" placeholder="请输入作品标题" maxlength="100" />
          </el-form-item>
          <el-form-item label="作品分类" required>
            <el-select v-model="form.category" placeholder="选择分类" style="width: 100%">
              <el-option v-for="c in CATEGORIES" :key="c" :label="c" :value="c" />
            </el-select>
          </el-form-item>
          <el-form-item label="作品简介">
            <el-input v-model="form.description" type="textarea" :rows="3" maxlength="500" show-word-limit />
          </el-form-item>
          <el-form-item label="作品文件" required>
            <input ref="fileInput" type="file" style="width: 100%" accept=".mp4,.mov,.avi,.png,.jpg,.jpeg,.gif,.zip,.rar,.pdf" @change="onPickFile" />
            <div class="file-tip">支持 mp4/mov/avi/png/jpg/gif/zip/rar/pdf, 上限 200MB</div>
          </el-form-item>
          <el-button type="primary" style="width: 100%" @click="doSubmit">提 交 作 品</el-button>
        </el-form>
      </el-card>
    </el-col>

    <!-- 右侧: 我的稿件 -->
    <el-col :span="15">
      <el-card shadow="never">
        <template #header>
          <div class="card-head">
            <b>我的稿件</b>
            <el-button link type="primary" @click="load">刷新</el-button>
          </div>
        </template>
        <el-table :data="list" v-loading="loading" empty-text="还没有投稿记录">
          <el-table-column prop="title" label="标题" min-width="140" show-overflow-tooltip />
          <el-table-column prop="category" label="分类" width="90" />
          <el-table-column label="作品" width="90">
            <template #default="{ row }">
              <MediaPreview :file-url="row.fileUrl" :file-name="row.fileName" :file-size="row.fileSize" />
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="STATUS_TYPE[row.status]" effect="light">{{ STATUS_CN[row.status] }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="showLogs(row)">流转记录</el-button>
              <el-button v-if="row.status === 'REVISE'" link type="warning" size="small" @click="openRevise(row)">修改重投</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="还没有投稿记录, 从左侧开始提交第一份作品吧" :image-size="80" />
          </template>
        </el-table>
      </el-card>
    </el-col>
  </el-row>

  <!-- 流转记录抽屉 -->
  <el-drawer v-model="logsDrawer" :title="`《${currentTitle}》流转记录`" size="420px">
    <el-timeline v-loading="logsLoading" style="padding-left: 8px">
      <el-timeline-item
        v-for="log in logs"
        :key="log.id"
        :timestamp="log.createTime?.replace('T', ' ')"
        :type="log.toStatus === 'ACCEPTED' ? 'success' : log.toStatus === 'REJECTED' ? 'danger' : 'primary'"
      >
        <b>{{ log.action }}</b>
        <div class="log-detail">{{ log.detail }}</div>
        <div class="log-status" v-if="log.fromStatus">
          {{ STATUS_CN[log.fromStatus] }} → {{ STATUS_CN[log.toStatus] }}
        </div>
      </el-timeline-item>
    </el-timeline>
  </el-drawer>

  <!-- 修改重投抽屉 -->
  <el-drawer v-model="reviseDrawer" :title="`《${reviseRow?.title}》修改重投`" size="420px">
    <el-form label-position="top">
      <el-form-item label="作品简介">
        <el-input v-model="reviseForm.description" type="textarea" :rows="4" maxlength="500" show-word-limit />
      </el-form-item>
      <el-form-item label="更换作品文件(可选)">
        <input ref="reviseFileInput" type="file" style="width: 100%" accept=".mp4,.mov,.avi,.png,.jpg,.jpeg,.gif,.zip,.rar,.pdf" @change="onReviseFile" />
        <div class="file-tip">重新提交后将再次进入外审流程</div>
      </el-form-item>
      <el-button type="warning" style="width: 100%" @click="doRevise">重新送审</el-button>
    </el-form>
  </el-drawer>
</template>

<style scoped>
/* ===== 概览条 ===== */
.summary-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.summary-item {
  flex: 1;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 10px 0;
  text-align: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.summary-value {
  font-size: 22px;
  font-weight: 700;
  display: block;
  line-height: 1.2;
}

.summary-label {
  font-size: 12px;
  color: #909399;
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.file-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.file-link {
  color: #409eff;
  text-decoration: none;
}

.log-detail {
  font-size: 13px;
  color: #606266;
  margin-top: 2px;
}

.log-status {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
</style>
