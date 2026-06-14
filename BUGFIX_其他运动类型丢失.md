# Bug 修复说明：「其他」运动打卡刷新后类型丢失

## 问题描述

选择「其他」运动类型打卡时，数据被保存为 `sport_type_id=5`，刷新页面后显示为「健身」，原始「其他」类型信息丢失。

## 根因分析

### 数据流追踪

1. 前端用户选择「其他」→ `formData.type = 'other'`
2. `frontToBack()` 调用 `getTypeId('other')` 查询 `typeIdMapCache`
3. **问题点 1**：`typeIdMapCache` 中 `other: 5`，与 `gym: 5`、`fitness: 5` 共享同一 ID
4. 记录以 `sport_type_id=5` 写入后端数据库
5. 刷新时 `backToFront()` 调用 `getTypeInfo(5)` 查询 `idTypeMapCache`
6. **问题点 2**：`idTypeMapCache[5]` 仅映射为 `{ type: 'gym', typeName: '健身' }`，无「其他」条目
7. **问题点 3**：数据库 `sport_type` 表（init.sql）只有 8 条记录（ID 1-8），缺少「其他」类型

### 根因总结

| 问题 | 位置 | 说明 |
|------|------|------|
| ID 冲突 | `checkin.js` → `typeIdMapCache` | `other` 与 `gym`/`fitness` 共用 `sport_type_id=5` |
| 反向映射缺失 | `checkin.js` → `idTypeMapCache` | ID 5 仅映射为「健身」，无「其他」条目 |
| 数据库缺数据 | `init.sql` | `sport_type` 表无「其他」记录，后端无法返回该类型 |

## 修复方案

为「其他」分配独立的 `sport_type_id=9`，与「健身」(ID=5) 彻底解耦。

## 修改文件清单

### 1. 后端：`backend/src/main/resources/sql/init.sql`

**修改内容**：在 `sport_type` 表的 INSERT 语句中新增「其他」类型记录

```sql
-- 修改前
('徒步', 'hiking', '户外徒步登山', 5.50, 8, 450);

-- 修改后
('徒步', 'hiking', '户外徒步登山', 5.50, 8, 450),
('其他', 'other', '其他运动类型', 5.00, 9, 300);
```

| 字段 | 值 | 说明 |
|------|----|------|
| name | 其他 | 类型名称 |
| icon | other | 与前端 `type` 值一致 |
| description | 其他运动类型 | 类型描述 |
| calorie_per_minute | 5.00 | 与前端 `caloriePerMinuteMap.other` 一致 |
| sort | 9 | 排在最后 |
| hot_count | 300 | 初始热度 |

### 2. 前端：`frontend/src/stores/checkin.js`

**修改 1**：`typeIdMapCache` — 将 `other` 从 ID 5 改为 ID 9

```javascript
// 修改前
other: 5

// 修改后
other: 9
```

**修改 2**：`idTypeMapCache` — 新增 ID 9 的反向映射

```javascript
// 修改前（无第 9 条）
8: { type: 'hiking', typeName: '徒步', amountUnit: '公里' }

// 修改后
8: { type: 'hiking', typeName: '徒步', amountUnit: '公里' },
9: { type: 'other', typeName: '其他', amountUnit: '次' }
```

**修改 3**：`getTypeId()` — 未知类型 fallback 从 ID 5 改为 ID 9

```javascript
// 修改前
return typeIdMapCache[type] || 5

// 修改后
return typeIdMapCache[type] || 9
```

> 未知类型归入「其他」比归入「健身」更合理。

**修改 4**：`amountUnitForType()` — 显式添加 `other` 映射

```javascript
// 修改前（无 other 条目，靠 fallback 返回 '次'）
hiking: '公里'

// 修改后
hiking: '公里',
other: '次'
```

## 修复效果验证

| 场景 | 修复前 | 修复后 |
|------|--------|--------|
| 选择「其他」打卡 | `sport_type_id=5` 保存 | `sport_type_id=9` 保存 |
| 刷新后显示 | 显示「健身」 | 显示「其他」 |
| 后端 API 返回类型列表 | 无「其他」条目 | 包含「其他」(ID=9) |
| 未知类型 fallback | 归入「健身」(ID=5) | 归入「其他」(ID=9) |

## 注意事项

- 已有数据库需手动执行：`INSERT INTO sport_type (name, icon, description, calorie_per_minute, sort, hot_count) VALUES ('其他', 'other', '其他运动类型', 5.00, 9, 300);`
- 修复前已保存的 `sport_type_id=5` 的「其他」记录无法自动修正（因数据库层面无法区分），需根据业务需要手动处理
