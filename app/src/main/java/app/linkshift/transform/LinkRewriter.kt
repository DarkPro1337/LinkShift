package app.linkshift.transform

import android.net.Uri
import androidx.core.net.toUri

data class ActiveRule(val transformer: LinkTransformer, val variant: Variant)

data class RewriteResult(val text: String, val changedCount: Int)

/** Rewrites every supported URL in a text in place, leaving the rest of the text untouched. */
class LinkRewriter(
    private val rules: List<ActiveRule>,
    private val stripTracking: Boolean = true,
) {

    fun rewrite(text: String): RewriteResult {
        val out = StringBuilder(text.length)
        var cursor = 0
        var changed = 0

        for (match in URL_REGEX.findAll(text)) {
            val start = match.range.first
            val raw = trimTrailing(match.value)
            val end = start + raw.length

            out.append(text, cursor, start)
            val replacement = transformOrNull(raw)
            if (replacement != null) {
                out.append(replacement)
                changed++
            } else {
                out.append(raw)
            }
            cursor = end
        }
        out.append(text, cursor, text.length)

        return RewriteResult(out.toString(), changed)
    }

    private fun transformOrNull(raw: String): String? {
        val uri = raw.toUri()
        val rule = rules.firstOrNull { it.transformer.canHandle(uri) } ?: return null
        var result = rule.transformer.transform(uri, rule.variant)
        if (stripTracking) {
            result = keepQueryParams(result, rule.transformer.keptQueryParams)
        }
        return result.toString()
    }

    companion object {
        private val URL_REGEX = Regex("""https?://[^\s<>"'`]+""", RegexOption.IGNORE_CASE)
        private const val TRAILING_PUNCTUATION = ".,!?;:*"
        private val BRACKETS = mapOf(')' to '(', ']' to '[', '}' to '{')

        /** Drops sentence punctuation and unbalanced closing brackets that are not part of the URL. */
        internal fun trimTrailing(url: String): String {
            var end = url.length
            while (end > 0) {
                val last = url[end - 1]
                val open = BRACKETS[last]
                val drop = when {
                    last in TRAILING_PUNCTUATION -> true
                    open != null -> {
                        val body = url.substring(0, end)
                        body.count { it == last } > body.count { it == open }
                    }
                    else -> false
                }
                if (!drop) break
                end--
            }
            return url.substring(0, end)
        }

        /** Keeps only [keep] query parameters, preserving their original encoding and order. */
        internal fun keepQueryParams(uri: Uri, keep: Set<String>): Uri {
            val query = uri.encodedQuery ?: return uri
            val kept = query.split('&').filter { pair ->
                pair.isNotEmpty() && Uri.decode(pair.substringBefore('=')) in keep
            }
            return uri.buildUpon()
                .encodedQuery(kept.joinToString("&").ifEmpty { null })
                .build()
        }
    }
}
