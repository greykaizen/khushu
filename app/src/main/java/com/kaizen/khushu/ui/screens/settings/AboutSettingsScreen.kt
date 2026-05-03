package com.kaizen.khushu.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaizen.khushu.BuildConfig

private const val KHUSHU_REPO_URL = "https://github.com/greykaizen/khushu"
private const val KHUSHU_ISSUES_URL = "https://github.com/greykaizen/khushu/issues/new/choose"
private const val BTC_ADDRESS = "bc1q04zs40e9cakxuu2lw3r04jc2vmmv084rvz5kx9"
private const val ETH_BASE_ADDRESS = "0x139aB14D67B1E0dAaEDe1CF5e3234B1Cc3644BE0"
private const val USDT_TRON_ADDRESS = "TDmZvrFGBKyP9opEL1FCzpvH9MXBmJ9r6q"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    fun openUrl(url: String) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    fun copyAddress(label: String, address: String) {
        clipboardManager.setText(AnnotatedString(address))
        Toast.makeText(context, "$label address copied", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { SettingsTopBarTitle("About Khushu", scrollBehavior) },
                navigationIcon = { SettingsBackButton(onBack) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            SectionHeader("Story")
            Text(
                text = "Khushu is being built as a calmer kind of Muslim app: one that helps you return to salah, dhikr, and learning without turning worship into noise. The goal is not feature bloat. The goal is focus, clarity, and a product that feels intentional every time you open it.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
//            Text(
//                text = "This first version is the foundation. Prayer times, reminders, reading, and reflection are being shaped into something more disciplined and more beautiful over time. If you see rough edges, that is part of building in public and tightening the product with real use.",
//                style = MaterialTheme.typography.bodyLarge,
//                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
//            )

            SectionHeader("Links")
            MenuSectionItem(
                title = "GitHub Repository",
                detail = "View source, releases and project progress",
                imageVector = Icons.Default.Code,
                onClick = { openUrl(KHUSHU_REPO_URL) }
            )
            MenuSectionItem(
                title = "Project Page",
                detail = "Open the public GitHub project page",
                imageVector = Icons.Default.Language,
                onClick = { openUrl(KHUSHU_REPO_URL) }
            )

            SectionHeader("Support")
            MenuSectionItem(
                title = "Report a Bug / Issue",
                detail = "Open GitHub issues and report what broke",
                imageVector = Icons.Default.BugReport,
                onClick = { openUrl(KHUSHU_ISSUES_URL) }
            )
            MenuSectionItem(
                title = "Contribute to Development",
                detail = "Follow project progress and contribute on GitHub",
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                onClick = { openUrl(KHUSHU_REPO_URL) }
            )

            SettingsSectionCard(
                title = "Support Khushu",
                subtitle = "If Khushu has been useful to you, you can help keep it private, ad-free, and improving."
            ) {
                DonationAddressRow(
                    label = "Bitcoin",
                    address = BTC_ADDRESS,
                    onCopy = { copyAddress("Bitcoin", BTC_ADDRESS) }
                )
                DonationAddressRow(
                    label = "Ethereum / Base",
                    address = ETH_BASE_ADDRESS,
                    onCopy = { copyAddress("Ethereum / Base", ETH_BASE_ADDRESS) }
                )
                DonationAddressRow(
                    label = "USDT (TRON)",
                    address = USDT_TRON_ADDRESS,
                    onCopy = { copyAddress("USDT (TRON)", USDT_TRON_ADDRESS) }
                )
            }

            FilledTonalButton(
                onClick = { openUrl(KHUSHU_ISSUES_URL) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Report an Issue",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Text(
                text = "Version ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.52f),
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}

@Composable
private fun DonationAddressRow(
    label: String,
    address: String,
    onCopy: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy $label address"
                    )
                }
            }
            Text(
                text = address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
