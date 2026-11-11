# PerfectIconsPacker — 本机应用版

个人 fork：[qiyuey/PerfectIconsPacker](https://github.com/qiyuey/PerfectIconsPacker)，上游：[YifePlayte/PerfectIconsPacker](https://github.com/YifePlayte/PerfectIconsPacker)。

面向 HyperOS 4 的完美图标应用工具。从上游同步资源，使用已有 root 直接挂载并刷新图标缓存。首页提供“应用图标”“重新应用”“恢复系统图标”，不再导出 ZIP / MTZ 或安装 root 模块。

底栏使用 Liquid Glass，采用背景采样、折射透镜、按压形变与拖动吸附。应用明暗模式固定跟随系统。

底部悬浮栏包含“图标、设置”两个 Tab，支持点击和横向滑动切页。图标页合并当前快照、检查更新、清理缓存及图标应用，主题固定为 HyperOS 4。设置提供 Root 权限状态、重新检测和图标日志；执行图标操作时自动弹出日志。

临时 root 重启后失效时，先重新取得 root，再点击“重新应用”。不需要元模块，不自动重启手机；桌面布局与原系统主题资源保留。应用 ID 为 `io.github.perfecticons`，与上游动态图标适配包名一致。旧版 `top.qiyuey.perfecticons` 会被 Android 视为另一款应用，不能原地覆盖或自动迁移数据。

- [架构与部署](docs/ARCHITECTURE.md)
- [主题资源格式](docs/THEME_FORMAT.md)
## 约束

- 只支持 HyperOS 4 的主题结构。
- 图标资源固定直连 `pzcn/Perfect-Icons-Completion-Project`，不提供仓库、加速或代理编辑。
- 导航为图标、设置；设置包含 Root 权限与图标日志入口。图标主题固定 HyperOS 4，无独立资源页或主题选择器。已移除自定义编辑与 SAF 入口，旧自定义文件不再参与应用。
- 图标资源仓库只读：不修改、不提交、不执行其中任何脚本。
- 参考项目 `Perfect-Icons-APP` 中的 shell 脚本**禁止执行**，其中大量变量需要外部注入。

## 构建与签名

CI 的 Debug APK 使用 Android 调试签名；未配置签名时 Release APK 未签名。配置签名后，`assembleRelease` 会输出已签名的 `app-release.apk`。

签名参数全部通过环境变量读取（见 `app/build.gradle.kts`）：

| 环境变量 | 含义 |
| --- | --- |
| `SIGNING_STORE_FILE` | jks 文件路径 |
| `SIGNING_STORE_PASSWORD` | keystore 密码 |
| `SIGNING_KEY_ALIAS` | 密钥别名 |
| `SIGNING_KEY_PASSWORD` | 密钥密码 |

只要 `SIGNING_STORE_FILE` 非空，release 就会用这套配置签名。

### CI 签名

在 **Settings → Secrets and variables → Actions** 添加四个 secret：

| Secret | 内容 |
| --- | --- |
| `SIGNING_KEYSTORE_BASE64` | jks 的 base64 文本 |
| `SIGNING_STORE_PASSWORD` | keystore 密码 |
| `SIGNING_KEY_ALIAS` | 密钥别名 |
| `SIGNING_KEY_PASSWORD` | 密钥密码 |

把 jks 转成单行 base64：

```bash
base64 -w0 release.jks
```

工作流把 base64 解码到 `$RUNNER_TEMP/release.jks`，再把四个变量写入 `$GITHUB_ENV` 供 Gradle 使用。
未配置 `SIGNING_KEYSTORE_BASE64` 时（fork、PR）自动跳过，仍然产出未签名 APK。

### 本地签名

```bash
SIGNING_STORE_FILE=/绝对路径/release.jks \
SIGNING_STORE_PASSWORD=**** \
SIGNING_KEY_ALIAS=your-alias \
SIGNING_KEY_PASSWORD=**** \
./gradlew :app:assembleRelease
```

### 校验签名

```bash
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
```

### CI 产物

每次构建上传三个 artifact，APK 以 zip 压缩包形式提供（[nightly.link](https://nightly.link)
只支持 `*.zip` 下载，这也是不能用 `archive: false` 的原因）：

| Artifact | 内容 |
| --- | --- |
| `app-debug` | `app-debug.apk` |
| `app-release` | `app-release.apk`（未配置签名时为未签名包） |
| `test-reports` | 单元测试报告目录 |

免登录下载最新构建：

```text
https://nightly.link/YifePlayte/PerfectIconsPacker/workflows/build/main/app-release.zip
```

## 许可证

本项目基于 [GPL-3.0](LICENSE) 发布。

图标资源版权归原作者所有，来自 `pzcn/Perfect-Icons-Completion-Project`，不适用本项目的许可证。
