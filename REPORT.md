# LiquidBounce b100 更新版反编译与源码还原报告

更新日期：2026-10-28
对象：用户提供的新版 `liquidbounce-b100.jar`（18,857,011 字节，SHA-256 `13d021ec…`）

## 1. 交付物

| 路径 | 内容 |
| --- | --- |
| `liquidbounce-b100-restored/` | **可用源码工程**：621 个源文件（539 Kotlin + 82 Java） |
| `liquidbounce-b100-rebuilt.jar` | 由该工程完整 Gradle 构建出的 fat jar（18,856,022 字节） |
| `liquidbounce-b100-decompiled/` | 新版 jar 的完整反编译留档（832 个源码文件，92,585 行） |

上一版 `liquidbounce-b100.jar` 已被覆盖，但 `liquidbounce-main/` 与历史报告仍可参考。
本次新 jar 相对官方 b100 的改动已全部反映到 `liquidbounce-b100-restored`。

## 2. 新 jar 相对上一版的变化

### 2.1 新增文件

| 文件 | 内容 |
| --- | --- |
| `movement/Stuck.kt` | 新模块：冻结自身动量、按 Pulse 周期释放；Hypixel 下拦截 C03 并改发 C05；处理 S12/S08 时解冻 |
| `ui/client/GuiFisProxy.kt` | FisProxy 管理界面：Token 输入、获取资料/状态/开始加速/换 IP/停止/入口列表/复制地址 |
| `utils/fisproxy/FisProxyClient.kt` | FisProxy HTTP 客户端：Admission 准入、HMAC 签名、请求重试、会话操作；同文件包含 `stringOrNull/string` 扩展（编译为 `FisProxyClientKt`） |
| `utils/fisproxy/FisProxyException.kt` | 带 HTTP 状态码/错误码的异常 |

### 2.2 修改文件

| 文件 | 改动 |
| --- | --- |
| `ModuleManager.kt` | 注册 `Stuck`（位于 `Step` 之前） |
| `GuiMainMenu.kt` | 右上角新增 `FisProxy` 按钮（id 110），打开 `GuiFisProxy` |
| `ItemESP.kt` | 删除 `handleEvents()` 覆盖，模块不再依赖 `InventoryCleaner` 激活 |
| `Hypixel.kt` | 新增 `isReducing`；目标筛选改为 `findReduceTarget`：用 `runWithModifiedRaycastResult` + `mc.objectMouseOver.entityHit` + `isSelected` + 距离双重判断；新增 `isWithinReduceRange` |
| `KeepSprint.kt` | 新增 `OnlyKillAura`；`NewUniversal` 状态机：`sprintCancelled/attackPending/pendingTicks/forceAttackNext/killAuraAttack`；新增 `isNewUniversal`、`isVelocityReducing`、`shouldSkipKeepSprint`、`stopSprinting`、`consumeForcedAttack`、`onAttackPerformed`、`cancelPending`；新增 `onTick`/`onJump` 处理器 |
| `KillAura.kt` | `AutoBlock` 改为 `Off/Packet/Hypixel/Fake`，删除 `Hypixel50%`/`Dual`/双剑逻辑；`isPacketAutoBlock` 只含 `Packet`/`Hypixel`；攻击前调用 `KeepSprint.stopSprinting()`，攻击后 `onAttackPerformed()`；每 tick 消费 `consumeForcedAttack()` 并把 `clicks` 至少设为 1 |
| `Velocity.kt` | 新增 `isHypixelMode()` |
| `Sprint.kt` | `correctSprintState` 开头增加 KeepSprint 拦截：`KeepSprint.handleEvents() && isNewUniversal && attackPending` 时 `setSprintSafely(false)` 并返回 |
| `MixinEntityPlayer.java` | `getOldAttackInProgress` 调用改回静态形式（与 jar 一致） |

保留但未改动的上一版特性：`Hypixel` 攻击减伤/起跳重置、`Velocity` 的 Reduce 系列设置、`KeepSprintEvent`、`Scaffold FakeRotation`、`Events` 注册等。

## 3. 验证结果

### 3.1 编译与构建

- 编译：Kotlin 2.0.21（Gradle 6.9.1 + Java 8），**BUILD SUCCESSFUL**
- 构建：`gradlew build reobfShadowJar -x reobfJar`，产出 18,856,022 字节的 reobf fat jar
- 新 jar：18,857,011 字节，条目 7667；重建 jar 条目 7666（缺 `module-info.class`）

### 3.2 与新 jar 对比

完整记录见 `decompile-report/jar-comparison.txt`。

| 项目 | 结果 |
| --- | --- |
| 项目 class 总数 | 1041 |
| 字节完全一致 | **1018** |
| 字节不同 | 23 |
| 资源差异 | 仅 `git.properties` |
| 条目差异 | 仅 `module-info.class` |

23 个字节不同的 class 中，一部分只是调试信息/常量池差异（如 `GuiMainMenu`、`Sprint`、`ItemESP`、`NoSlow`、`Rotations`、`Scaffold` 内部类等），
其余集中在本次新增/重写的 `Hypixel`、`KeepSprint`、`KillAura`、`Stuck`、`FisProxyClient`、`GuiFisProxy`，
差异为分支写法、lambda 编号、局部变量顺序等代码生成层面，未发现功能缺失。

### 3.3 反编译留档

- 832 个文件：750 `.kt` + 82 `.java`，92,585 行
- 18 处 `// $VF: Couldn't be decompiled`（协程/复杂构造器）
- 按“一个 class 一个文件”输出，仅用于对照，不用于编译

## 4. 说明与限制

1. 新 jar 中的 `FisProxy`、`Stuck`、`GuiFisProxy` 是私有功能，没有公开源码可对照，均从字节码还原。
2. `FisProxy` 客户端涉及 HTTP/HMAC/Admission，已按反编译逻辑实现；未连接真实服务做联调。
3. 重建 jar 缺少 `module-info.class`（ForgeGradle reobf 无法处理，Java 8 不需要）；
   `git.properties` 来自临时构建，commit/host 与原 jar 不同。
4. 未做游戏内启动测试。
5. 项目仍为 GPL-3.0；仅为学习/研究与二次开发。

## 5. 渲染引擎替换（2026-10-29）

渲染层已整体替换为 Aerolite 版本，细节见 [RENDER-ENGINE.md](RENDER-ENGINE.md)。

- `utils/render` 68 个文件（约 10,800 行）全部换成 Aerolite 实现，`RenderUtils` 由 Kotlin object 变为 Aerolite 的 Java 类；
- 新增 `assets/minecraft/shaders/**` 着色器资源（rounded rect、blurArea、glow、outline）；
- 保留 b100 调用面的兼容层（`RoundedCorners`、`INSTANCE`、`RenderExtra.kt` 顶层函数、`StaticStorage`、`RenderTheme`）；
- `build.gradle` 修正：`shadowJar` 排除 `module-info.class`、无 git 仓库时不再中断、关闭与 `reobfShadowJar` 冲突的 `reobfJar`。

验证：JDK 8 + Gradle 6.9.1 离线执行 `gradlew build`（clean）**BUILD SUCCESSFUL**，
产物 `build/libs/liquidbounce-b100.jar`：21,846,366 字节、7,781 个条目、zip 校验通过。

## 6. 许可

LiquidBounce 采用 GPL-3.0（https://github.com/CCBlueX/LiquidBounce）。
