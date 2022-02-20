package vzd.tools.directoryadministration.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.mamoe.yamlkt.Yaml
import vzd.tools.directoryadministration.BaseDirectoryEntry
import vzd.tools.directoryadministration.UpdateBaseDirectoryEntry
import java.io.File

class ModifyBaseAttrCommand: CliktCommand(name="modify-base-attr", help="Modify specific attributes of a base entry") {
    private val logger = KotlinLogging.logger {}
    private val params: Map<String, String> by option("-p", "--param",
        help="Specify query parameters to find matching entries").associate()
    private val attrs: Map<String, String> by option("-s", "--set", metavar = "ATTR=VALUE",
        help="Set the attribute value in BaseDirectoryEntry.").associate()
    private val context by requireObject<CommandContext>()

    override fun run() = catching {

        if (params.isEmpty()) {
            throw UsageError("Please specify at least one query parameter")
        }

        val baseToUpdate: BaseDirectoryEntry? = params.let {
            val result = runBlocking { context.client.readDirectoryEntry(params) }
            if (result?.size ?: 0 > 1) {
                throw CliktError("Found too many entries: ${result?.size}. Please change your query.")
            }
            result?.first()?.directoryEntryBase
        }

        val dn = baseToUpdate?.dn

        setAttributes(baseToUpdate, attrs)

        logger.debug { "Data will to send to server: $baseToUpdate" }

        if (dn != null && baseToUpdate != null) {
            val jsonData = Json.encodeToString(baseToUpdate)
            val updateBaseDirectoryEntry = Json { ignoreUnknownKeys = true }.decodeFromString<UpdateBaseDirectoryEntry>(jsonData)
            // server bug: when updating telematikID with no certificates the exception is thrown
            updateBaseDirectoryEntry.telematikID = null
            runBlocking { context.client.modifyDirectoryEntry(dn.uid, updateBaseDirectoryEntry) }
            val result = runBlocking {  context.client.readDirectoryEntry(mapOf("uid" to dn.uid)) }

            when (context.outputFormat) {
                OutputFormat.JSON -> Output.printJson(result?.first()?.directoryEntryBase)
                OutputFormat.HUMAN, OutputFormat.YAML -> Output.printYaml(result?.first()?.directoryEntryBase)
                else -> throw UsageError("Cant load for editing in for format: ${context.outputFormat}")
            }
        }
    }
}