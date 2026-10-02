package app.linkshift.share

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import app.linkshift.R
import app.linkshift.settings.Settings
import app.linkshift.settings.ShareAction

/** Invisible share target: rewrites links in the shared text, re-shares or copies it, then finishes. */
class ShareReceiverActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handle(intent)
        finish()
    }

    private fun handle(intent: Intent) {
        val text = intent.takeIf { it.action == Intent.ACTION_SEND }
            ?.getCharSequenceExtra(Intent.EXTRA_TEXT)
            ?.toString()
        if (text.isNullOrBlank()) {
            toast(R.string.toast_no_links)
            return
        }

        val settings = Settings(this)
        val result = settings.rewriter().rewrite(text)
        if (result.changedCount == 0) {
            toast(R.string.toast_no_links)
            return
        }

        when (actionFor(intent, settings)) {
            ShareAction.SHARE -> share(result.text, intent.getStringExtra(Intent.EXTRA_SUBJECT))
            ShareAction.COPY -> copy(result.text)
        }
    }

    private fun actionFor(intent: Intent, settings: Settings): ShareAction =
        when (intent.component?.className) {
            ALIAS_SHARE -> ShareAction.SHARE
            ALIAS_COPY -> ShareAction.COPY
            else -> settings.defaultAction
        }

    private fun share(text: String, subject: String?) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
        }
        val ownTargets = listOf(javaClass.name, ALIAS_SHARE, ALIAS_COPY)
            .map { ComponentName(this, it) }
            .toTypedArray()
        val chooser = Intent.createChooser(send, getString(R.string.chooser_title))
            .putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, ownTargets)
        startActivity(chooser)
    }

    private fun copy(text: String) {
        val clipboard = getSystemService(ClipboardManager::class.java)
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.app_name), text))
        // Android 13+ shows its own clipboard confirmation.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            toast(R.string.toast_copied)
        }
    }

    private fun toast(resId: Int) {
        Toast.makeText(applicationContext, resId, Toast.LENGTH_SHORT).show()
    }

    private companion object {
        const val ALIAS_SHARE = "app.linkshift.share.FixAndShare"
        const val ALIAS_COPY = "app.linkshift.share.FixAndCopy"
    }
}
