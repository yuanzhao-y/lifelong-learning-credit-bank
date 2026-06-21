<template>
  <div class="file-upload-container">
    <el-upload
      class="upload-demo"
      drag
      action="#"
      :http-request="handleUpload"
      :before-upload="beforeUpload"
      :on-remove="handleRemove"
      :file-list="fileList"
      multiple
    >
      <el-icon class="el-icon--upload"><upload-filled /></el-icon>
      <div class="el-upload__text">
        将文件拖到此处，或<em>点击上传</em>
      </div>
      <template #tip>
        <div class="el-upload__tip">
          支持任意格式的证明文件，大小不超过 10MB
        </div>
      </template>
    </el-upload>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { uploadFile } from '@/api/file'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  bizType?: string
  bizId?: number
}>()

const emit = defineEmits<{
  (e: 'change', fileIds: number[]): void
}>()

interface UploadFileItem {
  name: string
  url?: string
  uid: number
  id?: number
}

const fileList = ref<UploadFileItem[]>([])
const fileIds = ref<number[]>([])

const beforeUpload = (file: any) => {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  return true
}

const handleUpload = async (options: any) => {
  const { file } = options
  try {
    const res: any = await uploadFile(file, props.bizType || 'common', props.bizId)
    const fileId = res.id
    fileIds.value.push(fileId)
    
    // Add to fileList
    const idx = fileList.value.findIndex(item => item.uid === file.uid)
    if (idx !== -1) {
      fileList.value[idx].id = fileId
    } else {
      fileList.value.push({
        name: file.name,
        uid: file.uid,
        id: fileId
      })
    }
    
    emit('change', fileIds.value)
    ElMessage.success('上传成功')
  } catch (error: any) {
    ElMessage.error(error.message || '上传失败')
  }
}

const handleRemove = (file: any) => {
  const idx = fileList.value.findIndex(item => item.uid === file.uid)
  if (idx !== -1) {
    const deletedItem = fileList.value[idx]
    if (deletedItem.id) {
      fileIds.value = fileIds.value.filter(id => id !== deletedItem.id)
    }
    fileList.value.splice(idx, 1)
    emit('change', fileIds.value)
  }
}
</script>

<style scoped>
.file-upload-container :deep(.el-upload-dragger) {
  background: var(--bg-surface) !important;
  border: 1px dashed var(--border) !important;
  border-radius: var(--radius-md);
  transition: var(--transition);
}
.file-upload-container :deep(.el-upload-dragger:hover) {
  border-color: var(--accent) !important;
}
.file-upload-container :deep(.el-upload__text) {
  color: var(--text-secondary);
}
.file-upload-container :deep(.el-upload__text em) {
  color: var(--accent);
}
.file-upload-container :deep(.el-upload__tip) {
  color: var(--text-muted);
}
.file-upload-container :deep(.el-upload-list__item) {
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  color: var(--text-primary);
}
.file-upload-container :deep(.el-upload-list__item:hover) {
  background: rgba(255, 255, 255, 0.04);
}
.file-upload-container :deep(.el-upload-list__item .el-icon--close) {
  color: var(--text-secondary);
}
</style>
