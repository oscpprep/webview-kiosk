"""Check the current workflow publishes a stable debug APK release asset."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
RELEASE = (ROOT / ".github/workflows/debug-release.yml").read_text(encoding="utf-8")


class ReleaseArtifactsTest(unittest.TestCase):
    def test_builds_debug_variant(self):
        self.assertIn("run: ./gradlew :app:assembleDebug", RELEASE)

    def test_debug_apk_is_published_with_stock_filename(self):
        self.assertIn("name: Publish permanent latest APK download", RELEASE)
        self.assertIn("uses: softprops/action-gh-release@v2", RELEASE)
        self.assertIn("tag_name: debug-latest", RELEASE)
        self.assertIn("files: app/build/outputs/apk/debug/app-debug.apk", RELEASE)
        self.assertIn("fail_on_unmatched_files: true", RELEASE)

    def test_publication_waits_for_tests_and_uses_writable_contents_permission(self):
        self.assertIn("uses: ./.github/workflows/tests.yaml", RELEASE)
        self.assertIn("needs: verify", RELEASE)
        self.assertIn("contents: write", RELEASE)

    def test_release_action_creates_or_updates_stable_tag_at_current_commit(self):
        self.assertIn("tag_name: debug-latest", RELEASE)
        self.assertIn("target_commitish: ${{ github.sha }}", RELEASE)
        self.assertIn("uses: softprops/action-gh-release@v2", RELEASE)


if __name__ == "__main__":
    unittest.main()
