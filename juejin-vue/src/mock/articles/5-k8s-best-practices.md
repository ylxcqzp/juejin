# Kubernetes 生产环境最佳实践（2025 版）

## 1. 资源配置

**永远为每个容器设置 resource requests 和 limits**：

```yaml
apiVersion: v1
kind: Pod
spec:
  containers:
    - name: app
      image: myapp:latest
      resources:
        requests:
          memory: "256Mi"
          cpu: "250m"
        limits:
          memory: "512Mi"
          cpu: "500m"
```

如果不设置资源限制，一个 Pod 的内存泄漏可能拖垮整个节点。

## 2. 健康检查

```yaml
livenessProbe:
  httpGet:
    path: /healthz
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
  failureThreshold: 3

readinessProbe:
  httpGet:
    path: /ready
    port: 8080
  initialDelaySeconds: 5
  periodSeconds: 5
```

`livenessProbe` 用于检测容器是否需要重启，`readinessProbe` 用于检测容器是否可以接收流量。

## 3. 滚动更新策略

```yaml
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxSurge: 1
    maxUnavailable: 0  # 零停机部署
```

设置 `maxUnavailable: 0` 确保滚动更新期间始终有足够副本处理请求。

## 4. Pod 反亲和性

避免同一服务的所有副本调度到同一节点：

```yaml
affinity:
  podAntiAffinity:
    preferredDuringSchedulingIgnoredDuringExecution:
      - weight: 100
        podAffinityTerm:
          topologyKey: kubernetes.io/hostname
          labelSelector:
            matchLabels:
              app: myapp
```

## 5. HPA 自动伸缩

| 指标 | 阈值 | 最小副本 | 最大副本 |
|------|------|----------|----------|
| CPU | 70% | 2 | 10 |
| 内存 | 80% | 2 | 10 |
| 自定义（QPS）| 1000/s | 3 | 20 |
| 自定义（延迟）| 200ms P99 | 5 | 30 |

> 🔒 安全提醒：生产环境务必使用非 root 用户运行容器，并启用 `readOnlyRootFilesystem` 防止容器内文件被篡改。

![K8s Dashboard](https://picsum.photos/seed/k8s-prod/800/400)
