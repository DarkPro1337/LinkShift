package app.linkshift.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import app.linkshift.transform.ActiveRule
import app.linkshift.transform.LinkRewriter
import app.linkshift.transform.LinkTransformer
import app.linkshift.transform.TransformerRegistry
import app.linkshift.transform.Variant

enum class ShareAction { SHARE, COPY }

class Settings(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var defaultAction: ShareAction
        get() = prefs.getString(KEY_DEFAULT_ACTION, null)
            ?.let { name -> ShareAction.entries.firstOrNull { it.name == name } }
            ?: ShareAction.SHARE
        set(value) = prefs.edit { putString(KEY_DEFAULT_ACTION, value.name) }

    var stripTracking: Boolean
        get() = prefs.getBoolean(KEY_STRIP_TRACKING, true)
        set(value) = prefs.edit { putBoolean(KEY_STRIP_TRACKING, value) }

    fun isEnabled(transformer: LinkTransformer): Boolean =
        prefs.getBoolean(enabledKey(transformer), true)

    fun setEnabled(transformer: LinkTransformer, enabled: Boolean) =
        prefs.edit { putBoolean(enabledKey(transformer), enabled) }

    /** Falls back to the default variant if the stored one no longer exists. */
    fun variant(transformer: LinkTransformer): Variant {
        val id = prefs.getString(variantKey(transformer), null)
        return transformer.variants.firstOrNull { it.id == id } ?: transformer.variants.first()
    }

    fun setVariant(transformer: LinkTransformer, variant: Variant) =
        prefs.edit { putString(variantKey(transformer), variant.id) }

    fun activeRules(transformers: List<LinkTransformer> = TransformerRegistry.all): List<ActiveRule> =
        transformers.filter(::isEnabled).map { ActiveRule(it, variant(it)) }

    fun rewriter(): LinkRewriter = LinkRewriter(activeRules(), stripTracking)

    private fun enabledKey(t: LinkTransformer) = "enabled_${t.id}"
    private fun variantKey(t: LinkTransformer) = "variant_${t.id}"

    private companion object {
        const val KEY_DEFAULT_ACTION = "default_action"
        const val KEY_STRIP_TRACKING = "strip_tracking"
    }
}
