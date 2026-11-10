<template>
  <div class="category-page">
    <div class="category-header">
      <h2>腊制品商品分类</h2>
      <p>精选传统腊味，按部位、工艺、口味分类</p>
    </div>
    <div class="container">
      <!-- 按部位分类（大分类） -->
      <div class="section-block">
        <div class="section-title">
          <span class="section-icon">&#x1F437;</span>
          <h3>按部位分类</h3>
        </div>
        <div class="category-grid">
          <div
            v-for="cat in partCategories"
            :key="cat.value"
            class="category-card"
            :class="{ active: activePart === cat.value }"
            @click="selectPart(cat.value)"
          >
            <div class="category-icon">{{ cat.icon }}</div>
            <h4>{{ cat.label }}</h4>
            <p>{{ cat.desc }}</p>
          </div>
        </div>
      </div>

      <!-- 按熏制工艺分类（小分类） -->
      <div class="section-block">
        <div class="section-title">
          <span class="section-icon">&#x1F525;</span>
          <h3>按熏制工艺</h3>
        </div>
        <div class="tag-grid">
          <div
            v-for="cat in craftCategories"
            :key="cat.value"
            class="tag-card"
            :class="{ active: activeCraft === cat.value }"
            @click="selectCraft(cat.value)"
          >
            <span class="tag-icon">{{ cat.icon }}</span>
            <span class="tag-label">{{ cat.label }}</span>
            <span class="tag-desc">{{ cat.desc }}</span>
          </div>
        </div>
      </div>

      <!-- 按口味分类（小分类） -->
      <div class="section-block">
        <div class="section-title">
          <span class="section-icon">&#x1F336;</span>
          <h3>按口味分类</h3>
        </div>
        <div class="tag-grid">
          <div
            v-for="cat in flavorCategories"
            :key="cat.value"
            class="tag-card"
            :class="{ active: activeFlavor === cat.value }"
            @click="selectFlavor(cat.value)"
          >
            <span class="tag-icon">{{ cat.icon }}</span>
            <span class="tag-label">{{ cat.label }}</span>
            <span class="tag-desc">{{ cat.desc }}</span>
          </div>
        </div>
      </div>

      <!-- 当前筛选结果 -->
      <div class="result-bar" v-if="activePart || activeCraft || activeFlavor">
        <span>当前筛选：</span>
        <el-tag
          v-if="activePart"
          closable
          @close="
            activePart = '';
            searchCategory();
          "
          >{{ activePart }}</el-tag
        >
        <el-tag
          v-if="activeCraft"
          type="warning"
          closable
          @close="
            activeCraft = '';
            searchCategory();
          "
          >{{ activeCraft }}</el-tag
        >
        <el-tag
          v-if="activeFlavor"
          type="danger"
          closable
          @close="
            activeFlavor = '';
            searchCategory();
          "
          >{{ activeFlavor }}</el-tag
        >
        <el-button type="info" link @click="clearAll">清除全部</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

const activePart = ref("");
const activeCraft = ref("");
const activeFlavor = ref("");

const partCategories = [
  {
    label: "五花腊肉",
    value: "五花腊肉",
    icon: "\u{1F953}",
    desc: "肥瘦相间，经典之选",
  },
  {
    label: "后腿腊肉",
    value: "后腿腊肉",
    icon: "\u{1F356}",
    desc: "瘦肉紧实，口感醇厚",
  },
  {
    label: "腊排骨",
    value: "腊排骨",
    icon: "\u{1F969}",
    desc: "肉香浓郁，炖汤极鲜",
  },
  {
    label: "腊猪头",
    value: "腊猪头",
    icon: "\u{1F437}",
    desc: "胶质丰富，宴席硬菜",
  },
  { label: "腊猪脚", value: "腊猪脚", icon: "\u{1F43E}", desc: "配酸萝卜绝配" },
  {
    label: "腊肠",
    value: "腊肠",
    icon: "\u{1F32D}",
    desc: "手工灌制，多种风味",
  },
  {
    label: "其他腊制品",
    value: "其他腊制品",
    icon: "\u{1F356}",
    desc: "多样传统腊味，风味独特",
  },
];

const craftCategories = [
  {
    label: "柴火熏制",
    value: "柴火熏制",
    icon: "\u{1F525}",
    desc: "杂木柴火慢熏，烟熏味浓",
  },
  {
    label: "柏枝熏制",
    value: "柏枝熏制",
    icon: "\u{1F332}",
    desc: "柏树枝熏，香气清幽",
  },
  {
    label: "茶壳熏制",
    value: "茶壳熏制",
    icon: "\u{1F375}",
    desc: "油茶壳熏，淡淡茶香",
  },
  {
    label: "果木熏制",
    value: "果木熏制",
    icon: "\u{1F333}",
    desc: "果木柴火熏制，独特风味",
  },
];

const flavorCategories = [
  {
    label: "原味腌制",
    value: "原味腌制",
    icon: "\u{1F9C2}",
    desc: "纯盐腌制，烟熏本味",
  },
  {
    label: "花椒味",
    value: "花椒味",
    icon: "\u{1F336}",
    desc: "花椒提香，麻香扑鼻",
  },
  {
    label: "辣椒味",
    value: "辣椒味",
    icon: "\u{1F336}",
    desc: "干辣椒腌制，麻辣诱人",
  },
  {
    label: "柏香味",
    value: "柏香味",
    icon: "\u{1F332}",
    desc: "柏叶清香，清新自然",
  },
  {
    label: "酱香味",
    value: "酱香味",
    icon: "\u{1F963}",
    desc: "豆瓣酱腌制，酱香厚重",
  },
  {
    label: "广式甜味",
    value: "广式甜味",
    icon: "\u{1F36F}",
    desc: "酒香微甜，广式风味",
  },
];

function selectPart(val) {
  activePart.value = activePart.value === val ? "" : val;
  searchCategory();
}

function selectCraft(val) {
  activeCraft.value = activeCraft.value === val ? "" : val;
  searchCategory();
}

function selectFlavor(val) {
  activeFlavor.value = activeFlavor.value === val ? "" : val;
  searchCategory();
}

function clearAll() {
  activePart.value = "";
  activeCraft.value = "";
  activeFlavor.value = "";
  router.push({ path: "/" });
}

function searchCategory() {
  const params = {};
  if (activePart.value) params.category = activePart.value;
  if (activeCraft.value) params.craft = activeCraft.value;
  if (activeFlavor.value) params.flavor = activeFlavor.value;
  router.push({ path: "/", query: params });
}
</script>

<style scoped>
.category-page {
  padding: 0 0 40px;
}
.category-header {
  background: linear-gradient(135deg, #e64a19, #ff8f00);
  padding: 40px 20px;
  text-align: center;
  color: #fff;
}
.category-header h2 {
  font-size: 28px;
  margin-bottom: 8px;
}
.category-header p {
  font-size: 14px;
  opacity: 0.9;
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.section-block {
  margin-bottom: 32px;
}
.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 2px solid #e64a19;
}
.section-icon {
  font-size: 24px;
}
.section-title h3 {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

/* 大分类卡片 */
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}
.category-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 2px solid transparent;
}
.category-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(230, 74, 25, 0.12);
  border-color: #ffe0cc;
}
.category-card.active {
  border-color: #e64a19;
  background: #fff5f0;
}
.category-icon {
  font-size: 40px;
  margin-bottom: 10px;
}
.category-card h4 {
  font-size: 16px;
  color: #333;
  margin-bottom: 4px;
  font-weight: 600;
}
.category-card p {
  font-size: 12px;
  color: #999;
}

/* 小分类标签卡片 */
.tag-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 12px;
}
.tag-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  border: 2px solid transparent;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}
.tag-card:hover {
  border-color: #ffcc80;
  box-shadow: 0 4px 12px rgba(230, 74, 25, 0.08);
}
.tag-card.active {
  border-color: #e64a19;
  background: #fff5f0;
}
.tag-icon {
  font-size: 20px;
}
.tag-label {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}
.tag-desc {
  font-size: 11px;
  color: #999;
}

/* 筛选结果条 */
.result-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fff5f0;
  border-radius: 8px;
  margin-top: 8px;
  font-size: 13px;
  color: #666;
}

@media (max-width: 600px) {
  .category-header { padding: 32px 16px; }
  .category-header h2 { font-size: 24px; }
  .container { padding: 18px 14px; }
  .section-block { margin-bottom: 26px; }
  .category-grid,
  .tag-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; }
  .category-card { min-height: 142px; padding: 18px 10px; }
  .category-icon { font-size: 34px; margin-bottom: 7px; }
  .category-card h4 { font-size: 15px; }
  .category-card p { line-height: 1.45; }
  .tag-card { min-height: 104px; padding: 14px 12px; }
  .result-bar { flex-wrap: wrap; }
}
</style>
