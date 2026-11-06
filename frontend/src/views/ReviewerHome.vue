<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { myReviewTasks, submitOpinion, awardList, awardNominations, myAwardScores, scoreNomination } from '../api'
import MediaPreview from '../components/MediaPreview.vue'
import { STATUS_CN, STATUS_TYPE, RECOMMENDATION_CN, RESULT_CN, AWARD_STATUS_CN, AWARD_STATUS_TYPE } from '../utils/format'

const activeTab = ref('tasks')
const tasks = ref([])
const tasksLoading = ref(false)

async function loadTasks() {
  tasksLoading.value = true
  try {
    tasks.value = await myReviewTasks()
  } finally {
    tasksLoading.value = false
  }
}
onMounted(loadTasks)

/* ============ 提交评审意见 ============ */
const opinionDrawer = ref(false)
const opinionForm = reactive({ score: 80, comment: '', recommendation: 'ACCEPT' })
let currentTask = null

function openOpinion(row) {
  currentTask = row
  opinionForm.score = 80
  opinionForm.comment = ''
  opinionForm.recommendation = 'ACCEPT'
  opinionDrawer.value = true
}

async function doSubmitOpinion() {
  try {
    await submitOpinion(currentTask.assignmentId, { ...opinionForm })
    ElMessage.success('意见已提交')
    opinionDrawer.value = false
    loadTasks()
  } catch {
    /* 拦截器已弹错误 */
  }
}

/* ============ 奖项评分(评委) ============ */
const awards = ref([])
const chosenAwardId = ref(null)
const nominations = ref([])
const myScores = ref({})
const scoreLoading = ref(false)

async function loadAwards() {
  awards.value = await awardList()
  const reviewing = awards.value.filter((a) => a.status === 'REVIEWING')
  if (reviewing.length && !chosenAwardId.value) {
    chosenAwardId.value = reviewing[0].id
    loadNominations()
  }
}

async function loadNominations() {
  if (!chosenAwardId.value) {
    nominations.value = []
    return
  }
  scoreLoading.value = true
  try {
    nominations.value = await awardNominations(chosenAwardId.value)
    myScores.value = await myAwardScores(chosenAwardId.value)
  } finally {
    scoreLoading.value = false
  }
}
onMounted(loadAwards)

/** 评委对某提名打分(输入即保存) */
async function doScore(nom) {
  const val = myScores.value[nom.id]
  if (val === null || val === undefined || val === '') {
    ElMessage.warning('请先输入分数')
    return
  }
  await scoreNomination(chosenAwardId.value, nom.id, Number(val))
  ElMessage.success(`《${nom.manuscriptTitle}》打分已保存: ${val} 分`)
}
</script>

<template>
  <el-tabs v-model="activeTab">
    <!-- Tab 1: 评审任务 -->
    <el-tab-pane label="我的评审任务" name="tasks">
      <el-table :data="tasks" v-loading="tasksLoading" empty-text="暂无分配给您的稿件">
        <el-table-column prop="title" label="作品标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="90" />
        <el-table-column label="作品文件" width="120">
          <template #default="{ row }">
            <div class="file-cell">
              <MediaPreview :file-url="row.fileUrl" :file-name="row.title" :file-size="row.fileSize" />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="简介" min-width="150" show-overflow-tooltip />
        <el-table-column label="稿件状态" width="90">
          <template #default="{ row }">
            <el-tag :type="STATUS_TYPE[row.manuscriptStatus]" size="small">{{ STATUS_CN[row.manuscriptStatus] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="我的任务" width="90">
          <template #default="{ row }">
            <el-tag :type="row.assignStatus === 'ASSIGNED' ? 'warning' : 'success'" size="small">
              {{ row.assignStatus === 'ASSIGNED' ? '待评审' : '已提交' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.assignStatus === 'ASSIGNED' && row.manuscriptStatus === 'UNDER_REVIEW'"
              type="primary" size="small" @click="openOpinion(row)"
            >提交意见</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-tab-pane>

    <!-- Tab 2: 奖项评分 -->
    <el-tab-pane label="奖项评分(评委)" name="awards">
      <div class="award-bar">
        <el-select v-model="chosenAwardId" placeholder="选择评审中的奖项" style="width: 320px" @change="loadNominations">
          <el-option
            v-for="a in awards.filter((x) => x.status === 'REVIEWING')"
            :key="a.id"
            :label="a.name"
            :value="a.id"
          />
        </el-select>
        <span class="award-tip">仅"评审中"的奖项需要打分; 打分在公示前可反复修改</span>
      </div>
      <el-table :data="nominations" v-loading="scoreLoading" empty-text="该奖项暂无提名作品">
        <el-table-column prop="manuscriptTitle" label="作品" min-width="160" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="90" />
        <el-table-column prop="authorNickname" label="作者" width="110" />
        <el-table-column label="我的打分 (0-100)" width="200">
          <template #default="{ row }">
            <div class="score-cell">
              <el-input-number v-model="myScores[row.id]" :min="0" :max="100" size="small" controls-position="right" style="width: 110px" />
              <el-button type="primary" size="small" @click="doScore(row)">保存</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-tab-pane>
  </el-tabs>

  <!-- 提交意见抽屉 -->
  <el-drawer v-model="opinionDrawer" :title="`评审《${currentTask?.title}》`" size="420px">
    <el-form label-position="top">
      <el-form-item label="评分 (0-100)">
        <el-input-number v-model="opinionForm.score" :min="0" :max="100" style="width: 100%" />
      </el-form-item>
      <el-form-item label="推荐结论">
        <el-radio-group v-model="opinionForm.recommendation">
          <el-radio value="ACCEPT">{{ RECOMMENDATION_CN.ACCEPT }}</el-radio>
          <el-radio value="REVISE">{{ RECOMMENDATION_CN.REVISE }}</el-radio>
          <el-radio value="REJECT">{{ RECOMMENDATION_CN.REJECT }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="评审意见">
        <el-input v-model="opinionForm.comment" type="textarea" :rows="5" maxlength="1000" show-word-limit
          placeholder="从主题表达、技术完成度、艺术性等维度给出具体意见" />
      </el-form-item>
      <el-alert type="info" :closable="false" show-icon class="once-tip"
        title="意见提交后不可修改(一人一票), 请确认后再提交" />
      <el-button type="primary" style="width: 100%; margin-top: 12px" @click="doSubmitOpinion">
        提 交 评 审 意 见
      </el-button>
    </el-form>
  </el-drawer>
</template>

<style scoped>
.file-link {
  color: #409eff;
  text-decoration: none;
  font-size: 13px;
}

.award-bar {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 14px;
}

.award-tip {
  font-size: 12px;
  color: #909399;
}

.score-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.once-tip {
  margin-top: 4px;
}
</style>
