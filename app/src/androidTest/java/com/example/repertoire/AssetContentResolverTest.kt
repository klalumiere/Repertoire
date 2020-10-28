package com.example.repertoire

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AssetContentResolverTest {
    private val assetUri = Uri.parse("file:///android_asset/Happy%20Birthday.md")
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val resolver = AssetContentResolver(context)

    @Test
    fun resolveName() {
        assertEquals("Happy Birthday.md", resolver.resolveName(assetUri))
    }

    @Test
    fun openInputStream() {
        val content = resolver.openInputStream(assetUri)?.use { stream ->
            stream.readBytes().toString(Charsets.UTF_8)
        }
        assertTrue(content?.contains("Happy") ?: false)
    }
}
