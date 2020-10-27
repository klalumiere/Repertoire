package com.example.repertoire

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EndToEndTest {
    @Test
    fun canAddSong() {
        val assetUri = Uri.parse("file:///android_asset/Happy%20Birthday.md")
        val context = ApplicationProvider.getApplicationContext<Context>()
        val scenario = launchActivity<MainActivity>()

        AppDatabase.getInstance(context).songDao().deleteAll()
        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.onActivity { activity ->
            activity.songViewModel.repository.injectContentResolverForTests(
                AssetContentResolver(context))
            activity.injectActivityResultRegistryForTest(
                createFakeActivityResultRegistry(assetUri))
        }
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.addSongsFAB)).perform(click())

        onView(withId(R.id.song_list_view))
            .check(matches(atPosition(0, withText("Happy Birthday"))))
    }
}


private fun createFakeActivityResultRegistry(assetUri: Uri): ActivityResultRegistry {
    return object : ActivityResultRegistry() {
        override fun <I, O> onLaunch(requestCode: Int, contract: ActivityResultContract<I, O>,
            input: I, options: ActivityOptionsCompat?
        ) {
            dispatchResult(requestCode, listOf(assetUri))
        }
    }
}

private fun atPosition(position: Int, itemMatcher: Matcher<View?>): Matcher<View?>? {
    return object : BoundedMatcher<View?, RecyclerView>(RecyclerView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("has item at position $position: ")
            itemMatcher.describeTo(description)
        }

        override fun matchesSafely(view: RecyclerView): Boolean {
            val viewHolder = view.findViewHolderForAdapterPosition(position)
                ?: return false
            return itemMatcher.matches(viewHolder.itemView)
        }
    }
}
