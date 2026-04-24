<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card stat-users">
          <div class="stat-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">用户总数</p>
            <p class="stat-value">{{ stats.userCount }}</p>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-posts">
          <div class="stat-icon">
            <el-icon><Reading /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">图书总数</p>
            <p class="stat-value">{{ stats.postCount }}</p>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-comments">
          <div class="stat-icon">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">评论总数</p>
            <p class="stat-value">{{ stats.commentCount }}</p>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-notifications">
          <div class="stat-icon">
            <el-icon><Bell /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">通知总数</p>
            <p class="stat-value">{{ stats.notificationCount }}</p>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <div class="chart-card">
          <h3>图书发布趋势</h3>
          <div ref="postChartRef" class="chart"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card">
          <h3>交易模式分布</h3>
          <div ref="tradeChartRef" class="chart"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <div class="chart-card">
          <h3>图书列表</h3>
          <el-table
            :data="postList"
            v-loading="loading"
            style="width: 100%"
            max-height="300"
          >
            <el-table-column prop="title" label="书名" />
            <el-table-column prop="author" label="作者" width="120" />
            <el-table-column label="评论数" width="100" align="center">
              <template #default="{ row }">
                {{ row.comments_count || 0 }}
              </template>
            </el-table-column>
            <el-table-column prop="created_at" label="发布时间" width="180" />
          </el-table>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card">
          <h3>快速操作</h3>
          <div class="actions-wrapper">
            <div class="quick-actions">
              <el-button
                type="primary"
                class="action-btn"
                @click="$router.push('/posts')"
                >管理图书</el-button
              >
              <el-button
                type="primary"
                class="action-btn"
                @click="$router.push('/comments')"
                >处理评论</el-button
              >
              <el-button
                type="primary"
                class="action-btn"
                @click="$router.push('/users')"
                >管理用户</el-button
              >
              <el-button
                type="primary"
                class="action-btn"
                @click="$router.push('/notifications')"
                >管理通知</el-button
              >
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from "vue";
import * as echarts from "echarts";
import { User, Reading, ChatDotRound, Bell } from "@element-plus/icons-vue";
import { statsAPI, postAPI } from "../api";

const postChartRef = ref(null);
const tradeChartRef = ref(null);
const loading = ref(false);
let postChart = null;
let tradeChart = null;

const stats = reactive({
  userCount: 0,
  postCount: 0,
  commentCount: 0,
  notificationCount: 0,
});

const postList = ref([]);
const postTrendData = ref([]);
const tradeDistributionData = ref({ sell: 0, rent: 0, sell_rent: 0 });

const loadDashboardData = async () => {
  try {
    const res = await statsAPI.dashboard();
    stats.userCount = res.data.userCount;
    stats.postCount = res.data.postCount;
    stats.commentCount = res.data.commentCount;
    stats.notificationCount = res.data.notificationCount;
  } catch (error) {
    console.error("Failed to load dashboard data:", error);
  }
};

const loadPostTrend = async () => {
  try {
    const res = await statsAPI.postTrend();
    postTrendData.value = res.data || [];
    initPostChart();
  } catch (error) {
    console.error("Failed to load post trend:", error);
  }
};

const loadTradeDistribution = async () => {
  try {
    const res = await statsAPI.tradeDistribution();
    tradeDistributionData.value = res.data || {
      sell: 0,
      rent: 0,
      sell_rent: 0,
    };
    initTradeChart();
  } catch (error) {
    console.error("Failed to load trade distribution:", error);
  }
};

const loadPosts = async () => {
  loading.value = true;
  try {
    const res = await postAPI.list({ page: 1, pageSize: 10 });
    postList.value = res.data.list || [];
  } catch (error) {
    console.error("Failed to load posts:", error);
  } finally {
    loading.value = false;
  }
};

const initPostChart = () => {
  if (!postChartRef.value) return;
  postChart = echarts.init(postChartRef.value);

  const months = postTrendData.value.map((item) => item.Month);
  const counts = postTrendData.value.map((item) => item.Count);
  const sells = postTrendData.value.map((item) => item.Sell);
  const rents = postTrendData.value.map((item) => item.Rent);

  const option = {
    tooltip: { trigger: "axis" },
    legend: { data: ["发布量", "出售", "出租"] },
    xAxis: {
      type: "category",
      data: months.length > 0 ? months : ["暂无数据"],
    },
    yAxis: { type: "value" },
    series: [
      { name: "发布量", type: "bar", data: counts },
      { name: "出售", type: "line", data: sells },
      { name: "出租", type: "line", data: rents },
    ],
  };
  postChart.setOption(option);
};

const initTradeChart = () => {
  if (!tradeChartRef.value) return;
  tradeChart = echarts.init(tradeChartRef.value);

  const data = [
    {
      value: tradeDistributionData.value.sell || 0,
      name: "仅出售",
      itemStyle: { color: "#409EFF" },
    },
    {
      value: tradeDistributionData.value.rent || 0,
      name: "仅出租",
      itemStyle: { color: "#67C23A" },
    },
    {
      value: tradeDistributionData.value.sell_rent || 0,
      name: "可售可租",
      itemStyle: { color: "#E6A23C" },
    },
  ];

  const option = {
    tooltip: { trigger: "item" },
    legend: { orient: "horizontal", left: "right", top: "top" },
    series: [
      {
        name: "交易模式",
        type: "pie",
        radius: ["40%", "70%"],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: "#fff", borderWidth: 2 },
        label: { show: true, formatter: "{b}: {c} ({d}%)" },
        data: data,
      },
    ],
  };
  tradeChart.setOption(option);
};

const handleResize = () => {
  postChart?.resize();
  tradeChart?.resize();
};

onMounted(() => {
  loadDashboardData();
  loadPosts();
  loadPostTrend();
  loadTradeDistribution();
  window.addEventListener("resize", handleResize);
});

onUnmounted(() => {
  window.removeEventListener("resize", handleResize);
  postChart?.dispose();
  tradeChart?.dispose();
});
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  font-size: 28px;
  color: white;
  margin-right: 15px;
}

.stat-users .stat-icon {
  background: linear-gradient(135deg, #667eea, #764ba2);
}
.stat-posts .stat-icon {
  background: linear-gradient(135deg, #409eff, #66b1ff);
}
.stat-comments .stat-icon {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}
.stat-notifications .stat-icon {
  background: linear-gradient(135deg, #e6a23c, #f56c6c);
}

.stat-info {
  flex: 1;
}

.stat-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 5px;
}

.stat-value {
  color: #303133;
  font-size: 28px;
  font-weight: bold;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.chart-card h3 {
  color: #303133;
  font-size: 16px;
  margin-bottom: 15px;
}

.chart {
  width: 100%;
  height: 300px;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

:deep(.action-btn) {
  width: 100% !important;
  margin: 0 !important;
  box-sizing: border-box !important;
  color: #fff !important;
  border: none !important;
}
:deep(.action-btn:nth-child(1)) {
  background: #409eff !important; /* 蓝 */
}
:deep(.action-btn:nth-child(2)) {
  background: #67c23a !important; /* 绿 */
}
:deep(.action-btn:nth-child(3)) {
  background: #e6a23c !important; /* 橙 */
}
:deep(.action-btn:nth-child(4)) {
  background: #f56c6c !important; /* 红 */
}
</style>
