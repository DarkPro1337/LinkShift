# LinkShift

A tiny Android app that fixes links before you send them to Telegram, Discord or wherever.

You know the deal: you send a friend a TikTok or Instagram link and they get a bare URL with no preview. There are services like `tnktok.com` or `fixupx.com` that fix this if you swap the domain. LinkShift does the swap for you, so you don't have to edit links by hand.

## How to use it

1. Hit Share in any app.
2. Pick LinkShift.
3. That's it. Depending on your settings, the Share menu either opens again with the fixed link, or the link just gets copied to the clipboard.

There are three entries in the Share menu:

- **Fix link** does whatever you picked as the default in settings;
- **Fix and share** always opens Share again;
- **Fix and copy** always copies.

If the text has several links, all of them get fixed and the rest of the text stays as is.

## What it fixes

| Service | From | To (first one is the default) |
|---|---|---|
| TikTok | `tiktok.com`, `vt.tiktok.com`, `vm.tiktok.com` | `d.tnktok.com`, `tnktok.com`, `kktiktok.com` |
| X / Twitter | `x.com`, `twitter.com` | `fixupx.com`, `fxtwitter.com`, `vxtwitter.com` |
| Instagram | `instagram.com` | `oginstagram.com`, `kkinstagram.com`, `uuinstagram.com` |
| Reddit | `reddit.com`, `old.reddit.com` | `vxreddit.com` |

You can turn each service off or pick a different option for it in settings.

It also strips the junk after `?`: `share_id`, `utm_source`, `igsh` and friends. Useful params stay, like Instagram's `img_index` so the right picture in a carousel opens. If that gets in the way, you can turn it off in settings.

There's also a "Try it" field in settings: paste some text and see what comes out.

## What it doesn't do

No background services, no Accessibility, no clipboard snooping. The app only wakes up when you share something through it, and closes right after.

## About the mirror services

Services like `ddinstagram` or `vxtiktok` die every now and then: Instagram blocks them, lawyers send a letter, or the author just gets tired of paying for servers. If an option stops showing previews, pick another one in settings. If they're all dead, the list needs updating in code, which is one line (see below).

## Installing

### Just install it

Grab `app-release.apk`, open it on your phone and allow installs from that source (Telegram or your file manager). If Play Protect complains about an unknown app, tap "More details" → "Install anyway".

Needs Android 8.0 or newer.

### Build it yourself

You'll need Android Studio. On first open it asks which JVM to use, pick 21.

- **Run on your phone:** turn on USB or Wi-Fi debugging, pick the phone in the device list and hit Run.
- **Build an APK for friends:** Build → Generate Signed App Bundle / APK → APK → release. Keep the keystore and password somewhere safe. Without them, updates won't install over the old version, and people will have to uninstall first.
- **Tests:**

  ```bash
  ./gradlew test
  ```

## Adding a new service

Each service is a small class in `app/src/main/java/app/linkshift/transform/services/`. For example, Bluesky:

```kotlin
object BlueskyTransformer : HostSwapTransformer(
    id = "bluesky",
    displayName = "Bluesky",
    sourceHosts = setOf("bsky.app"),
    targetHosts = listOf("fxbsky.app"),
)
```

Then add it to `TransformerRegistry.all` with one line, and you're done. It shows up in the UI on its own.

A few things to know:

- `sourceHosts` covers subdomains too, so `tiktok.com` also catches `www.`, `vt.` and `m.`;
- the first domain in `targetHosts` is the default option;
- if the service has query params that must survive the cleanup, list them in `keptQueryParams`;
- better not to change `id` later, settings are tied to it.

If swapping the domain isn't enough, implement `LinkTransformer` directly and do whatever you want with the link.

## Stack

Kotlin, Jetpack Compose, Material 3. Only androidx as dependencies, tests on JUnit and Robolectric. Settings live in plain `SharedPreferences`.
