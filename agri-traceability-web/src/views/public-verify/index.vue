<template>
  <div class="verify-page">
    <div class="verify-card">
      <div class="head">
        <div class="seal"><Link /></div>
        <h1>农产品质量安全验真</h1>
        <p>区块链可信追溯节点</p>
      </div>
      <div class="query">
        <el-input v-model="code" placeholder="请输入合格证编号" clearable />
        <el-button type="primary" @click="search">验真</el-button>
      </div>
      <div v-if="result" class="result">
        <div class="result-top">
          <span>核验结果</span>
          <el-tag :type="result.valid ? 'success' : 'danger'">{{ result.verifyMessage }}</el-tag>
        </div>
        <h2>{{ result.productName || '未查询到产品' }}</h2>
        <p class="code">{{ result.certificateCode }}</p>
        <div class="info-row"><span>生产主体</span><strong>{{ result.producerName }}</strong></div>
        <div class="info-row"><span>产品类别</span><strong>{{ result.productCategory }}</strong></div>
        <div class="info-row"><span>产地地址</span><strong>{{ result.originAddress }}</strong></div>
        <div class="info-row"><span>采收时间</span><strong>{{ result.harvestTime }}</strong></div>
        <div class="info-row"><span>检测机构</span><strong>{{ result.testAgency }}</strong></div>
        <div class="info-row"><span>检测结论</span><strong>{{ result.conclusion }}</strong></div>
        <div class="chain-box">
          <div>交易哈希</div>
          <p>{{ result.txHash || '暂无链上交易' }}</p>
          <div>区块高度：{{ result.blockNumber || '-' }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Link } from '@element-plus/icons-vue'
import { publicVerify } from '@/api/public'

const route = useRoute()
const code = ref(route.params.code || '')
const result = ref(null)

async function search() {
  if (!code.value) {
    ElMessage.warning('请输入合格证编号')
    return
  }
  result.value = await publicVerify(code.value)
}

onMounted(() => {
  if (code.value) search()
})
</script>

<style scoped>
.verify-page {
  min-height: 100vh;
  padding: 28px 16px;
  background: linear-gradient(180deg, #e8f3ee 0%, var(--agri-bg) 45%);
}

.verify-card {
  max-width: 480px;
  margin: 0 auto;
  padding: 22px;
  background: #fff;
  border: 1px solid var(--agri-border);
  border-radius: 8px;
  box-shadow: var(--agri-shadow);
}

.head {
  text-align: center;
}

.seal {
  display: inline-flex;
  width: 54px;
  height: 54px;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: var(--agri-primary);
  border-radius: 8px;
}

h1 {
  margin: 12px 0 4px;
  font-size: 21px;
}

.head p,
.code {
  margin: 0;
  color: var(--agri-text-secondary);
}

.query {
  display: flex;
  gap: 8px;
  margin: 22px 0;
}

.result-top,
.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 11px 0;
  border-bottom: 1px solid var(--agri-border);
}

h2 {
  margin: 12px 0 4px;
  text-align: center;
}

.code {
  text-align: center;
}

.info-row span {
  color: var(--agri-text-secondary);
}

.info-row strong {
  text-align: right;
}

.chain-box {
  margin-top: 14px;
  padding: 12px;
  overflow-wrap: anywhere;
  background: var(--agri-bg);
  border-radius: 8px;
  font-size: 13px;
}

.chain-box p {
  margin: 6px 0;
  color: var(--agri-primary);
  font-family: Consolas, Monaco, monospace;
}
</style>
