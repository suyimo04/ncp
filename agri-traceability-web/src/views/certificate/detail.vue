<template>
  <div class="detail-page" v-loading="loading">
    <AppCard title="合格证详情">
      <template #extra>
        <el-button @click="$router.back()">返回</el-button>
      </template>
      <div v-if="detail" class="cert-layout">
        <div class="cert-paper">
          <h2>农产品承诺达标合格证</h2>
          <p class="code">{{ cert.certificateCode || '待签发' }}</p>
          <div class="cert-row"><span>产品名称</span><strong>{{ batch.productName }}</strong></div>
          <div class="cert-row"><span>经营主体</span><strong>{{ cert.producerName }}</strong></div>
          <div class="cert-row"><span>产地</span><strong>{{ batch.originAddress }}</strong></div>
          <div class="cert-row"><span>检测结论</span><strong>{{ report.conclusion }}</strong></div>
          <div class="cert-row"><span>签发时间</span><strong>{{ cert.issueTime || '-' }}</strong></div>
          <div class="cert-row"><span>有效期至</span><strong>{{ cert.expireTime || '-' }}</strong></div>
        </div>
        <div class="side">
          <QrcodeVue v-if="cert.qrCodeUrl" :value="cert.qrCodeUrl" :size="180" />
          <el-empty v-else description="签发后生成二维码" />
          <div class="hash-text full">{{ cert.certificateHash || '证书哈希待生成' }}</div>
          <el-button v-if="cert.qrCodeUrl" type="primary" @click="openVerify">打开公众验真</el-button>
        </div>
      </div>
    </AppCard>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import QrcodeVue from 'qrcode.vue'
import AppCard from '@/components/common/AppCard.vue'
import { certificateApi } from '@/api/certificate'

const route = useRoute()
const loading = ref(false)
const detail = ref(null)
const cert = computed(() => detail.value?.certificate || {})
const batch = computed(() => detail.value?.batch || {})
const report = computed(() => detail.value?.report || {})

async function loadData() {
  loading.value = true
  try {
    detail.value = await certificateApi.detail(route.params.id)
  } finally {
    loading.value = false
  }
}

function openVerify() {
  window.open(cert.value.qrCodeUrl, '_blank')
}

onMounted(loadData)
</script>

<style scoped>
.cert-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 260px;
  gap: 20px;
}

.cert-paper {
  min-height: 420px;
  padding: 34px;
  border: 2px solid var(--agri-primary);
  border-radius: 8px;
  background: #fff;
}

h2 {
  margin: 0;
  color: var(--agri-primary);
  text-align: center;
  font-size: 30px;
}

.code {
  margin: 10px 0 26px;
  color: var(--agri-text-secondary);
  text-align: center;
}

.cert-row {
  display: flex;
  padding: 14px 0;
  border-bottom: 1px solid var(--agri-border);
}

.cert-row span {
  width: 110px;
  color: var(--agri-text-secondary);
}

.side {
  display: flex;
  align-items: center;
  flex-direction: column;
  gap: 14px;
}

.full {
  max-width: 240px;
  white-space: normal;
  word-break: break-all;
}
</style>
