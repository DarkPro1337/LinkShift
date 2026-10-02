package app.linkshift.transform.services

import app.linkshift.transform.HostSwapTransformer

object TikTokTransformer : HostSwapTransformer(
    id = "tiktok",
    displayName = "TikTok",
    sourceHosts = setOf("tiktok.com"),
    targetHosts = listOf("d.tnktok.com", "tnktok.com", "kktiktok.com"),
)
