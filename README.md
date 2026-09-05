# 星途清单 Android 原生培训 Demo

“星途清单”是一款离线行程准备清单，用于演示 Android 原生应用迁移到 HarmonyOS 原生应用时的页面、状态、数据与系统能力映射。工程采用 Kotlin + XML，没有使用 Compose，也没有申请网络权限。

## 已实现范围

- 首页：动态问候、下一行程、倒计时、完成进度、待办快捷打卡。
- 行程：搜索、状态筛选、列表、手机单页导航、`sw600dp` 平板分栏。
- 行程详情：进度、备注、清单打卡、清单项增删改、行程编辑/删除/分享。
- 新建行程：日期选择、5 种主题、商务/周末/空白模板、表单校验。
- 数据：行程与事项汇总、近 7 天趋势、分类完成率。
- 我的：跟随系统/浅色/深色主题、振动与通知开关、演示数据重置。
- 原生能力实验室：设备与窗口信息、运行时通知权限、立即通知、振动、系统分享、小组件说明。
- 桌面小组件：下一行程、倒计时和完成进度，点击通过 Deep Link 进入详情。
- 离线数据：Room + SharedPreferences；首次启动自动生成日期动态的深圳、杭州两条演示数据。

## 架构

```text
UI（Activity / Fragment / XML / ViewBinding）
        ↓ StateFlow / UiEffect
ViewModel
        ↓
UseCase（校验、筛选、状态、进度、统计、分享文案）
        ↓
Repository
        ↓
Room / SharedPreferences / Android 系统能力
```

所有日期在数据库中保存为 Epoch Day，避免时区导致行程日期偏移。清单项和行程使用外键级联删除；演示数据日期相对“今天”生成，因此不会过期。

## 培训演示建议（约 8 分钟）

1. 首页勾选一个待办，观察 Room 数据变化驱动首页进度和统计页联动更新。
2. 进入行程页，演示搜索、状态筛选和详情；平板设备上展示分栏布局。
3. 新建一个行程，切换主题和清单模板，再新增/编辑/删除清单项。
4. 在详情页调用 Android 系统分享。
5. 在“我的”切换深色模式，进入“原生能力实验室”演示通知权限、通知、振动和设备信息。
6. 添加桌面小组件并点击，演示 `xingtu://trip/{tripId}` Deep Link。
7. 回到“我的”重置演示数据，为下一轮课堂演示恢复初始状态。

迁移讲解时可重点对照：Activity/Fragment 与 HarmonyOS Ability/页面路由、Room 与关系型数据库方案、App Widget 与服务卡片、Android Runtime Permission 与鸿蒙授权模型、`sw600dp` 资源适配与响应式断点布局。

## 构建与验证

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug
```

Debug APK：`app/build/outputs/apk/debug/app-debug.apk`

当前验证结果：62 个领域层单元测试全部通过、Android Lint 通过、Debug APK 已使用本机调试证书签名。该包仅用于培训演示，不用于商店上架。
