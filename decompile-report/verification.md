# 还原验证记录（2026-10-28 更新版）

对象：新版 `liquidbounce-b100.jar`，18,857,011 字节，SHA-256 `13d021ec…`
基线：官方 b100（tag `b100`，commit `03bc91a3`）+ 本次 jar 的增量改动

## 1. 增量定位

- 新 jar 项目 class：1041 个（1041 = 上一版 1034 + 7 个新 class）
- 顶层 class：773 个；新增顶层文件：
  - `movement/Stuck`
  - `ui/client/GuiFisProxy`
  - `utils/fisproxy/FisProxyClient`（含 `Admission` 内部类）
  - `utils/fisproxy/FisProxyClientKt`
  - `utils/fisproxy/FisProxyException`
- 以上一版 rebuilt jar 为基线做字节对比，真实变化集中在 17 个 class + 7 个新 class。

## 2. 改动文件

新增：
- `features/module/modules/movement/Stuck.kt`
- `ui/client/GuiFisProxy.kt`
- `utils/fisproxy/FisProxyClient.kt`（含 `stringOrNull/string` 顶层函数）
- `utils/fisproxy/FisProxyException.kt`

修改：
- `features/module/ModuleManager.kt`（注册 Stuck）
- `ui/client/GuiMainMenu.kt`（FisProxy 按钮）
- `features/module/modules/render/ItemESP.kt`（移除 handleEvents 覆盖）
- `features/module/modules/combat/Hypixel.kt`（isReducing / findReduceTarget / isWithinReduceRange）
- `features/module/modules/combat/KeepSprint.kt`（OnlyKillAura / 状态机 / onTick、onJump）
- `features/module/modules/combat/KillAura.kt`（Hypixel 自动格挡，移除 Dual/Hypixel50%）
- `features/module/modules/combat/Velocity.kt`（isHypixelMode）
- `features/module/modules/movement/Sprint.kt`（KeepSprint 拦截）
- `injection/forge/mixins/entity/MixinEntityPlayer.java`（静态调用）

## 3. 构建结果

环境：Gradle 6.9.1、Zulu JDK 8、Kotlin 2.0.21。

```text
gradlew build reobfShadowJar -x reobfJar
BUILD SUCCESSFUL
```

产物：`build/libs/liquidbounce-b100.jar`，18,856,022 字节。
已复制为工程根目录的 `liquidbounce-b100-rebuilt.jar`。

本机处理：
- 官方 ForgeGradle 版本在 JitPack 404，使用缓存中同源的
  `com.github.UnlegitMinecraft:ForgeGradle:f2c5bb338e`；
- Gradle 仓库改用可访问的 `https://repo.maven.apache.org/maven2`；
- Kotlin 编译 `in-process`；
- shadowJar 排除 `module-info.class`（reobf 无法处理）；
- 临时 git 仓库满足 `generateGitProperties`。

## 4. 与新 jar 对比

完整记录：`jar-comparison.txt`

| 项目 | 结果 |
| --- | --- |
| 项目 class | 1041 |
| 字节完全一致 | 1018 |
| 字节不同 | 23 |
| 资源差异 | 仅 `git.properties` |
| 条目差异 | 仅 `module-info.class` |

23 个差异 class 中：
- 仅调试信息/常量池差异：`GuiMainMenu`、`Sprint`、`ItemESP`、`NoSlow`、`Rotations`、`Scaffold` 内部类等
- 代码生成结构差异：`Hypixel`、`KeepSprint`、`KillAura`、`Stuck`、`FisProxyClient`、`GuiFisProxy`、
  `FisProxyException`、`MixinEntityPlayer` 等
- 方法级差异集中在 if/return 极性、lambda 编号、局部变量顺序、`nop`；
  FisProxy/GuiFisProxy 为纯手写还原，类体积略有差异，但方法集合与调用一致

## 5. 未验证项

- 未连接 FisProxy 真实服务联调
- 未做游戏内启动测试
- 重建 jar 的 `git.properties`、`module-info.class` 与原 jar 不同
