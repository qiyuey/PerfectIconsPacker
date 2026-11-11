#!/system/bin/sh
# Mount operations only. Resource assembly and validation are implemented in Kotlin.
set -eu
fail() { echo "错误：$*" >&2; exit 1; }
[ "$(id -u)" = 0 ] || fail '请在 root 管理器中允许完美图标使用 root。'
[ "$(readlink /proc/self/ns/mnt)" = "$(readlink /proc/1/ns/mnt)" ] || fail '未进入全局挂载空间，请重新授予 root 后重试。'
[ "$(readlink -f /system/media)" = /product/media ] || fail '该系统的主题资源路径尚未适配。'
base=/data/adb/perfecticons-live
stage=$base/stage
boot=$(cat /proc/sys/kernel/random/boot_id)
mode=${1:-}
case "$mode" in apply|reapply|restore) ;; *) fail '不支持的操作。' ;; esac
mkdir /data/adb/perfecticons-live.lock 2>/dev/null || fail '另一个操作尚未结束，请先检查状态。'
trap 'rmdir /data/adb/perfecticons-live.lock' EXIT
umask 077
mkdir -p "$base/releases"
chmod 700 "$base"
mount_id() { awk -v p="$1" '$5==p {print $1}' /proc/self/mountinfo; }
owned() {
    [ -f "$base/boot" ] && [ "$(cat "$base/boot")" = "$boot" ] && [ -s "$base/mounts" ] || return 1
    [ "$(wc -l < "$base/mounts")" -eq 3 ] || return 1
    while read -r number target; do [ "$(mount_id "$target")" = "$number" ] || return 1; done < "$base/mounts"
}
refresh() {
    pm clear --user 0 --cache-only com.miui.home | grep -q '^Success' || fail '无法仅清理桌面缓存。'
    am force-stop com.miui.home
    am start -a android.intent.action.MAIN -c android.intent.category.HOME >/dev/null
    echo '图标缓存已刷新；桌面布局保持不变，手机没有重启。'
}
pm help | grep -q -- '--cache-only' || fail '系统不支持仅清缓存，已停止。'
if [ "$mode" = reapply ]; then
    [ -f "$base/source" ] || fail '尚无已保存的图标，请先点击应用图标。'
    source=$(cat "$base/source")
    case "$source" in "$base"/releases/*/theme) ;; *) fail '资源记录无效。' ;; esac
elif [ "$mode" = apply ]; then
    source=${2:-}
    case "$source" in /data/user/0/top.qiyuey.perfecticons/files/deployments/*/theme|/data/data/top.qiyuey.perfecticons/files/deployments/*/theme) ;; *) fail '资源目录不合法。' ;; esac
    [ -f "$source/default/icons" ] && [ -f "$source/default/dynamicicons" ] || fail '图标资源不完整。'
    release=$base/releases/$(basename "$(dirname "$source")")
    [ ! -e "$release" ] || fail '该资源版本已经存在。'
    mkdir "$release"
    cp -a "$source" "$release/theme"
    chown -R root:root "$release"
    chmod -R u=rwX,go=rX "$release"
    chcon -R u:object_r:system_file:s0 "$release/theme"
    source=$release/theme
fi

# Check every existing mount, including nested mounts created by other tools.
existing=$(awk '$5=="/product/media/theme/default" || index($5,"/product/media/theme/default/")==1 || $5=="/product/media/theme/miui_mod_icons" {print $1}' /proc/self/mountinfo)
if [ -n "$existing" ]; then
    owned || fail '存在其他工具的图标挂载，请先从原工具撤销后再应用。'
    if [ "$mode" = reapply ]; then refresh; exit 0; fi
    # Stop only the launcher. Unrelated apps holding icon resources are not killed.
    am force-stop com.miui.home
    if ! umount /product/media/theme/default; then
        am start -a android.intent.action.MAIN -c android.intent.category.HOME >/dev/null
        fail '图标资源被应用占用，请退出相关应用后重试。'
    fi
    if ! umount /product/media/theme/miui_mod_icons; then
        mount --bind "$stage/default" /product/media/theme/default
        mount_id /product/media/theme/default | while read -r number; do
            awk -v id="$number" '$2!="/product/media/theme/default" {print} END {print id " /product/media/theme/default"}' "$base/mounts" > "$base/mounts.new"
        done
        mv "$base/mounts.new" "$base/mounts"
        am start -a android.intent.action.MAIN -c android.intent.category.HOME >/dev/null
        fail '单张图标资源被占用，已恢复原挂载。'
    fi
    umount "$stage" || fail '无法释放旧合并目录，状态已保留。'
    rm -f "$base/boot" "$base/mounts"
fi
if [ "$mode" = restore ]; then
    refresh
    echo '已恢复系统原有图标。'
    exit 0
fi
[ -f "$source/default/icons" ] && [ -f "$source/default/dynamicicons" ] || fail '保存的图标资源缺失。'
[ -z "$(mount_id "$stage")" ] || fail '存在未完成的挂载，请先检查。'
if [ -d "$stage" ]; then rmdir "$stage" || fail '合并目录不是空目录。'; fi
mkdir "$stage"
printf '%s\n' "$boot" > "$base/boot"
: > "$base/mounts"
rollback() {
    result=$?
    trap - EXIT
    if [ "$result" -ne 0 ]; then
        reverse=$(awk '{a[NR]=$0} END {for(i=NR;i>0;i--)print a[i]}' "$base/mounts")
        if [ -n "$reverse" ]; then
            printf '%s\n' "$reverse" | while read -r number target; do
                [ "$(mount_id "$target")" = "$number" ] && umount "$target" || exit 1
            done || { rmdir /data/adb/perfecticons-live.lock; echo '回滚未完成，已保留状态。' >&2; exit "$result"; }
        fi
        rm -f "$base/mounts" "$base/boot"
        am start -a android.intent.action.MAIN -c android.intent.category.HOME >/dev/null 2>&1 || true
    fi
    rmdir /data/adb/perfecticons-live.lock
    exit "$result"
}
trap rollback EXIT
record() { printf '%s %s\n' "$(mount_id "$1")" "$1" >> "$base/mounts"; }
kb=$(du -sk "$source" /product/media/theme/default /product/media/theme/miui_mod_icons | awk '{n+=$1} END {print n+16384}')
mount -t tmpfs -o "mode=0755,size=${kb}k" KSU "$stage"
record "$stage"
mkdir "$stage/default" "$stage/miui_mod_icons"
cp -a /product/media/theme/default/. "$stage/default/"
cp -a /product/media/theme/miui_mod_icons/. "$stage/miui_mod_icons/"
cp -a "$source/default/." "$stage/default/"
cp -a "$source/miui_mod_icons/." "$stage/miui_mod_icons/"
chcon -R u:object_r:system_file:s0 "$stage"
mount --bind "$stage/default" /product/media/theme/default
record /product/media/theme/default
mount --bind "$stage/miui_mod_icons" /product/media/theme/miui_mod_icons
record /product/media/theme/miui_mod_icons
for name in icons dynamicicons; do
    expected=$(sha256sum "$source/default/$name" | cut -d ' ' -f 1)
    actual=$(sha256sum "/product/media/theme/default/$name" | cut -d ' ' -f 1)
    [ "$actual" = "$expected" ] || fail '资源校验失败。'
done
refresh
[ "$(cat /proc/sys/kernel/random/boot_id)" = "$boot" ] || fail '启动标识变化。'
printf '%s\n' "$source" > "$base/source"
trap - EXIT
rmdir /data/adb/perfecticons-live.lock
echo '图标已应用。重启后重新取得临时 root，再点击重新应用即可。'
