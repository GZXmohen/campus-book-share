<template>
  <div class="posts-page">
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="书名">
          <el-input v-model="filterForm.title" placeholder="请输入书名" clearable />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="filterForm.author" placeholder="请输入作者" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="postList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="书名" min-width="150" />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="contact_wx" label="联系微信" width="150" />
        <el-table-column label="交易模式" width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.is_sell && row.is_rent">出售/出租</span>
            <span v-else-if="row.is_sell">出售</span>
            <span v-else-if="row.is_rent">出租</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="发布时间" width="180" />
        <el-table-column label="操作" width="200" align="center">
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

    <el-dialog v-model="detailVisible" title="图书详情" width="700px">
      <el-descriptions :column="2" border v-if="currentPost">
        <el-descriptions-item label="书名" :span="2">{{ currentPost.title }}</el-descriptions-item>
        <el-descriptions-item label="作者">{{ currentPost.author }}</el-descriptions-item>
        <el-descriptions-item label="发布者">{{ currentPost.User?.username }}</el-descriptions-item>
        <el-descriptions-item label="交易模式" :span="2">
          <span v-if="currentPost.is_sell && currentPost.is_rent">出售/出租</span>
          <span v-else-if="currentPost.is_sell">出售</span>
          <span v-else-if="currentPost.is_rent">出租</span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="联系方式" :span="2">
          微信: {{ currentPost.contact_wx || '-' }} | QQ: {{ currentPost.contact_qq || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="简介" :span="2">{{ currentPost.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="封面图" :span="2">
          <el-image
            v-if="currentPost.cover_image"
            :src="currentPost.cover_image"
            style="width: 100px; height: 140px"
            fit="cover"
          />
          <span v-else>无</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { postAPI } from '../api'

const loading = ref(false)
const postList = ref([])
const detailVisible = ref(false)
const currentPost = ref(null)

const filterForm = reactive({
  title: '',
  author: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const loadPosts = async () => {
  loading.value = true
  try {
    const res = await postAPI.list({
      page: pagination.page,
      pageSize: pagination.pageSize,
      title: filterForm.title,
      author: filterForm.author
    })
    postList.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('Failed to load posts:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadPosts()
}

const handleReset = () => {
  filterForm.title = ''
  filterForm.author = ''
  handleSearch()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  loadPosts()
}

const handleCurrentChange = (val) => {
  pagination.page = val
  loadPosts()
}

const handleView = (row) => {
  currentPost.value = row
  detailVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除图书《${row.title}》吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await postAPI.delete(row.id)
      ElMessage.success('删除成功')
      loadPosts()
    } catch (error) {
      console.error('Failed to delete post:', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.posts-page {
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