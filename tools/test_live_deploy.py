"""Exercise the shipped shell detach helper with fake mounts, without root."""
from pathlib import Path
import os
import re
import shutil
import subprocess
import unittest


ROOT = Path(__file__).resolve().parents[1]
SCRIPT = ROOT / "app/src/main/assets/root/live-deploy.sh"
BASH = shutil.which("bash") if os.name != "nt" else "C:/Program Files/Git/bin/bash.exe"


class DetachTest(unittest.TestCase):
    def run_case(self, *, busy=False, current="42", children="", lazy_fails=False,
                 change_after_failure=False, expected_success=True, calls="normal"):
        helper = re.search(r"^detach_owned\(\) \{\n.*?^\}", SCRIPT.read_text(encoding="utf-8"), re.M | re.S).group()
        # awk is mocked only for the state file lookup. The actual helper runs unchanged.
        shell = f"""
set -eu
base=/unused
current='{current}'
children='{children}'
calls=''
awk() {{ echo 42; }}
mount_id() {{ printf '%s' "$current"; }}
mount_children() {{ printf '%s' "$children"; }}
umount() {{
    if [ "$1" = -l ]; then
        calls="$calls lazy"
        {'return 1' if lazy_fails else "current=''; return 0"}
    fi
    calls=normal
    {'current=99;' if change_after_failure else ''}
    {'return 1' if busy else "current=''; return 0"}
}}
{helper}
if detach_owned /icons; then result=0; else result=1; fi
[ "$result" = {'0' if expected_success else '1'} ]
[ "$calls" = '{calls}' ]
"""
        result = subprocess.run([BASH, "-c", shell], capture_output=True, text=True, encoding="utf-8")
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)

    def test_normal_unmount(self):
        self.run_case()

    def test_busy_unmount_detaches(self):
        self.run_case(busy=True, calls="normal lazy")

    def test_unknown_mount_is_untouched(self):
        self.run_case(current="99", expected_success=False, calls="")

    def test_stacked_mount_is_untouched(self):
        self.run_case(current="42\n99", expected_success=False, calls="")

    def test_nested_mount_is_untouched(self):
        self.run_case(children="99", expected_success=False, calls="")

    def test_failed_detach_keeps_failure(self):
        self.run_case(busy=True, lazy_fails=True, expected_success=False, calls="normal lazy")

    def test_mount_replaced_before_fallback_is_untouched(self):
        self.run_case(busy=True, change_after_failure=True, expected_success=False)


if __name__ == "__main__":
    unittest.main()
