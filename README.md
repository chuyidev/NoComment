# NoComment - 评论隐藏工具

## 项目简介
NoComment 是一款用于隐藏特定应用评论区的 Android 应用。它通过无障碍服务实现对哔哩哔哩和抖音等应用评论区的自动隐藏，为用户提供更加简洁的浏览体验。用户可以在应用设置中轻松开启或关闭相应功能，同时应用会根据系统主题自动适配界面颜色。

## 下载链接
你可以通过以下链接下载 NoComment 应用：[https://wwti.lanzouk.com/s/chuyidev-NoComment](https://wwti.lanzouk.com/s/chuyidev-NoComment)

## 功能特性
1. **多应用支持**：目前支持哔哩哔哩（长视频、短视频、动态）和抖音应用的评论区隐藏。
2. **无障碍服务**：利用 Android 无障碍服务实现自动隐藏评论区功能。
3. **设置界面**：提供简洁直观的设置界面，用户可以方便地开启或关闭不同应用的评论隐藏功能。
4. **主题适配**：支持根据系统主题自动切换亮色和暗色模式。

## 目前支持
-  ✔ Bilibili 长短视频、动态
-  ✔ 抖音
-  ☐ 小红书
-  ☐ 知乎
  

## 安装与使用
### 安装步骤
1. 点击上述下载链接下载应用安装包。
2. 在手机中找到下载的安装包并进行安装。

### 使用步骤
1. 打开应用，进入主界面。
2. 检查无障碍功能权限状态，若未启用，点击“前往无障碍设置”按钮，在系统设置中开启 NoComment 的无障碍服务权限。
3. 在设置界面中，根据需求开启或关闭不同应用的评论隐藏功能。

## 代码结构
### 主要文件说明
- `MainActivity.kt`：应用的主界面，负责显示无障碍功能权限状态和设置卡片，处理用户的开关操作。
- `HideCommentService.kt`：无障碍服务类，监听应用的界面变化，根据用户设置隐藏相应应用的评论区。
- `AccessibilityUtil.kt`：提供检查无障碍服务是否启用的工具方法。
- `GlobalData.kt`：用于存储和读取应用的全局数据，如各个功能的开关状态。
- `ui/theme` 目录：包含应用的主题和颜色方案定义。

### 关键代码片段
#### 检查无障碍服务是否启用
```kotlin
fun isAccessibilityEnabled(context: Context, servicePackageName: String): Boolean {
    val enabledServicesSetting = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    val componentNames = enabledServicesSetting?.split(":") ?: emptyList()
    return componentNames.any { it.contains(servicePackageName) }
}
```

#### 隐藏抖音评论区
```kotlin
private fun hideDouyinComment(root: AccessibilityNodeInfo) {
    val commentNode = root.findAccessibilityNodeInfosByViewId("com.ss.android.ugc.aweme:id/title").firstOrNull()
    val videoViewNode = root.findAccessibilityNodeInfosByViewId("com.ss.android.ugc.aweme:id/mask_view").firstOrNull()
    if (commentNode != null) {
        videoViewNode?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }
}
```

## 下一步计划
~~目前的下一步计划是实现应用的后台防杀功能，确保应用在后台能够稳定运行，持续提供评论隐藏服务。具体的实现思路和方案正在研究中，后续会持续更新项目。~~
添加更多应用

## 注意事项
- 应用需要无障碍服务权限才能正常工作，请确保在系统设置中开启该权限。
- 确保打开了保活措施
- 由于不同应用的界面结构可能会发生变化，某些情况下可能无法正常隐藏评论区，请关注应用更新以获取更好的兼容性。

## 贡献与反馈
如果你对项目有任何建议或发现了问题，欢迎在项目仓库中提交 Issue 或 Pull Request。你的反馈将有助于项目的不断完善和发展。