# 渲染引擎：替换为 Aerolite 版本

替换日期：2026-10-29
参考工程：`E:\Tencent Flies\Tencent Files\1601770694\SourceCode\Aerolite-main`
目标工程：本目录（LiquidBounce b100 源码还原工程）

## 1. 替换范围

| 内容 | 说明 |
| --- | --- |
| `src/main/java/net/ccbluex/liquidbounce/utils/render/**` | 68 个文件、约 10,800 行，整体替换为 Aerolite 的实现 |
| `src/main/resources/assets/minecraft/shaders/**` | Aerolite 的顶点/片元着色器与后处理（blurArea）资源，原样加入 |

原有的 `RenderUtils.kt`（Kotlin object，1640 行）被 Aerolite 的 `RenderUtils.java`（约 2700 行）取代，
其内部的绘制实现全部换成 Aerolite 的代码。

## 2. 组件对照

| 子系统 | 替换前（b100） | 替换后（Aerolite） |
| --- | --- | --- |
| 2D 图元 | RenderUtils.kt 内部 glBegin 代码 | `RenderUtils.java` + `RenderPrimitives` / `RenderGL` / `RenderTexture` / `RenderHelper` |
| 圆角矩形 / 描边 | Kotlin 手写三角扇 | `RoundedUtil` + `ShaderUtil`（GLSL `roundedRect`，含抗锯齿与 blur 开关） |
| 背景模糊 | 无 | `BlurUtils`（`shaders/post/blurArea.json`）+ `shader/UIEffectRenderer` |
| 模板裁剪 | `OutlineUtils` + 内联 stencil 代码 | `StencilUtils` / `Stencil` |
| GL 状态 | RenderUtils 内部 map | `RenderGL` / `GLUtils` / `BlendUtils` |
| 3D 与实体绘制 | RenderUtils.kt | `Render3D` / `RenderEntity` / `RenderEffects` / `RenderExtra.kt` |
| 文字与纹理 | FontRenderer 直绘 | `RenderText` / `RenderTexture` / `WorldTextRenderer` |
| 着色器框架 | `shader/*.kt` | `shader/*.java`（FramebufferShader、Shader、BackgroundShader、Glow/Outline/Gradient/Rainbow） |

## 3. 兼容层（保证原有调用点不用改）

- `RenderUtils.INSTANCE`、`RenderUtils.Corner` / `RenderUtils.RoundedCorners` 枚举；
- `drawRoundedRect` / `drawRoundedRectInt` / `drawRoundedBorder` / `drawRoundedBorderRect` 等 b100 签名；
- b100 专有的 `withClipping`、`withOutline`、`drawHueCircle`、`drawDome`、`drawCone`、`drawConesForEntities` 等
  迁到 `RenderExtra.kt` 顶层函数，各调用点已改为导入顶层函数（Java mixin 通过 `RenderExtraKt` 调用）；
- 新增 `injection/access/StaticStorage.java`：`MixinMinecraft` 每帧写入当前 `ScaledResolution`，
  供 `BlurUtils`、`FramebufferShader`、`RenderUtils` 读取；
- 新增 `RenderTheme.kt`：替代 Aerolite 中 `CombatVisuals` / `ClientThemesUtils` 的主题色与动画时钟；
- `ColorSettings.kt` 以本工程的配置系统重写，替代 FDPClient 的 `ConfigurableImpl`。

## 4. 适配细节

- 纹理资源路径 `aerolite/...` → `liquidbounce/...`（沿用本工程已有贴图）；
- 字体引用 `Fonts.font35` → `Fonts.fontSemibold35`（b100 的字体注册表）；
- 移除 Aerolite 私有的 `me.stars.vec.Particle` / `FDPClient` / `legacyui` / `slib.Colors` 依赖，
  对应的粒子绘制与配色改由本工程 `ParticleUtils`、`RenderTheme` 承担；
- 未移植 `RenderWings.kt`（依赖 Wings 模块）与 `Pair.kt`（无引用）。

## 5. 构建

- 环境：JDK 8（Zulu 8）、Gradle 6.9.1、离线依赖缓存；
- `build.gradle` 三处调整（仅为让构建可复现，不涉及渲染逻辑）：
  1. `shadowJar` 排除 `module-info.class`（Java 9+ 多版本元数据无法 reobf）；
  2. `gitProperties.failOnNoGitDirectory = false`（目录不是 git 仓库时不再中断构建）；
  3. `tasks.reobfJar.enabled = false`（与 `reobfShadowJar` 写同一输出文件会导致 jar 截断）。
- 命令：`gradlew build`
- 产物：`build/libs/liquidbounce-b100.jar`，21,833,477 字节，7773 个条目，zip 校验通过。

## 6. 现状与限制

1. **字体栈未替换**：`ui/font`（Fonts / AWTFontRenderer / GameFontRenderer）仍是 b100 的实现。
   Aerolite 字体栈依赖其自带 ttf 资源与 FDP 字体重载，属于独立子系统，本次未动。
2. `RoundedUtil` / `BlurUtils` / `StencilUtils` 已编入工程，但 UI 兼容层里的圆角仍走 CPU 三角扇
   （`drawRoundedRectangle`），因此界面观感与 b100 接近；若要启用着色器圆角/背景模糊，需要把
   `drawRoundedRect` 的 `RoundedCorners.ALL` 分支切到 `RoundedUtil.drawRound`，并进游戏验证 GL 状态。
3. 未在游戏内启动实测（当前环境无 Forge 客户端运行环境）。
