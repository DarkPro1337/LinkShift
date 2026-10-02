package app.linkshift.transform

import android.net.Uri
import app.linkshift.transform.services.InstagramTransformer
import app.linkshift.transform.services.RedditTransformer
import app.linkshift.transform.services.TikTokTransformer
import app.linkshift.transform.services.XTransformer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TransformersTest {

    private fun LinkTransformer.apply(url: String): String {
        val uri = Uri.parse(url)
        assertTrue("$id should handle $url", canHandle(uri))
        return transform(uri).toString()
    }

    @Test
    fun tiktok() {
        assertEquals("https://d.tnktok.com/ZSabc/", TikTokTransformer.apply("https://vt.tiktok.com/ZSabc/"))
        assertEquals("https://d.tnktok.com/ZSabc", TikTokTransformer.apply("https://vm.tiktok.com/ZSabc"))
        assertEquals(
            "https://d.tnktok.com/@user/video/123?is_from_webapp=1",
            TikTokTransformer.apply("https://www.tiktok.com/@user/video/123?is_from_webapp=1"),
        )

        val embed = TikTokTransformer.variants.first { it.id == "tnktok.com" }
        assertEquals(
            "https://tnktok.com/ZSabc/",
            TikTokTransformer.transform(Uri.parse("https://vt.tiktok.com/ZSabc/"), embed).toString(),
        )
    }

    @Test
    fun x() {
        assertEquals("https://fixupx.com/u/status/1?s=20", XTransformer.apply("https://x.com/u/status/1?s=20"))
        assertEquals("https://fixupx.com/u/status/1", XTransformer.apply("https://mobile.twitter.com/u/status/1"))
        assertEquals("https://fixupx.com/u/status/1", XTransformer.apply("http://twitter.com/u/status/1"))
    }

    @Test
    fun instagram() {
        assertEquals(
            "https://oginstagram.com/reel/abc/?igsh=xyz",
            InstagramTransformer.apply("https://www.instagram.com/reel/abc/?igsh=xyz"),
        )
    }

    @Test
    fun reddit() {
        assertEquals("https://vxreddit.com/r/a/comments/b/", RedditTransformer.apply("https://old.reddit.com/r/a/comments/b/"))
        assertEquals("https://vxreddit.com/r/a#c", RedditTransformer.apply("https://www.reddit.com/r/a#c"))
    }

    @Test
    fun `does not match lookalike or already fixed hosts`() {
        val uris = listOf(
            "https://fixupx.com/a",
            "https://vxtwitter.com/a",
            "https://d.tnktok.com/a",
            "https://tnktok.com/a",
            "https://kktiktok.com/a",
            "https://notreddit.com/a",
            "https://rxddit.com/a",
            "https://vxreddit.com/a",
            "https://kkinstagram.com/a",
            "https://uuinstagram.com/a",
            "https://oginstagram.com/a",
            "ftp://x.com/a",
        ).map(Uri::parse)
        for (uri in uris) {
            for (t in TransformerRegistry.all) {
                assertFalse("${t.id} must not handle $uri", t.canHandle(uri))
            }
        }
    }

    @Test
    fun `transformer ids are unique`() {
        val ids = TransformerRegistry.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }
}
