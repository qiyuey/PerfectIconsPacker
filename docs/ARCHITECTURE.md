# 架构

## 产品与数据流

应用的底部 Liquid Glass 悬浮栏包含“图标、设置”两个页面，无独立资源页。`ui → domain → data`：Compose/Miuix 页面与 ViewModel 调用上游快照合并和部署资源写入；数据层负责 JGit 与 root。

两个 Tab 共用 HorizontalPager 与 LiquidPagerNavigation，分别使用 MiuixIcons.Regular.GridView、Settings。IconResources 将当前快照、检查更新、清理缓存放在 HomeScreen 的“应用图标”下方；ResourcesViewModel 管理资源操作，操作完成后刷新图标状态，应用图标与缓存修改互斥。SettingsScreen 展示共享 RootViewModel 的权限状态、检测错误、重新检测及图标日志入口；Scaffold 统一检测并向图标页传入 rootGranted。

`IconRepository` 固定直连 `pzcn/Perfect-Icons-Completion-Project`。meta.git 浅拉取 commit/tree（blob:none），独立非浅 blobs.git 按 SHA 拉 blob；按选定图标主题筛选，只取静态0/1层、主题单张PNG、主题及common动态图标。保留重试、进度、快照锁和来源记录；临时物化完成后替换快照。

`ThemeAssembler` 只合并快照中的基础与主题图标，使用应用内置HyperOS4蒙版。不读取旧custom目录。`DeploymentResources` 生成独立default/icons、default/dynamicicons与miui_mod_icons/*.png。内层ZIP由ThemeZipWriter校验路径、保留图片字节。自定义存储、导入器、禁用/覆盖/附加逻辑、SAF Provider、Module/MTZ导出及其专用测试均已移除；不删除设备上的历史文件。

## Liquid Glass 导航

仅使用Miuix官方液态玻璃示例派生实现：LayerBackdrop、Lens/Vibrancy、CombinedBackdrop、按压/拖动透镜和高光。明暗模式固定跟随系统，旧theme_mode与材质偏好不再读取。

默认64dp面板、4dp内边距、56dp单项；Tab宽度按图标/标签度量并限制窄屏总宽，底部inset只处理一次。手势在Initial阶段持续观察；目标同步写入并以KernelSU同款UNDISPATCHED弹簧更新，继承当前速度，不逐事件snap或预先cancel；松手只吸附目标。Pager由LiquidPagerNavigation统一管理selectedPage和导航Job，程序切页时忽略途经页，手动横滑时同步选择；替换请求先转移Job所有权再取消旧任务。使用HorizontalPager与springAnimateToPage，预组合邻页，保留状态；显式列表overscroll关闭默认效果。版权见THIRD_PARTY_NOTICES.md，工作区设计skill仅维护这一种效果。

## 本机应用与Root

图标日志入口位于设置页，复用 Scaffold 持有的 HomeViewModel 与日志弹窗；无日志时禁用按钮，图标操作自动弹出实时日志。

设置页的 Root 就绪、检测中、未授权及错误状态复用图标资源状态的 StatusCard，统一标题、语义色、说明、页脚、卡片内边距与圆角；重新检测保留在下方独立操作卡。

首页只提供应用、重新应用、恢复系统图标。RootDeployer通过libsu运行自有assets/root/live-deploy.sh；Kotlin负责组装校验，shell仅承担权限、复制、挂载与系统命令。不运行参考项目脚本。

Application使用FLAG_MOUNT_MASTER；脚本确认UID0与PID1挂载namespace相同。资源复制到/data/adb/perfecticons-live/releases/<UUID>/theme、归属root并设置system_file标签；临时组装目录随后清理。已挂载的资源不自动删除。

tmpfs合并ROM原资源与上游资源后bind两个系统目录；保存boot和三个mount ID。其他工具挂载阻止应用；仅撤销自身挂载，默认遇EBUSY停止，不强停其他应用、不force/lazy卸载。异常回滚保留未能撤销的状态。

仅清com.miui.home缓存并重启桌面进程；不清布局、数据库、设置或全局缓存，不重启手机。重启后由用户重新取得临时root，再重新应用保存资源；不承诺自动开机挂载。

## 构建

Java27编译工具链（本机winget Oracle.JDK.27；输出Java21字节码以兼容当前Lint读取器）、Gradle9.8.2、AGP9.6.0-alpha02、Kotlin2.5.0-Beta2、SDK37.2 / target37、Build Tools37.0.0。Compose1.13.0-rc01、Miuix0.9.4；minSdk33。依赖解析沿用上游的 Gradle Plugin Portal、Google Maven 和 Maven Central，不再维护本地镜像及 CI 分支特例。

验证`assembleDebug`、`testDebugUnitTest`；发布收缩验证使用`assembleRelease`。拖动轨迹回归使用fake时钟；视觉和性能验收单独记录。

本机性能验收可用`-PpersonalRelease=true :app:assembleRelease`：R8优化、不可调试，使用既有debug安装签名以覆盖本机旧包；不使用该参数时保留原发布签名流程。

三应用Tab样式统一：MiuixIcons.Regular库引用、24dp、footnote2、1dp图文间距；NavigationLayout与LiquidGlassNavigationBar共用相同实现（仅包名不同）。大字体动态增高，列表根据实际底栏占用+12dp避让；无独立顶部8dp留白。工作区 [design-hyperos-ui 的 Tab 视觉标准](../../.agents/skills/design-hyperos-ui/references/tab-standards.md)为统一标准入口。

页面布局使用共享PageDesign：12dp手机边距、800dp内容上限、12dp分组间距、16dp自由内容内边距；标题采用Miuix可折叠TopAppBar和主题实色，移除每页额外的模糊采样，仅底栏保留Liquid Glass。状态卡使用主题字体与普通Card，移除巨型溢出图标及自定义色块。标准见工作区 [design-hyperos-ui 的页面规范](../../.agents/skills/design-hyperos-ui/references/pages.md)。

## 已移除功能的残留清理

主题固定为 IconTheme.styles（HyperOS4），同步与组装使用同一常量；不再读取历史 icon_styles 偏好，移除主题选择器、偏好包装与未使用的上游主题列表 API。历史快照若不是 HyperOS4，提示重新同步。RootDetector只验证成功的UID0回复，并保留用户主动重检时的libsu结论/非root shell缓存刷新；不再探测Root品牌、版本或元模块。删除旧安装结果模型、无调用的设置刷新/错误消除入口及SAF混淆保留规则。

快照接口直接返回RepoSnapshot；同步固定使用默认仓库，保留4次重试、取消传播、快照锁和来源校验，不再构建多端点列表。同步进度仅保留当前显示的阶段和计数。ZIP保留真实文件/目录写入与路径校验，删除已无调用的字节写入和图标导入标识转换辅助。内存日志直接保存文本，删除从未使用的旧分级模型。

minSdk33使用原有自适应启动图标，删除不可命中的旧密度PNG并合并版本资源目录；启动图标图形保持不变。删除无使用的BuildConfig生成与网络状态权限。

## 功能范围

已移除证书与隐私功能的页面、ViewModel、部署逻辑、Root 脚本、包可见性声明、专用测试及文档。升级保留应用数据，不执行历史证书挂载或隐私设置的撤销，也不清理设备上的既有运行状态。
