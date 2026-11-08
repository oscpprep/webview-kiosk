"""Static regression checks for the current, minimal CI workflow setup.

Uses only the standard library so these checks run before Gradle or pnpm.
"""

import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
TESTS = (ROOT / ".github/workflows/tests.yaml").read_text(encoding="utf-8")
DEBUG_RELEASE = (ROOT / ".github/workflows/debug-release.yml").read_text(encoding="utf-8")


class CiCachingTest(unittest.TestCase):
    def test_gradle_cache_is_enabled_without_competing_java_cache(self):
        self.assertEqual(1, TESTS.count("uses: gradle/actions/setup-gradle@v6"))
        self.assertNotIn("cache: gradle", TESTS)
        self.assertIn("cache-read-only:", TESTS)
        self.assertIn("head.repo.full_name != github.repository", TESTS)
        self.assertIn(
            "run: sh gradlew :adblock:test :app:testDebugUnitTest "
            "--build-cache --profile --stacktrace",
            TESTS,
        )
        self.assertEqual(1, TESTS.count("build/reports/profile/"))

    def test_no_avd_or_installed_apk_automated_jobs_remain(self):
        self.assertNotIn("  emulator:", TESTS)
        self.assertNotIn("reactivecircus/android-emulator-runner", TESTS)
        self.assertNotIn("connectedDebugAndroidTest", TESTS)
        self.assertNotIn("avd-cache", TESTS)
        self.assertIn("  android:\\n", TESTS)

    def test_old_instrumentation_suite_stays_retired_and_r8_probes_are_opt_in(self):
        self.assertTrue((ROOT / "app/src/androidTest/java/uk/nktnet/webviewkiosk/r8/R8RuntimeInstrumentation.java").is_file())
        self.assertFalse((ROOT / "scripts/smoke-release-apk.sh").exists())
        self.assertFalse((ROOT / "scripts/ci/run-android-instrumentation.sh").exists())
        self.assertFalse((ROOT / "scripts/ci/test-local.sh").exists())
        self.assertFalse((ROOT / "scripts/maintenance/update-graphics.sh").exists())
        self.assertFalse((ROOT / "scripts/ci/report_android_instrumentation.py").exists())
        gradle_build = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
        versions = (ROOT / "gradle/libs.versions.toml").read_text(encoding="utf-8")
        self.assertRegex(gradle_build, r'testInstrumentationRunner\s*=\s*"uk\.nktnet\.webviewkiosk\.r8\.R8RuntimeInstrumentation"')
        self.assertIn('testBuildType = "smokeRelease"', gradle_build)
        self.assertRegex(
            gradle_build,
            r'androidComponents\s*\{[^}]*beforeVariants\(selector\(\)'
            r'\.all\(\)\)\s*\{\s*'
            r'it\.hostTests\\[HostTestBuilder\.UNIT_TEST_TYPE\\]\?\.enable\s*=\s*true',
        )
        self.assertNotIn("android.onlyEnableUnitTestForTheTestedBuildType=false", gradle_build)
        self.assertNotIn("androidTestImplementation", gradle_build)
        self.assertNotIn("--runtime", TESTS)
        self.assertNotIn("connectedSmokeReleaseAndroidTest", TESTS)
        self.assertNotIn("androidx-test-runner", versions)
        self.assertNotIn("androidx-test-ext-junit", versions)
        self.assertNotIn("androidx-test-core", versions)

    def test_debug_release_workflow_gates_publication_on_tests(self):
        self.assertIn("uses: ./.github/workflows/tests.yaml", DEBUG_RELEASE)
        self.assertIn("needs: verify", DEBUG_RELEASE)
        self.assertIn("run: ./gradlew :app:assembleDebug", DEBUG_RELEASE)
        self.assertIn("app/build/outputs/apk/debug/app-debug.apk", DEBUG_RELEASE)
        self.assertNotIn("docs.yaml", DEBUG_RELEASE)
        self.assertNotIn("release.yaml", DEBUG_RELEASE)


if __name__ == "__main__":
    unittest.main()
