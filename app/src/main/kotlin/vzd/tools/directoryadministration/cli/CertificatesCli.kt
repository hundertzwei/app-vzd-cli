package vzd.tools.directoryadministration.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import mu.KotlinLogging
import vzd.tools.directoryadministration.Client
import vzd.tools.directoryadministration.UserCertificate

private val logger = KotlinLogging.logger {}

val printerSerializersModule = SerializersModule {
    contextual(CertificateDataDERPrinterSerializer)
}
val CertificateOutputMapping = mapOf(
    "yaml" to { value: List<UserCertificate>?, showCert: Boolean -> printYaml(value, showCert) },
    "json" to { value: List<UserCertificate>?, showCert: Boolean -> printJson(value, showCert) },
    "list" to { value: List<UserCertificate>?, _: Boolean ->
        value?.forEach {
            val base64str = it.userCertificate?.base64String
            val cert = if (base64str != null) CertificateDataDERSurrogate.Factory.convert(base64str) else null
            println("${it.dn?.uid} ${it.telematikID} ${it.entryType} ${cert?.publicKeyAlgorithm} ${it.description}")
        }
    },
)

class ListCertificates: CliktCommand(name = "list-cert", help="List certificates") {
    private val output by option(help="How the entries should be displayed").choice(*CertificateOutputMapping.keys.toTypedArray()).default("list")
    private val showRawCert by option("--cert-raw", help="Show raw certificate data instead of text summary").flag()
    private val params: Map<String, String> by option("-Q", "--query").associate()
    private val client by requireObject<Client>();

    override fun run() {
        if (params.isEmpty()) {
            throw UsageError("Specify at least one query parameter")
        }

        val result = runBlocking { client.readDirectoryCertificates(params) }

        CertificateOutputMapping.get(output)?.invoke(result, showRawCert)
    }
}

class DeleteCertificates: CliktCommand(name = "delete-cert", help="Delete certificates") {
    override fun run() {
        TODO("Not yet implemented")
    }
}