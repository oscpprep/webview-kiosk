package uk.nktnet.webviewkiosk.config.remote.outbound

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import uk.nktnet.webviewkiosk.config.data.SystemInfo
import uk.nktnet.webviewkiosk.utils.BaseJson
import uk.nktnet.webviewkiosk.utils.WebviewKioskStatus

@Serializable
sealed interface OutboundResponseMessage {
    val messageId: String
    val username: String
    val appInstanceId: String
    val requestMessageId: String?
    val correlationData: String?

    fun getType(): String
}

@Serializable
@SerialName("get_status")
data class OutboundStatusResponse(
    override val messageId: String,
    override val username: String,
    override val appInstanceId: String,
    override val requestMessageId: String?,
    override val correlationData: String?,
    val data: WebviewKioskStatus,
) : OutboundResponseMessage {
    override fun getType(): String = "status"
}

@Serializable
@SerialName("get_settings")
data class OutboundSettingsResponse(
    override var messageId: String,
    override val username: String,
    override val appInstanceId: String,
    override val requestMessageId: String?,
    override val correlationData: String?,
    val data: SettingsResponseData,
) : OutboundResponseMessage {
    override fun getType(): String = "settings"

    @Serializable data class SettingsResponseData(val settings: JsonObject)
}

@Serializable
@SerialName("get_system_info")
data class OutboundSystemInfoResponse(
    override var messageId: String,
    override val username: String,
    override val appInstanceId: String,
    override val requestMessageId: String?,
    override val correlationData: String?,
    val data: SystemInfo,
) : OutboundResponseMessage {
    override fun getType(): String = "system_info"
}

@Serializable
@SerialName("get_launchable_packages")
data class OutboundLaunchablePackagesResponse(
    override var messageId: String,
    override val username: String,
    override val appInstanceId: String,
    override val requestMessageId: String?,
    override val correlationData: String?,
    val data: Data,
) : OutboundResponseMessage {
    override fun getType(): String = "get_launchable_packages"

    @Serializable data class Data(val packages: List<String>)
}

@Serializable
@SerialName("get_lock_task_packages")
data class OutboundLockTaskPackagesResponse(
    override var messageId: String,
    override val username: String,
    override val appInstanceId: String,
    override val requestMessageId: String?,
    override val correlationData: String?,
    val data: Data,
) : OutboundResponseMessage {
    override fun getType(): String = "get_lock_task_packages"

    @Serializable data class Data(val packages: List<String>)
}

@Serializable
@SerialName("install_local_file")
data class OutboundInstallLocalFileResponse(
    override var messageId: String,
    override val username: String,
    override val appInstanceId: String,
    override val requestMessageId: String?,
    override val correlationData: String?,
    val data: Data,
) : OutboundResponseMessage {
    override fun getType(): String = "install_local_file"

    @Serializable
    data class Data(
        val type: String = "install_local_file",
        val ok: Boolean,
        val filename: String? = null,
        val url: String? = null,
        val setAsHome: Boolean? = null,
        val error: String? = null,
    )
}

@Serializable
@SerialName("local_file_info")
data class OutboundLocalFileInfoResponse(
    override var messageId: String,
    override val username: String,
    override val appInstanceId: String,
    override val requestMessageId: String?,
    override val correlationData: String?,
    val data: Data,
) : OutboundResponseMessage {
    override fun getType(): String = "local_file_info"

    @Serializable
    data class Data(
        val type: String = "local_file_info",
        val files: List<FileInfo>,
        val homeUrl: String,
    )

    @Serializable
    data class FileInfo(
        val filename: String,
        val size: Long,
        val modified: Long,
        val url: String,
    )
}

@Serializable
@SerialName("error")
data class OutboundErrorResponse(
    override var messageId: String,
    override val username: String,
    override val appInstanceId: String,
    override val requestMessageId: String?,
    override val correlationData: String?,
    val payloadStr: String,
    val errorMessage: String,
) : OutboundResponseMessage {
    override fun getType(): String = "error"
}

val OutboundResponseJsonParser =
    Json(BaseJson) {
        classDiscriminator = "responseType"
    }
