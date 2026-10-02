package app.linkshift.transform.services

import app.linkshift.transform.HostSwapTransformer

object InstagramTransformer : HostSwapTransformer(
    id = "instagram",
    displayName = "Instagram",
    sourceHosts = setOf("instagram.com"),
    targetHosts = listOf("oginstagram.com", "kkinstagram.com", "uuinstagram.com"),
    keptQueryParams = setOf("img_index"),
)
