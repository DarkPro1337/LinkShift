package app.linkshift.transform

import app.linkshift.transform.services.InstagramTransformer
import app.linkshift.transform.services.RedditTransformer
import app.linkshift.transform.services.TikTokTransformer
import app.linkshift.transform.services.XTransformer

object TransformerRegistry {
    /** Order matters: the first transformer that can handle a link wins. */
    val all: List<LinkTransformer> = listOf(
        TikTokTransformer,
        XTransformer,
        InstagramTransformer,
        RedditTransformer,
    )
}
