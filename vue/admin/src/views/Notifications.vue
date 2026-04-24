<template>
  <div class="notifications-page">
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="通知内容">
          <el-input v-model="filterForm.content" placeholder="请输入关键词" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="notificationList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="content" label="通知内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="user.username" label="接收者" width="100" />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.is_read ? 'success' : 'warning'">
              {{ row.is_read ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="发送时间" width="180" />
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleView(row)">查看</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="通知详情" width="600px">
      <el-descriptions :column="1" border v-if="currentNotification">
        <el-descriptions-item label="通知ID">{{ currentNotification.id }}</el-descriptions-item>
        <el-descriptions-item label="接收者">{{ currentNotification.user?.username }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="getTypeTag(currentNotification.type)">{{ getTypeName(currentNotification.type) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentNotification.is_read ? 'success' : 'warning'">
            {{ currentNotification.is_read ? '已读' : '未读' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发送时间">{{ currentNotification.created_at }}</el-descriptions-item>
        <el-descriptions-item label="通知内容">{{ currentNotification.content }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { notificationAPI } from '../api'

const loading = ref(false)
const notificationList = ref([])
const detailVisible = ref(false)
const currentNotification = ref(null)

const filterForm = reactive({
  content: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const typeMap = {
  1: { name: '评论通知', tag: 'info' },
  2: { name: '回复通知', tag: 'primary' },
  3: { name: '系统通知', tag: 'warning' }
}

const getTypeName = (type) => typeMap[type]?.name || '未知'
const getTypeTag = (type) => typeMap[type]?.tag || 'info'

const loadNotifications = async () => {
  loading.value = true
  try {
    const res = await notificationAPI.list({
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    notificationList.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('Failed to load notifications:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadNotifications()
}

const handleReset = () => {
  filterForm.content = ''
  handleSearch()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  loadNotifications()
}

const handleCurrentChange = (val) => {
  pagination.page = val
  loadNotifications()
}

const handleView = (row) => {
  currentNotification.value = row
  detailVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这条通知吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await notificationAPI.delete(row.id)
      ElMessage.success('删除成功')
      loadNotifications()
    } catch (error) {
      console.error('Failed to delete notification:', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  loadNotifications()
})
</script>

<style scoped>
.notifications-page {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  background: white;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>