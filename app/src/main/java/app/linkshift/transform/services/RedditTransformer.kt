package app.linkshift.transform.services

import app.linkshift.transform.HostSwapTransformer

object RedditTransformer : HostSwapTransformer(
    id = "reddit",
    displayName = "Reddit",
    sourceHosts = setOf("reddit.com"),
    targetHosts = listOf("vxreddit.com"),
    keptQueryParams = setOf("context"),
)
