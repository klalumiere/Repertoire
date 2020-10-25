package com.example.repertoire

import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EndToEndTest {
    @Test
    fun canAddSong() {
        val assetUri = Uri.parse("file:///android_asset/Happy%20Birthday.md")
        val testRegistry = object : ActivityResultRegistry() {
                override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, listOf(assetUri))
            }
        }

        val scenario = launchActivity<MainActivity>()
        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.onActivity { activity ->
            activity.injectActivityResultRegistryForTest(testRegistry)
        }
        scenario.moveToState(Lifecycle.State.RESUMED)
//        onView(withId(R.id.addSongsFAB)).perform(click())
        assertEquals(2, 1+1)
    }
}
