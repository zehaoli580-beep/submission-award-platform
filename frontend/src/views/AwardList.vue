<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { awardList, awardNominations } from '../api'
import MediaPreview from '../components/MediaPreview.vue'
import { AWARD_STATUS_CN, AWARD_STATUS_TYPE, RESULT_CN } from '../utils/format'

/**
 * 获奖公示页(所有登录角色可见):
 * - 已公示(PUBLISHED)的奖项: 获奖作品展示领奖台卡片 + 完整排名(终评分进度条)
 * - 未公示的奖项只显示状态, 提名详情不开放(后端同样拦截)
 */
const awards = ref([])
const loading = ref(false)
const expanded = ref({}) // awardId -> nominations[]

async function load() {
  loading.value = true
  try {
    awards.value = await awardList()
    // 默认展开第一个已公示奖项
    const first = awards.value.find((a) => a.status === 'PUBLISHED')
    if (first) toggleExpand(first)
  } finally {
    loading.value = false
  }
}
onMounted(load)

async function toggleExpand(award) {
  if (award.status !== 'PUBLISHED') {
    ElMessage.info('该奖项尚未公示, 提名与得分暂不公开')
    return
  }
  if (expanded.value[award.id]) {
    delete expanded.value[award.id]
    expanded.value = { ...expanded.value }
    return
  }
  const noms = await awardNominations(award.id)
  expanded.value[award.id] = noms
}

const MEDAL = ['🥇', '🥈', '🥉']

/** 已公示奖项展开时: 获奖作品(WON)用于领奖台, 其余为落选名单 */
function podiumOf(awardId) {
  const noms = expanded.value[awardId] || []
  return noms.filter((n) => n.result === 'WON')
}

function losersOf(awardId) {
  const noms = expanded.value[awardId] || []
  return noms.filter((n) => n.result !== 'WON')
}

/** 全部提名按名次展示(前3有奖牌) */
function rankedOf(awardId) {
  const noms = expanded.value[awardId] || []
  return [...noms].sort((a, b) => (b.finalScore ?? 0) - (a.finalScore ?? 0))
}
</script>

<template>
  <div v-loading="loading">
    <el-empty v-if="!awards.length" description="还没有奖项批次" />

    <el-card v-for="a in awards" :key="a.id" shadow="never" class="award-card">
      <div class="award-head" @click="toggleExpand(a)">
        <div>
          <span class="award-name">{{ a.name }}</span>
          <el-tag :type="AWARD_STATUS_TYPE[a.status]" size="small" class="award-tag">{{ AWARD_STATUS_CN[a.status] }}</el-tag>
        </div>
        <div class="award-desc">{{ a.description || '—' }}</div>
        <div class="publish-time" v-if="a.publishTime">公示于 {{ a.publishTime.replace('T', ' ') }}</div>
      </div>

      <!-- 已公示: 领奖台 + 完整排名 -->
      <div v-if="a.status === 'PUBLISHED' && expanded[a.id]">
        <!-- 领奖台: 获奖作品大卡片 -->
        <div v-if="podiumOf(a.id).length" class="podium">
          <div v-for="(n, i) in podiumOf(a.id)" :key="n.id" class="podium-card" :class="`rank-${i + 1}`">
            <div class="podium-medal">{{ MEDAL[i] || '🏅' }}</div>
            <div class="podium-title">{{ n.manuscriptTitle }}</div>
            <div class="podium-meta">{{ n.category }} · {{ n.authorNickname }}</div>
            <div class="podium-preview">
              <MediaPreview :file-url="n.fileUrl" :file-name="n.manuscriptTitle" :file-size="n.fileSize" />
            </div>
            <div class="podium-score">{{ n.finalScore }}<span class="unit">分</span></div>
            <el-progress :percentage="Math.round(n.finalScore)" :stroke-width="10"
              :color="i === 0 ? '#f7b500' : i === 1 ? '#b0c4de' : '#cd7f32'" :show-text="false" />
          </div>
        </div>

        <!-- 完整排名表(含落选) -->
        <div class="rank-list">
          <div class="rank-list-title">完整排名</div>
          <div v-for="(n, i) in rankedOf(a.id)" :key="n.id" class="rank-item" :class="{ won: n.result === 'WON' }">
            <span class="rank-no">{{ MEDAL[i] || `${i + 1}` }}</span>
            <span class="rank-title">{{ n.manuscriptTitle }}</span>
            <span class="rank-meta">{{ n.category }} · {{ n.authorNickname }}</span>
            <span class="rank-score">{{ n.finalScore }} 分</span>
            <el-tag v-if="n.result === 'WON'" type="success" size="small">{{ RESULT_CN.WON }}</el-tag>
            <el-tag v-else type="info" size="small">{{ RESULT_CN.LOST }}</el-tag>
          </div>
        </div>
      </div>
      <div v-else-if="a.status === 'PUBLISHED'" class="rank-tip">点击上方查看获奖名单与完整排名</div>
    </el-card>
  </div>
</template>

<style scoped>
.award-card {
  margin-bottom: 16px;
  cursor: pointer;
}

.award-head {
  display: flex;
  align-items: baseline;
  gap: 12px;
  flex-wrap: wrap;
}

.award-name {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
}

.award-tag {
  margin-left: 8px;
}

.award-desc {
  font-size: 13px;
  color: #909399;
  flex: 1;
}

.publish-time {
  font-size: 12px;
  color: #c0c4cc;
}

.rank-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #c0c4cc;
  text-align: center;
}

/* ===== 领奖台 ===== */
.podium {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 14px;
  margin-top: 16px;
}

.podium-card {
  border-radius: 14px;
  padding: 18px 18px 14px;
  text-align: center;
  border: 1px solid #ebeef5;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.06);
}

.podium-card.rank-1 {
  background: linear-gradient(160deg, #fff8e1 0%, #ffecb3 100%);
  border-color: #f7d774;
}

.podium-card.rank-2 {
  background: linear-gradient(160deg, #f5f7fa 0%, #e8edf3 100%);
  border-color: #c5d2e0;
}

.podium-card.rank-3 {
  background: linear-gradient(160deg, #fdf1e7 0%, #f8dfc8 100%);
  border-color: #e0b48a;
}

.podium-medal {
  font-size: 40px;
  line-height: 1;
  margin-bottom: 6px;
}

.podium-title {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.podium-meta {
  font-size: 12px;
  color: #909399;
  margin: 4px 0 8px;
}

.podium-preview {
  margin-bottom: 8px;
  min-height: 24px;
}

.podium-score {
  font-size: 26px;
  font-weight: 800;
  color: #303133;
  margin-bottom: 6px;
}

.podium-score .unit {
  font-size: 13px;
  font-weight: 400;
  color: #909399;
  margin-left: 2px;
}

/* ===== 完整排名 ===== */
.rank-list {
  margin-top: 18px;
  border-top: 1px dashed #e4e7ed;
  padding-top: 10px;
}

.rank-list-title {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 4px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 6px;
  border-bottom: 1px solid #f2f3f5;
}

.rank-item.won {
  background: #f0f9eb;
  border-radius: 6px;
  padding-left: 10px;
}

.rank-no {
  width: 30px;
  text-align: center;
  font-size: 16px;
  font-weight: 700;
  color: #909399;
}

.rank-title {
  font-size: 15px;
  color: #303133;
  font-weight: 500;
}

.rank-meta {
  font-size: 13px;
  color: #909399;
  flex: 1;
}

.rank-score {
  font-size: 14px;
  font-weight: 600;
  color: #e6a23c;
}
</style>
