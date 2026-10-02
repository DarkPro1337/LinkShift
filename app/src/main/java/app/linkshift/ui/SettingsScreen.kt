package app.linkshift.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import app.linkshift.R
import app.linkshift.settings.Settings
import app.linkshift.settings.ShareAction
import app.linkshift.transform.ActiveRule
import app.linkshift.transform.LinkRewriter
import app.linkshift.transform.LinkTransformer
import app.linkshift.transform.TransformerRegistry
import app.linkshift.transform.Variant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: Settings,
    transformers: List<LinkTransformer> = TransformerRegistry.all,
) {
    var defaultAction by remember { mutableStateOf(settings.defaultAction) }
    var stripTracking by remember { mutableStateOf(settings.stripTracking) }
    val enabled = remember {
        mutableStateMapOf<String, Boolean>().apply {
            transformers.forEach { put(it.id, settings.isEnabled(it)) }
        }
    }
    val variants = remember {
        mutableStateMapOf<String, Variant>().apply {
            transformers.forEach { put(it.id, settings.variant(it)) }
        }
    }
    var sample by rememberSaveable { mutableStateOf("") }

    val rewriter = LinkRewriter(
        transformers
            .filter { enabled[it.id] == true }
            .map { ActiveRule(it, variants.getValue(it.id)) },
        stripTracking = stripTracking,
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.how_to_use),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item { SectionTitle(stringResource(R.string.section_default_action)) }
            item {
                DefaultActionSelector(defaultAction) {
                    defaultAction = it
                    settings.defaultAction = it
                }
            }
            item {
                SwitchRow(
                    title = stringResource(R.string.strip_tracking_title),
                    summary = stringResource(R.string.strip_tracking_summary),
                    checked = stripTracking,
                    onCheckedChange = {
                        stripTracking = it
                        settings.stripTracking = it
                    },
                )
            }

            item { SectionTitle(stringResource(R.string.section_try_it)) }
            item { TryIt(sample, onSampleChange = { sample = it }, rewriter = rewriter) }

            item { SectionTitle(stringResource(R.string.section_services)) }
            items(transformers, key = { it.id }) { transformer ->
                ServiceCard(
                    transformer = transformer,
                    enabled = enabled[transformer.id] == true,
                    selected = variants.getValue(transformer.id),
                    onEnabledChange = {
                        enabled[transformer.id] = it
                        settings.setEnabled(transformer, it)
                    },
                    onVariantChange = {
                        variants[transformer.id] = it
                        settings.setVariant(transformer, it)
                    },
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun DefaultActionSelector(selected: ShareAction, onSelect: (ShareAction) -> Unit) {
    val options = listOf(
        ShareAction.SHARE to stringResource(R.string.action_share),
        ShareAction.COPY to stringResource(R.string.action_copy),
    )
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (action, label) ->
            SegmentedButton(
                selected = action == selected,
                onClick = { onSelect(action) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) { Text(label) }
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, onValueChange = onCheckedChange, role = Role.Switch),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun TryIt(sample: String, onSampleChange: (String) -> Unit, rewriter: LinkRewriter) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = sample,
            onValueChange = onSampleChange,
            label = { Text(stringResource(R.string.try_it_hint)) },
            modifier = Modifier.fillMaxWidth(),
        )
        if (sample.isNotBlank()) {
            val result = rewriter.rewrite(sample)
            Text(
                text = stringResource(R.string.try_it_result),
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = if (result.changedCount > 0) result.text else stringResource(R.string.try_it_no_changes),
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ServiceCard(
    transformer: LinkTransformer,
    enabled: Boolean,
    selected: Variant,
    onEnabledChange: (Boolean) -> Unit,
    onVariantChange: (Variant) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(transformer.displayName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = transformer.describe(selected),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Switch(checked = enabled, onCheckedChange = onEnabledChange)
            }

            if (enabled && transformer.variants.size > 1) {
                Column(modifier = Modifier.padding(top = 8.dp).selectableGroup()) {
                    transformer.variants.forEach { variant ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = variant == selected,
                                    onClick = { onVariantChange(variant) },
                                    role = Role.RadioButton,
                                ),
                        ) {
                            RadioButton(selected = variant == selected, onClick = null)
                            Spacer(Modifier.width(8.dp))
                            Text(variant.label)
                        }
                    }
                }
            }
        }
    }
}
