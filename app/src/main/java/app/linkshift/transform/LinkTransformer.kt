package app.linkshift.transform

import android.net.Uri

data class Variant(val id: String, val label: String)

interface LinkTransformer {
    /** Stable key used for persisted settings. Never rename once released. */
    val id: String
    val displayName: String

    /** Available rewrite targets; the first one is the default. */
    val variants: List<Variant>

    /** Query parameters that carry meaning; everything else is dropped when tracking cleanup is on. */
    val keptQueryParams: Set<String> get() = emptySet()

    /** Short human-readable description of what gets rewritten, shown in the UI. */
    fun describe(variant: Variant): String

    fun canHandle(uri: Uri): Boolean
    fun transform(uri: Uri, variant: Variant = variants.first()): Uri
}

/**
 * Replaces the host of matching links and keeps path, query and fragment.
 *
 * A source host matches itself and any of its subdomains, so `tiktok.com`
 * also covers `www.tiktok.com`, `vt.tiktok.com` and `m.tiktok.com`.
 * Variant ids are the target hosts themselves.
 */
abstract class HostSwapTransformer(
    final override val id: String,
    final override val displayName: String,
    private val sourceHosts: Set<String>,
    targetHosts: List<String>,
    final override val keptQueryParams: Set<String> = emptySet(),
) : LinkTransformer {

    init {
        require(targetHosts.isNotEmpty()) { "$id: at least one target host is required" }
    }

    final override val variants: List<Variant> = targetHosts.map { Variant(id = it, label = it) }

    override fun describe(variant: Variant): String =
        "${sourceHosts.joinToString(", ")} -> ${variant.id}"

    override fun canHandle(uri: Uri): Boolean {
        val scheme = uri.scheme?.lowercase() ?: return false
        if (scheme != "http" && scheme != "https") return false
        val host = uri.host?.lowercase() ?: return false
        return sourceHosts.any { host == it || host.endsWith(".$it") }
    }

    override fun transform(uri: Uri, variant: Variant): Uri =
        uri.buildUpon()
            .scheme("https")
            .encodedAuthority(variant.id)
            .build()
}
