package com.example.repertoire

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EndToEndTest {
    private val assetUri = Uri.parse("file:///android_asset/Happy%20Birthday.md")
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun canAddSong() {
        val scenario = launchActivity<MainActivity>()

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

    @Test
    fun canSelectSong() {
        val scenario = launchActivity<MainActivity>()
        addSong(scenario)

        onView(withId(R.id.song_list_view))
            .perform(actionOnItemAtPosition<SongViewHolder>(0, longClick()))

        onView(withId(R.id.song_list_view))
            .check(matches(atPosition(0, isActivated())))
    }

    @Test
    fun canDeleteSong() {
        val scenario = launchActivity<MainActivity>()
        addSong(scenario)

        onView(withId(R.id.song_list_view))
            .perform(actionOnItemAtPosition<SongViewHolder>(0, longClick()))
        pressDeleteInOptionMenu()

        onView(withId(R.id.song_list_view)).check(matches(isEmpty()))
    }

    @Test
    fun deletedAndAddedIsNotSelected() {
        val scenario = launchActivity<MainActivity>()
        addSong(scenario)

        onView(withId(R.id.song_list_view))
            .perform(actionOnItemAtPosition<SongViewHolder>(0, longClick()))
        pressDeleteInOptionMenu()
        addSong(scenario)

        onView(withId(R.id.song_list_view))
            .check(matches(atPosition(0, not(isActivated()))))
    }


    @Before
    fun clearDatabase() {
        AppDatabase.getInstance(context).songDao().deleteAll()
    }

    private fun addSong(scenario: ActivityScenario<MainActivity>) {
        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.onActivity { activity ->
            activity.songViewModel.repository.injectContentResolverForTests(
                AssetContentResolver(context))
            activity.injectActivityResultRegistryForTest(
                createFakeActivityResultRegistry(assetUri))
        }
        scenario.moveToState(Lifecycle.State.RESUMED)
        onView(withId(R.id.addSongsFAB)).perform(click())
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

    private fun pressDeleteInOptionMenu() {
        openActionBarOverflowOrOptionsMenu(context)
        onView(withText("Delete")).perform(click())
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

private fun isActivated(): Matcher<View?> {
    return object : BoundedMatcher<View?, View>(View::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("is activated.")
        }

        override fun matchesSafely(view: View): Boolean {
            return view.isActivated
        }
    }
}

private fun isEmpty(): Matcher<View?> {
    return object : BoundedMatcher<View?, RecyclerView>(RecyclerView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("is empty.")
        }

        override fun matchesSafely(view: RecyclerView): Boolean {
            return view.findViewHolderForAdapterPosition(0) == null
        }
    }
}
