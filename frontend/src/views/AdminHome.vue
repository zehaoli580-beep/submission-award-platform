<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminStats, pendingPool, reviewingPool, acceptedPool, reviewerList,
  passInitial, rejectInitial, assignReviewers, manuscriptOpinions, finalReview,
  awardList, createAward, startAwardReview, nominate, awardNominations, publishAward
} from '../api'
import MediaPreview from '../components/MediaPreview.vue'
import { STATUS_CN, RECOMMENDATION_CN, RESULT_CN, AWARD_STATUS_CN, AWARD_STATUS_TYPE } from '../utils/format'

const activeTab = ref('pending')

/* ================= 顶部仪表盘 ================= */
const stats = ref(null)
const statsLoading = ref(false)

const statCards = computed(() => {
  const s = stats.value || {}
  return [
    { label: '待初审', value: s.pendingCount, icon: '📥', color: '#909399', desc: '需要您初审的稿件' },
    { label: '外审中', value: s.reviewingCount, icon: '🔍', color: '#e6a23c', desc: '审稿人评审进行中' },
    { label: '已录用', value: s.acceptedCount, icon: '✅', color: '#67c23a', desc: '可提名进入评奖' },
    { label: '退修改', value: s.reviseCount, icon: '✏️', color: '#f56c6c', desc: '等待作者重投' },
    { label: '作者 / 审稿人', value: `${s.authorCount} / ${s.reviewerCount}`, icon: '👥', color: '#409eff', desc: '平台注册用户' },
    { label: '奖项 (已公示)', value: `${s.awardCount} (${s.publishedAwardCount})`, icon: '🏆', color: '#b88230', desc: '评奖批次进展' }
  ]
})

async function loadStats() {
  statsLoading.value = true
  try {
    stats.value = await adminStats()
  } finally {
    statsLoading.value = false
  }
}

/* ================= Tab1 待初审 ================= */
const pending = ref([])
const pendingLoading = ref(false)

async function loadPending() {
  pendingLoading.value = true
  try {
    pending.value = await pendingPool()
  } finally {
    pendingLoading.value = false
  }
}

async function doRejectInitial(row) {
  try {
    await ElMessageBox.confirm(`确定退稿《${row.title}》吗? 该操作不可撤销`, '初审退稿', { type: 'warning' })
  } catch {
    return // 用户点了取消
  }
  await rejectInitial(row.id)
  ElMessage.success('已退稿')
  refreshAll()
}

/* 初审通过并分配审稿人弹窗 */
const reviewers = ref([])
const assignDialog = ref(false)
const assignForm = reactive({ manuscriptId: null, title: '', reviewerIds: [] })

async function openAssign(row) {
  if (!reviewers.value.length) {
    reviewers.value = await reviewerList()
  }
  assignForm.manuscriptId = row.id
  assignForm.title = row.title
  assignForm.reviewerIds = []
  assignDialog.value = true
}

async function doPassInitial() {
  if (!assignForm.reviewerIds.length) {
    ElMessage.warning('请至少选择一名审稿人')
    return
  }
  await passInitial(assignForm.manuscriptId, assignForm.reviewerIds)
  ElMessage.success('初审通过, 已分配审稿人')
  assignDialog.value = false
  refreshAll()
}

/* ================= Tab2 外审终审 ================= */
const reviewing = ref([])
const reviewingLoading = ref(false)

async function loadReviewing() {
  reviewingLoading.value = true
  try {
    reviewing.value = await reviewingPool()
  } finally {
    reviewingLoading.value = false
  }
}

/* 查看意见抽屉 */
const opinionsDrawer = ref(false)
const opinions = ref([])
const opinionsTitle = ref('')

async function showOpinions(row) {
  opinionsTitle.value = row.title
  opinions.value = await manuscriptOpinions(row.id)
  opinionsDrawer.value = true
}

/* 终审弹窗 */
const finalDialog = ref(false)
const finalForm = reactive({ manuscriptId: null, title: '', decision: 'ACCEPTED' })

function openFinal(row) {
  finalForm.manuscriptId = row.id
  finalForm.title = row.title
  finalForm.decision = 'ACCEPTED'
  finalDialog.value = true
}

async function doFinal() {
  await finalReview(finalForm.manuscriptId, finalForm.decision)
  ElMessage.success('终审决议已生效')
  finalDialog.value = false
  refreshAll()
}

/* 追加审稿人(复用 assign 弹窗语义, 走 /assign 接口) */
const addDialog = ref(false)
async function openAdd(row) {
  if (!reviewers.value.length) {
    reviewers.value = await reviewerList()
  }
  assignForm.manuscriptId = row.id
  assignForm.title = row.title
  assignForm.reviewerIds = []
  addDialog.value = true
}

async function doAdd() {
  if (!assignForm.reviewerIds.length) {
    ElMessage.warning('请至少选择一名审稿人')
    return
  }
  await assignReviewers(assignForm.manuscriptId, assignForm.reviewerIds)
  ElMessage.success('已追加审稿人')
  addDialog.value = false
}

/* ================= Tab3 奖项管理 ================= */
const awards = ref([])
const awardsLoading = ref(false)

async function loadAwards() {
  awardsLoading.value = true
  try {
    awards.value = await awardList()
  } finally {
    awardsLoading.value = false
  }
}

const createDialog = ref(false)
const createForm = reactive({ name: '', description: '' })

async function doCreateAward() {
  if (!createForm.name.trim()) {
    ElMessage.warning('请填写奖项名称')
    return
  }
  await createAward({ ...createForm })
  ElMessage.success('奖项已创建(筹备中)')
  createDialog.value = false
  createForm.name = ''
  createForm.description = ''
  loadAwards()
}

async function doStart(row) {
  try {
    await ElMessageBox.confirm(`开启《${row.name}》评审后即可提名作品与评委打分`, '开启评审', { type: 'info' })
  } catch {
    return
  }
  await startAwardReview(row.id)
  ElMessage.success('已开启评审')
  loadAwards()
}

/* 提名弹窗 */
const nominateDialog = ref(false)
const accepted = ref([])
const nominateForm = reactive({ awardId: null, name: '', manuscriptId: null })

async function openNominate(row) {
  accepted.value = await acceptedPool()
  nominateForm.awardId = row.id
  nominateForm.name = row.name
  nominateForm.manuscriptId = null
  nominateDialog.value = true
}

async function doNominate() {
  if (!nominateForm.manuscriptId) {
    ElMessage.warning('请选择要提名的已录用稿件')
    return
  }
  await nominate(nominateForm.awardId, nominateForm.manuscriptId)
  ElMessage.success('提名成功')
  nominateDialog.value = false
}

/* 公示弹窗 */
const publishDialog = ref(false)
const publishForm = reactive({ awardId: null, name: '', winnerCount: 1 })

function openPublish(row) {
  publishForm.awardId = row.id
  publishForm.name = row.name
  publishForm.winnerCount = 1
  publishDialog.value = true
}

async function doPublish() {
  await publishAward(publishForm.awardId, publishForm.winnerCount)
  ElMessage.success('已公示并生成获奖名单')
  publishDialog.value = false
  refreshAll()
}

/* 提名列表抽屉 */
const nomsDrawer = ref(false)
const nominations = ref([])
const nomsTitle = ref('')

async function showNoms(row) {
  nomsTitle.value = row.name
  nominations.value = await awardNominations(row.id)
  nomsDrawer.value = true
}

onMounted(() => {
  loadStats()
  loadPending()
  loadReviewing()
  loadAwards()
})

/** 各类操作后刷新仪表盘数字, 保持卡片与列表一致 */
function refreshAll() {
  loadStats()
  loadPending()
  loadReviewing()
  loadAwards()
}
</script>

<template>
  <!-- 顶部数据仪表盘 -->
  <div class="stats-row" v-loading="statsLoading">
    <div v-for="c in statCards" :key="c.label" class="stat-card">
      <span class="stat-icon">{{ c.icon }}</span>
      <div class="stat-body">
        <div class="stat-value" :style="{ color: c.color }">{{ c.value ?? 0 }}</div>
        <div class="stat-label">{{ c.label }}</div>
        <div class="stat-desc">{{ c.desc }}</div>
      </div>
    </div>
  </div>

  <el-tabs v-model="activeTab">
    <!-- Tab1: 待初审 -->
    <el-tab-pane label="待初审稿件" name="pending">
      <el-table :data="pending" v-loading="pendingLoading" empty-text="暂无待初审稿件">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="90" />
        <el-table-column prop="description" label="简介" min-width="160" show-overflow-tooltip />
        <el-table-column label="作品" width="90">
          <template #default="{ row }">
            <MediaPreview :file-url="row.fileUrl" :file-name="row.fileName" :file-size="row.fileSize" />
          </template>
        </el-table-column>
        <el-table-column label="投稿时间" width="165">
          <template #default="{ row }">{{ row.submitTime?.replace('T', ' ') }}</template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openAssign(row)">通过并分配</el-button>
            <el-button type="danger" size="small" plain @click="doRejectInitial(row)">退稿</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-tab-pane>

    <!-- Tab2: 外审中/终审 -->
    <el-tab-pane label="外审与终审" name="reviewing">
      <el-table :data="reviewing" v-loading="reviewingLoading" empty-text="暂无外审中稿件">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag type="warning" size="small">{{ STATUS_CN[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showOpinions(row)">查看意见</el-button>
            <el-button size="small" @click="openAdd(row)">追加审稿人</el-button>
            <el-button type="primary" size="small" @click="openFinal(row)">终审</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-tab-pane>

    <!-- Tab3: 奖项管理 -->
    <el-tab-pane label="奖项管理" name="awards">
      <div class="award-toolbar">
        <el-button type="primary" @click="createDialog = true">+ 创建奖项批次</el-button>
        <el-button @click="loadAwards">刷新</el-button>
      </div>
      <el-table :data="awards" v-loading="awardsLoading" empty-text="还没有奖项批次">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="奖项名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="AWARD_STATUS_TYPE[row.status]" size="small">{{ AWARD_STATUS_CN[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="公示时间" width="165">
          <template #default="{ row }">{{ row.publishTime?.replace('T', ' ') || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PREPARING'" type="primary" size="small" @click="doStart(row)">开启评审</el-button>
            <el-button v-if="row.status === 'REVIEWING'" size="small" @click="openNominate(row)">提名作品</el-button>
            <el-button v-if="row.status === 'REVIEWING'" type="success" size="small" @click="openPublish(row)">公示定奖</el-button>
            <el-button size="small" @click="showNoms(row)">查看提名</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-tab-pane>
  </el-tabs>

  <!-- 初审通过并分配 -->
  <el-dialog v-model="assignDialog" :title="`初审通过《${assignForm.title}》`" width="460px">
    <p class="dialog-tip">初审通过后稿件进入外审, 请选择审稿人(可多选):</p>
    <el-select v-model="assignForm.reviewerIds" multiple placeholder="选择审稿人" style="width: 100%">
      <el-option v-for="r in reviewers" :key="r.id" :label="`${r.nickname} (${r.username})`" :value="r.id" />
    </el-select>
    <template #footer>
      <el-button @click="assignDialog = false">取消</el-button>
      <el-button type="primary" @click="doPassInitial">通过并分配</el-button>
    </template>
  </el-dialog>

  <!-- 追加审稿人 -->
  <el-dialog v-model="addDialog" :title="`给《${assignForm.title}》追加审稿人`" width="460px">
    <el-select v-model="assignForm.reviewerIds" multiple placeholder="选择审稿人" style="width: 100%">
      <el-option v-for="r in reviewers" :key="r.id" :label="`${r.nickname} (${r.username})`" :value="r.id" />
    </el-select>
    <template #footer>
      <el-button @click="addDialog = false">取消</el-button>
      <el-button type="primary" @click="doAdd">追加分配</el-button>
    </template>
  </el-dialog>

  <!-- 终审 -->
  <el-dialog v-model="finalDialog" :title="`终审《${finalForm.title}》`" width="460px">
    <el-alert type="info" :closable="false" show-icon style="margin-bottom: 12px"
      title="终审前请先查看全部审稿意见; 有审稿人未提交意见时后端会拒绝终审" />
    <el-radio-group v-model="finalForm.decision" class="final-radios">
      <el-radio-button value="ACCEPTED">录用</el-radio-button>
      <el-radio-button value="REVISE">退修改</el-radio-button>
      <el-radio-button value="REJECTED">退稿</el-radio-button>
    </el-radio-group>
    <template #footer>
      <el-button @click="finalDialog = false">取消</el-button>
      <el-button type="primary" @click="doFinal">确认决议</el-button>
    </template>
  </el-dialog>

  <!-- 意见抽屉 -->
  <el-drawer v-model="opinionsDrawer" :title="`《${opinionsTitle}》收到的审稿意见`" size="520px">
    <el-empty v-if="!opinions.length" description="还没有收到任何意见" />
    <el-card v-for="o in opinions" :key="o.id" shadow="never" class="opinion-card">
      <div class="opinion-head">
        <b>{{ o.reviewerNickname }}</b>
        <el-tag size="small" :type="o.recommendation === 'ACCEPT' ? 'success' : o.recommendation === 'REVISE' ? 'warning' : 'danger'">
          {{ RECOMMENDATION_CN[o.recommendation] }}
        </el-tag>
        <span class="score">{{ o.score }} 分</span>
      </div>
      <div class="opinion-comment">{{ o.comment || '(未填写文字意见)' }}</div>
      <div class="opinion-time">{{ o.createTime?.replace('T', ' ') }}</div>
    </el-card>
  </el-drawer>

  <!-- 创建奖项 -->
  <el-dialog v-model="createDialog" title="创建奖项批次" width="480px">
    <el-form label-position="top">
      <el-form-item label="奖项名称" required>
        <el-input v-model="createForm.name" placeholder="如: 2026 数字媒体设计大赛" maxlength="100" />
      </el-form-item>
      <el-form-item label="奖项说明">
        <el-input v-model="createForm.description" type="textarea" :rows="3" maxlength="500" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="createDialog = false">取消</el-button>
      <el-button type="primary" @click="doCreateAward">创建</el-button>
    </template>
  </el-dialog>

  <!-- 提名 -->
  <el-dialog v-model="nominateDialog" :title="`向《${nominateForm.name}》提名作品`" width="480px">
    <p class="dialog-tip">只能提名状态为"已录用"的稿件:</p>
    <el-select v-model="nominateForm.manuscriptId" placeholder="选择已录用稿件" style="width: 100%">
      <el-option v-for="m in accepted" :key="m.id" :label="`#${m.id} ${m.title} (${m.category})`" :value="m.id" />
    </el-select>
    <template #footer>
      <el-button @click="nominateDialog = false">取消</el-button>
      <el-button type="primary" @click="doNominate">提名</el-button>
    </template>
  </el-dialog>

  <!-- 公示 -->
  <el-dialog v-model="publishDialog" :title="`公示《${publishForm.name}》`" width="480px">
    <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 12px"
      title="公示将立即计算终评分(评委≥3人时去掉最高最低取均值)并排名定奖, 有提名未打分会被拒绝" />
    <el-form label-position="top">
      <el-form-item label="获奖名额数">
        <el-input-number v-model="publishForm.winnerCount" :min="1" :max="10" style="width: 160px" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="publishDialog = false">取消</el-button>
      <el-button type="success" @click="doPublish">公示并定奖</el-button>
    </template>
  </el-dialog>

  <!-- 提名列表抽屉 -->
  <el-drawer v-model="nomsDrawer" :title="`《${nomsTitle}》提名情况`" size="560px">
    <el-table :data="nominations" empty-text="暂无提名">
      <el-table-column prop="manuscriptTitle" label="作品" min-width="150" show-overflow-tooltip />
      <el-table-column prop="authorNickname" label="作者" width="100" />
      <el-table-column prop="finalScore" label="终评分" width="90">
        <template #default="{ row }">{{ row.finalScore ?? '-' }}</template>
      </el-table-column>
      <el-table-column label="结果" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.result !== 'NOMINATED'" :type="row.result === 'WON' ? 'success' : 'info'" size="small">
            {{ RESULT_CN[row.result] }}
          </el-tag>
          <el-tag v-else type="warning" size="small">{{ RESULT_CN.NOMINATED }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
  </el-drawer>
</template>

<style scoped>
/* ===== 仪表盘卡片 ===== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin-bottom: 18px;
}

@media (max-width: 1100px) {
  .stats-row {
    grid-template-columns: repeat(3, 1fr);
  }
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 14px 14px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: transform 0.15s, box-shadow 0.15s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  font-size: 26px;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.1;
}

.stat-label {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
}

.stat-desc {
  font-size: 11px;
  color: #c0c4cc;
}

.file-link {
  color: #409eff;
  text-decoration: none;
  font-size: 13px;
}

.award-toolbar {
  margin-bottom: 14px;
}

.dialog-tip {
  font-size: 13px;
  color: #909399;
  margin-bottom: 10px;
}

.final-radios {
  width: 100%;
}

.final-radios :deep(.el-radio-button) {
  flex: 1;
}

.final-radios :deep(.el-radio-button__inner) {
  width: 100%;
}

.opinion-card {
  margin-bottom: 12px;
}

.opinion-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.score {
  color: #e6a23c;
  font-weight: 600;
  margin-left: auto;
}

.opinion-comment {
  font-size: 14px;
  color: #303133;
  margin-top: 6px;
}

.opinion-time {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 6px;
}
</style>
