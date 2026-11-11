# HyperOS 4 部署资源

应用私有暂存目录deployments/<UUID>/theme下包含：

- default/icons：ZIP，res/drawable-xxhdpi/<id>/0.png与1.png，以及transform_config.xml。
- default/dynamicicons：ZIP，layer_animating_icons/<id>/及其全部资源。
- miui_mod_icons/<id>.png：单张图标，原字节复制。

仅支持HyperOS4。静态只取上游0/1层，动态目录完整保留。ZIP拒绝绝对路径、反斜杠和路径穿越，遍历动态目录时不跟随符号链接。PNG保持原始字节，XML按文本压缩。

Kotlin组装后由自有root脚本复制到/data/adb/perfecticons-live/releases/<UUID>/theme；合并ROM默认资源后挂载到/product/media/theme/default及miui_mod_icons。不生成外层Module/MTZ，不支持自定义导入或高级附加。
