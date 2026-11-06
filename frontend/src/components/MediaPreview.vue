<script setup>
/**
 * 作品文件预览组件:
 * - 图片(png/jpg/jpeg/gif) -> 表格内缩略图, 点击大图查看(el-image 自带预览)
 * - 视频(mp4/mov/webm)     -> 表格内封面按钮, 点击弹出在线播放对话框
 * - 其他(zip/rar/pdf等)    -> 文件图标 + 下载链接
 * 文件类型由 fileUrl 的扩展名推断(后端存储名固定为 uuid.ext)
 */
import { computed, ref } from 'vue'
import { VideoPlay, Document, Picture } from '@element-plus/icons-vue'
import { formatSize } from '../utils/format'

const props = defineProps({
  fileUrl: { type: String, required: true },
  fileName: { type: String, default: '' },
  fileSize: { type: Number, default: null }
})

const ext = computed(() => (props.fileUrl || '').split('.').pop().toLowerCase())
const isImage = computed(() => ['png', 'jpg', 'jpeg', 'gif', 'webp'].includes(ext.value))
const isVideo = computed(() => ['mp4', 'mov', 'webm'].includes(ext.value))

const videoVisible = ref(false)
</script>

<template>
  <span class="media-preview">
    <!-- 图片: 缩略图 + 点击看大图 -->
    <el-image
      v-if="isImage"
      :src="fileUrl"
      :preview-src-list="[fileUrl]"
      fit="cover"
      preview-teleported
      class="thumb"
    />

    <!-- 视频: 播放按钮, 弹窗在线播放 -->
    <template v-else-if="isVideo">
      <el-button type="primary" size="small" plain @click="videoVisible = true">
        <el-icon class="btn-icon"><VideoPlay /></el-icon>播放
      </el-button>
      <el-dialog v-model="videoVisible" :title="fileName || '作品在线预览'" width="720px" destroy-on-close append-to-body>
        <video :src="fileUrl" controls autoplay class="player" />
        <div class="video-tip">若浏览器不支持该编码格式, 可下载后本地播放</div>
      </el-dialog>
    </template>

    <!-- 其他: 下载链接 -->
    <a v-else :href="fileUrl" target="_blank" class="file-link">
      <el-icon class="btn-icon"><Document /></el-icon>
      {{ formatSize(fileSize) }}
    </a>
  </span>
</template>

<style scoped>
.media-preview {
  display: inline-flex;
  align-items: center;
}

.thumb {
  width: 46px;
  height: 46px;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
  cursor: zoom-in;
}

.btn-icon {
  margin-right: 3px;
}

.file-link {
  color: #409eff;
  text-decoration: none;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
}

.player {
  width: 100%;
  max-height: 420px;
  border-radius: 8px;
  background: #000;
}

.video-tip {
  font-size: 12px;
  color: #909399;
  text-align: center;
  margin-top: 8px;
}
</style>
