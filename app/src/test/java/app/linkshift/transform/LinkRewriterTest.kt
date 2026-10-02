package app.linkshift.transform

import app.linkshift.transform.services.RedditTransformer
import app.linkshift.transform.services.TikTokTransformer
import app.linkshift.transform.services.XTransformer
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LinkRewriterTest {

    private fun rewriter(vararg transformers: LinkTransformer) =
        LinkRewriter(transformers.map { ActiveRule(it, it.variants.first()) })

    private val all = rewriter(*TransformerRegistry.all.toTypedArray())

    @Test
    fun `rewrites a single link`() {
        val result = all.rewrite("https://x.com/user/status/1")
        assertEquals("https://fixupx.com/user/status/1", result.text)
        assertEquals(1, result.changedCount)
    }

    @Test
    fun `keeps surrounding text and rewrites multiple links`() {
        val input = "Look: https://vt.tiktok.com/ZS123/ and https://twitter.com/a/status/2 lol"
        val result = all.rewrite(input)
        assertEquals(
            "Look: https://d.tnktok.com/ZS123/ and https://fixupx.com/a/status/2 lol",
            result.text,
        )
        assertEquals(2, result.changedCount)
    }

    @Test
    fun `keeps trailing punctuation and brackets outside the link`() {
        val result = all.rewrite("See (https://x.com/a/status/1). Wow, https://reddit.com/r/b!")
        assertEquals("See (https://fixupx.com/a/status/1). Wow, https://vxreddit.com/r/b!", result.text)
    }

    @Test
    fun `keeps balanced brackets inside the link`() {
        val result = all.rewrite("https://x.com/a_(b)")
        assertEquals("https://fixupx.com/a_(b)", result.text)
    }

    @Test
    fun `leaves unsupported links untouched`() {
        val input = "https://example.com/x and https://youtube.com/watch?v=1"
        val result = all.rewrite(input)
        assertEquals(input, result.text)
        assertEquals(0, result.changedCount)
    }

    @Test
    fun `skips disabled rules`() {
        val result = rewriter(TikTokTransformer, RedditTransformer)
            .rewrite("https://x.com/a https://reddit.com/r/b")
        assertEquals("https://x.com/a https://vxreddit.com/r/b", result.text)
        assertEquals(1, result.changedCount)
    }

    @Test
    fun `uses the selected variant`() {
        val variant = XTransformer.variants.first { it.id == "vxtwitter.com" }
        val result = LinkRewriter(listOf(ActiveRule(XTransformer, variant))).rewrite("https://x.com/a")
        assertEquals("https://vxtwitter.com/a", result.text)
    }

    @Test
    fun `strips tracking parameters from a real TikTok share link`() {
        val input = "https://www.tiktok.com/@bykirssten/video/7680268640876367112?sharer_language=en" +
            "&share_item_id=7680268640876367112&source=h5_m&sec_user_id=MS4wLjABAAAA8DdWFkwfNbSrxQx94S" +
            "&social_share_type=0&share_link_id=2141b952-cad6-4ae9-9b9d-c4fdb1c36f3d&share_app_id=1233" +
            "&ugbiz_name=MAIN&ug_btm=b2001&link_reflow_popup_iteration_sharer=%7B%22click_empty_to_play" +
            "%22:1,%22dynamic_cover%22:1%7D&enable_checksum=1"
        assertEquals(
            "https://d.tnktok.com/@bykirssten/video/7680268640876367112",
            all.rewrite(input).text,
        )
    }

    @Test
    fun `keeps meaningful parameters and fragment`() {
        assertEquals(
            "https://oginstagram.com/p/abc/?img_index=3",
            all.rewrite("https://www.instagram.com/p/abc/?igsh=xyz&img_index=3&utm_source=ig").text,
        )
        assertEquals(
            "https://vxreddit.com/r/a/comments/b/c/d/?context=3#top",
            all.rewrite("https://www.reddit.com/r/a/comments/b/c/d/?share_id=q&context=3#top").text,
        )
    }

    @Test
    fun `keeps query untouched when cleanup is disabled`() {
        val rewriter = LinkRewriter(listOf(ActiveRule(XTransformer, XTransformer.variants.first())), stripTracking = false)
        assertEquals("https://fixupx.com/a/status/1?s=20&t=abc", rewriter.rewrite("https://x.com/a/status/1?s=20&t=abc").text)
    }

    @Test
    fun `handles text without links`() {
        val result = all.rewrite("just text")
        assertEquals("just text", result.text)
        assertEquals(0, result.changedCount)
    }

    @Test
    fun `trimTrailing drops punctuation and unbalanced brackets only`() {
        assertEquals("https://a.com/x", LinkRewriter.trimTrailing("https://a.com/x)."))
        assertEquals("https://a.com/(x)", LinkRewriter.trimTrailing("https://a.com/(x)"))
        assertEquals("https://a.com/x", LinkRewriter.trimTrailing("https://a.com/x?!"))
    }
}
