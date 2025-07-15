# 复习策略配置说明

## 概述

本项目支持两种"未掌握"处理策略，可以通过配置文件动态切换。

## 配置方式

在 `application.properties` 文件中配置：

```properties
# 复习策略配置
# 策略类型: RESTART_FROM_TOMORROW(从明天重新开始) 或 ADD_EXTRA_REVIEWS(增加额外复习)
review.strategy.not-mastered=RESTART_FROM_TOMORROW
# 当使用ADD_EXTRA_REVIEWS策略时，额外增加的复习次数
review.strategy.extra-review-count=2
```

## 策略说明

### 1. RESTART_FROM_TOMORROW（默认策略）

**行为**：当复习标记为"未掌握"时，删除所有现有复习计划，从明天开始重新生成完整的10次复习计划。

**特点**：
- 完全重置复习进度
- 从明天开始按艾宾浩斯曲线重新安排
- 复习间隔：1, 2, 4, 7, 14, 21, 30, 45, 60, 90天

### 2. ADD_EXTRA_REVIEWS（新策略）

**行为**：当复习标记为"未掌握"时，在第二天和第三天增加额外复习，原有的所有未完成复习计划顺延2天。

**特点**：
- 保留原有复习计划结构
- 增加指定次数的额外复习（默认2次）
- 所有未完成的复习计划顺延相应天数
- 额外复习的reviewStage标记为0，便于识别

**示例**：
- 原计划：今天未掌握，后续有第3天、第7天、第14天的复习
- 新计划：明天、后天增加额外复习，原计划变为第5天、第9天、第16天

## 配置参数

| 参数 | 说明 | 默认值 | 可选值 |
|------|------|--------|--------|
| `review.strategy.not-mastered` | 未掌握处理策略 | `RESTART_FROM_TOMORROW` | `RESTART_FROM_TOMORROW`, `ADD_EXTRA_REVIEWS` |
| `review.strategy.extra-review-count` | 额外复习次数 | `2` | 任意正整数 |

## 使用建议

1. **RESTART_FROM_TOMORROW**：适合希望严格按照艾宾浩斯曲线重新学习的场景
2. **ADD_EXTRA_REVIEWS**：适合希望保持学习进度，只是加强练习的场景

## 注意事项

1. 配置修改后需要重启应用才能生效
2. 策略只影响"未掌握"的处理方式，不影响正常的复习计划生成
3. 额外复习的reviewStage为0，在统计和显示时可以特殊处理