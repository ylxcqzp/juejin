# TypeScript 5.0 新特性全解析

## 1. 装饰器标准化（Stage 3）

TypeScript 5.0 实现了 ECMAScript Stage 3 的**装饰器提案**，与传统的 `experimentalDecorators` 有显著区别：

```typescript
// 新的类装饰器
function log(originalMethod: any, context: ClassMethodDecoratorContext) {
  const methodName = String(context.name)
  return function (this: any, ...args: any[]) {
    console.log(`调用 ${methodName}，参数:`, args)
    const result = originalMethod.call(this, ...args)
    console.log(`返回值:`, result)
    return result
  }
}

class Calculator {
  @log
  add(a: number, b: number): number {
    return a + b
  }
}
```

## 2. const 类型参数

新的 `const` 修饰符让泛型推断更精确：

```typescript
// 之前 — 推断为 string[]
function getValues<T>(items: T[]): T[] { return items }
const values = getValues(['a', 'b', 'c'])  // type: string[]

// 现在 — 推断为 readonly ["a", "b", "c"]
function getConstValues<const T>(items: T[]): T[] { return items }
const constValues = getConstValues(['a', 'b', 'c'])
// type: readonly ["a", "b", "c"]
```

## 3. 枚举增强

支持联合枚举：

```typescript
enum Status {
  Draft = 'draft',
  Published = 'published',
  Archived = 'archived'
}

// 现在可以直接用枚举值作为类型
type PublishedStatus = Status.Published | Status.Archived
```

## 版本兼容性

| 特性 | TS 4.x | TS 5.0 | TS 5.4 |
|------|--------|--------|--------|
| 装饰器 | experimental | ✅ Stage 3 | ✅ |
| const 类型参数 | ❌ | ✅ | ✅ |
| 枚举联合 | ❌ | ✅ | ✅ |
| NoInfer 工具类型 | ❌ | ✅ | ✅ |

> 📘 TypeScript 5.0 是一次里程碑式的更新。装饰器标准化意味着未来可以直接在浏览器中运行，无需额外的转译步骤。
