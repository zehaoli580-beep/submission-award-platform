/** 稿件状态 -> 中文名(与后端 ManuscriptStatus.CN_NAMES 对应) */
export const STATUS_CN = {
  PENDING_INITIAL: '待初审',
  UNDER_REVIEW: '外审中',
  REVISE: '退修改',
  ACCEPTED: '已录用',
  REJECTED: '已退稿'
}

/** 状态 -> 标签颜色(Element Plus tag type) */
export const STATUS_TYPE = {
  PENDING_INITIAL: 'info',
  UNDER_REVIEW: 'warning',
  REVISE: 'danger',
  ACCEPTED: 'success',
  REJECTED: 'danger'
}

/** 角色中文名 */
export const ROLE_CN = {
  AUTHOR: '作者',
  REVIEWER: '审稿人',
  ADMIN: '管理员'
}

/** 字节数 -> 可读文件大小 */
export function formatSize(bytes) {
  if (!bytes && bytes !== 0) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(2) + ' MB'
}

/** 提名/评审结果中文名 */
export const RESULT_CN = {
  NOMINATED: '提名中',
  WON: '🏆 获奖',
  LOST: '落选'
}

/** 奖项批次状态 */
export const AWARD_STATUS_CN = {
  PREPARING: '筹备中',
  REVIEWING: '评审中',
  PUBLISHED: '已公示',
  ARCHIVED: '已归档'
}

export const AWARD_STATUS_TYPE = {
  PREPARING: 'info',
  REVIEWING: 'warning',
  PUBLISHED: 'success',
  ARCHIVED: 'info'
}

/** 审稿推荐结论 */
export const RECOMMENDATION_CN = {
  ACCEPT: '推荐录用',
  REVISE: '退修改',
  REJECT: '退稿'
}
