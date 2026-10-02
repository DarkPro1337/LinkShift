package app.linkshift.transform.services

import app.linkshift.transform.HostSwapTransformer

object XTransformer : HostSwapTransformer(
    id = "x",
    displayName = "X / Twitter",
    sourceHosts = setOf("x.com", "twitter.com"),
    targetHosts = listOf("fixupx.com", "fxtwitter.com", "vxtwitter.com"),
)
