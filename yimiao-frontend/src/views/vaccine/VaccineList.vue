<template>
  <div class="page-container">
    <n-card title="疫苗列表">
      <template #header-extra>
        <n-input
          v-model:value="searchName"
          placeholder="搜索疫苗名称"
          style="width: 200px"
          @keyup.enter="loadVaccines"
        />
      </template>
      
      <n-grid :cols="3" :x-gap="20" :y-gap="20">
        <n-gi v-for="vaccine in vaccines" :key="vaccine.id">
          <n-card hoverable @click="goDetail(vaccine.id)">
            <h3>{{ vaccine.name }}</h3>
            <p class="vaccine-info">
              <n-tag size="small" :type="vaccine.isFree ? 'success' : 'warning'">
                {{ vaccine.isFree ? '免费' : '自费' }}
              </n-tag>
              <span v-if="!vaccine.isFree" class="price">¥{{ vaccine.price }}</span>
            </p>
            <p class="vaccine-desc">{{ vaccine.description || '暂无描述' }}</p>
            <p class="vaccine-meta">
              <span>适用年龄: {{ vaccine.minAge }}-{{ vaccine.maxAge }}岁</span>
              <span>剂次: {{ vaccine.doseCount }}针</span>
            </p>
          </n-card>
        </n-gi>
      </n-grid>
      
      <n-pagination
        v-model:page="pagination.page"
        :page-count="pagination.totalPages"
        @update:page="loadVaccines"
        style="margin-top: 20px; justify-content: center"
      />
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { vaccineApi } from '@/api/vaccine'

const router = useRouter()
const searchName = ref('')
const vaccines = ref([])
const pagination = ref({
  page: 1,
  pageSize: 9,
  totalPages: 1
})

const loadVaccines = async () => {
  const res = await vaccineApi.getPage({
    pageNum: pagination.value.page,
    pageSize: pagination.value.pageSize,
    name: searchName.value,
    status: 0
  })
  
  if (res.code === 200) {
    vaccines.value = res.data.records
    pagination.value.totalPages = res.data.pages
  }
}

const goDetail = (id) => {
  router.push(`/vaccine/${id}`)
}

onMounted(() => {
  loadVaccines()
})
</script>

<style scoped>
.vaccine-info {
  margin: 10px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.price {
  color: #f0a020;
  font-weight: bold;
}

.vaccine-desc {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.vaccine-meta {
  display: flex;
  gap: 15px;
  color: #999;
  font-size: 12px;
}
</style>
