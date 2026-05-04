# Vue 3 Composition API 实战：从入门到精通

## 为什么选择 Composition API？

在 Vue 2 中，我们使用 Options API 来组织组件逻辑。但随着组件越来越复杂，Options API 的局限性开始显现：

- **逻辑复用困难**：mixin 和 slot 难以追踪数据来源
- **类型推断不友好**：TypeScript 支持需要额外的装饰器
- **代码组织分散**：同一个功能的 data、methods、computed 分散在不同选项中

Composition API 通过 **函数式组合** 解决了这些问题。

## 响应式系统核心

Vue 3 的响应式系统基于 **Proxy**，相比 Vue 2 的 `Object.defineProperty`：

| 特性 | Vue 2 | Vue 3 |
|------|-------|-------|
| 对象属性新增检测 | ❌ 需要 `$set` | ✅ 自动追踪 |
| 数组索引修改 | ❌ 无法检测 | ✅ 正常响应 |
| Map/Set 支持 | ❌ 不支持 | ✅ 原生支持 |
| 性能（大数据量） | 较慢 | 快约 2x |

## ref 与 reactive 的选择

```typescript
import { ref, reactive, computed, watch } from 'vue'

// ref：适合基本类型和需要替换整个值的场景
const count = ref(0)
const doubled = computed(() => count.value * 2)

// reactive：适合复杂对象
const user = reactive({
  name: '张三',
  profile: {
    bio: '前端工程师',
    skills: ['Vue', 'React', 'TypeScript']
  }
})

// 监听多个数据源
watch([count, () => user.name], ([newCount, newName], [oldCount, oldName]) => {
  console.log(`count: ${oldCount} → ${newCount}`)
  console.log(`name: ${oldName} → ${newName}`)
})
```

## 实际项目中的最佳实践

### 1. 封装可复用逻辑（Composables）

```typescript
// composables/useFetch.ts
export function useFetch<T>(url: string) {
  const data = ref<T | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function execute() {
    loading.value = true
    error.value = null
    try {
      const res = await fetch(url)
      data.value = await res.json()
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Unknown error'
    } finally {
      loading.value = false
    }
  }

  return { data, loading, error, execute }
}
```

### 2. 配合 TypeScript 的类型安全

```typescript
interface Article {
  id: number
  title: string
  content: string
  tags: string[]
  author: { name: string; avatar: string }
}

const article = ref<Article | null>(null)

// 完整的类型推导和智能提示
article.value?.tags.forEach(tag => console.log(tag.toUpperCase()))
```

> 💡 提示：使用 `<script setup lang="ts">` 可以获得最佳的 TypeScript 开发体验，无需额外配置。

## 性能优化清单

1. 使用 `shallowRef` 处理大型只读数据集
2. `v-memo` 指令缓存子树（Vue 3.2+）
3. `computed` 返回值会被自动缓存
4. 异步组件 `defineAsyncComponent` 实现代码拆分
5. 合理使用 `v-once` 和 `v-show` 替代不必要的 `v-if`

![Vue 3 响应式系统架构](https://picsum.photos/seed/vue3reactivity/800/400)

## 总结

Composition API 不仅仅是 API 的变化，更是一种 **编程思维的转变**。从 "按选项组织" 变为 "按功能组织"，让你的代码更具可维护性和可测试性。
