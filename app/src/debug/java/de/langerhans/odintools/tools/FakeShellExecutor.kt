package de.langerhans.odintools.tools

/**
 * Debug-only fake of [ShellExecutor]. Instead of transacting with the AYN PServer
 * vendor binder (which only exists on real Odin 2 firmware), it emulates a tiny shell
 * over in-memory maps. This lets the whole app run as a "virtual Odin 2" on any
 * emulator so features can actually be exercised.
 *
 * It only overrides [executeAsRoot] + [pServerAvailable]; every higher-level helper in
 * [ShellExecutor] funnels through [executeAsRoot], so they all work unchanged.
 */
class FakeShellExecutor : ShellExecutor() {

    override val pServerAvailable: Boolean = true

    private val systemSettings = mutableMapOf<String, String>()
    private val secureSettings = mutableMapOf<String, String>()
    private val files = mutableMapOf<String, String>()
    private val props = mutableMapOf(
        // Codename "Q9" => DeviceType.ODIN2, so the incompatible-device dialog stays hidden.
        SettingsRepo.KEY_VENDOR_NAME to "Q9",
        SettingsRepo.KEY_BUILD_VERSION to "1.0.0.288",
    )

    override fun executeAsRoot(cmd: String): Result<String?> {
        val trimmed = cmd.trim()

        // echo <value> > <file>
        if (trimmed.startsWith("echo ") && trimmed.contains(">")) {
            val (left, right) = trimmed.split(">", limit = 2)
            val value = left.removePrefix("echo").trim()
            val file = right.trim()
            if (file.isNotEmpty()) files[file] = value
            return Result.success(null)
        }

        val tokens = trimmed.split(Regex("\\s+"))
        return when {
            tokens[0] == "getprop" && tokens.size >= 2 ->
                Result.success(props[tokens[1]])

            tokens.size >= 4 && tokens[0] == "settings" && tokens[1] == "put" && tokens[2] == "system" -> {
                systemSettings[tokens[3]] = tokens.drop(4).joinToString(" ")
                Result.success(null)
            }
            tokens.size >= 4 && tokens[0] == "settings" && tokens[1] == "get" && tokens[2] == "system" ->
                Result.success(systemSettings[tokens[3]])

            tokens.size >= 4 && tokens[0] == "settings" && tokens[1] == "put" && tokens[2] == "secure" -> {
                secureSettings[tokens[3]] = tokens.drop(4).joinToString(" ")
                Result.success(null)
            }
            tokens.size >= 4 && tokens[0] == "settings" && tokens[1] == "get" && tokens[2] == "secure" ->
                Result.success(secureSettings[tokens[3]])

            tokens[0] == "cat" && tokens.size >= 2 ->
                Result.success(files[tokens[1]])

            // pm grant, service call, logcat, etc. — accept and no-op.
            else -> Result.success(null)
        }
    }
}
