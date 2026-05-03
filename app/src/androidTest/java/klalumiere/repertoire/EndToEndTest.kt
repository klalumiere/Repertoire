package klalumiere.repertoire

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EndToEndTest {
    companion object {
        private val assetUri = Uri.parse("file:///android_asset/Happy%20Birthday.md")
    }
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var contentResolverInjector: RepertoireContentResolverFactory.InjectForTests
    private lateinit var addSongsInjector: AddSongsActivityResultRegistryFactory.InjectForTests

    @Test
    fun canAddSong() {
        launchActivity<MainActivity>().use {
            onView(withId(R.id.addSongsFAB)).perform(click())

            onView(withId(R.id.song_list_view))
                .check(matches(atPosition(0, withText("Happy Birthday"))))
        }
    }

    @Test
    fun canSelectSong() {
        launchActivity<MainActivity>().use {
            addSong()

            onView(withId(R.id.song_list_view))
                .perform(actionOnItemAtPosition<SongViewHolder>(0, longClickWithInputSource()))

            onView(withId(R.id.song_list_view))
                .check(matches(atPosition(0, isActivated())))
        }
    }

    @Test
    fun canDeleteSong() {
        launchActivity<MainActivity>().use {
            addSong()

            onView(withId(R.id.song_list_view))
                .perform(actionOnItemAtPosition<SongViewHolder>(0, longClickWithInputSource()))

            pressDeleteInOptionMenu()

            onView(withId(R.id.song_list_view)).check(matches(isEmpty()))
        }
    }

    @Test
    fun deletedAndAddedIsNotSelected() {
        launchActivity<MainActivity>().use {
            addSong()

            onView(withId(R.id.song_list_view))
                .perform(actionOnItemAtPosition<SongViewHolder>(0, longClickWithInputSource()))
            pressDeleteInOptionMenu()
            addSong()

            onView(withId(R.id.song_list_view))
                .check(matches(atPosition(0, not(isActivated()))))
        }
    }

    @Test
    fun turningDevicePreservesSelection() {
        launchActivity<MainActivity>().use {
            addSong()

            onView(withId(R.id.song_list_view))
                .perform(actionOnItemAtPosition<SongViewHolder>(0, longClickWithInputSource()))
            onView(isRoot()).perform(OrientationChange.landscape())

            onView(withId(R.id.song_list_view))
                .check(matches(atPosition(0, isActivated())))

            onView(isRoot()).perform(OrientationChange.portrait()) // necessary, otherwise, can make other tests fail
        }
    }

    @Test
    fun canTransitionToSongActivity() {
        launchActivity<MainActivity>().use {
            addSong()

            onView(withId(R.id.song_list_view))
                .perform(actionOnItemAtPosition<SongViewHolder>(0, click()))

            onView(withId(R.id.song_title_text_view)).check(matches(withText("Happy Birthday")))
        }
    }

    @Test
    fun canRenderSong() {
        runBlocking { SongRepository(context).add(assetUri, "Happy Birthday") }
        val intent = Intent(context, SongActivity::class.java).apply {
            putExtra(SongActivity.SONG_NAME, "Happy Birthday")
            putExtra(SongActivity.SONG_URI_AS_STRING, assetUri.toString())
        }
        launchActivity<SongActivity>(intent).use {
            onView(withId(R.id.song_title_text_view)).check(matches(withText("Happy Birthday")))
            eventually {
                onView(withId(R.id.song_text_view)).check(matches(withSubstring("Happy Birthday to You")))
            }
        }
    }


    @Before
    fun clearDatabase() {
        contentResolverInjector = RepertoireContentResolverFactory.InjectForTests(AssetContentResolver(context))
        addSongsInjector = AddSongsActivityResultRegistryFactory.InjectForTests(createFakeActivityResultRegistry(assetUri))
        AppDatabase.getInstance(context).songDao().deleteAll()
    }

    @After
    fun closeResources() {
        contentResolverInjector.close()
        addSongsInjector.close()
    }

    private fun addSong() {
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



private class OrientationChange private constructor(private val orientation: Int) : ViewAction
{
    companion object {
        fun landscape(): ViewAction {
            return OrientationChange(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        }

        fun portrait(): ViewAction {
            return OrientationChange(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }
    }

    override fun getConstraints(): Matcher<View> {
        return isRoot()
    }

    override fun getDescription(): String {
        return "change orientation to $orientation"
    }

    override fun perform(uiController: UiController, view: View) {
        uiController.loopMainThreadUntilIdle()
        val activity = convertToActivity(view)
        activity.requestedOrientation = orientation
        val resumedActivities =
            ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
        if (resumedActivities.isEmpty()) throw RuntimeException("Could not change orientation")
    }

    private fun convertToActivity(view: View): Activity {
        var activity: Activity? = convertToActivity(view.context)
        if(activity != null) return activity
        if(view is ViewGroup) {
            for(i in 0 until view.childCount) {
                activity = convertToActivity(view.getChildAt(i).context)
                if(activity != null) return activity
            }
        }
        throw RuntimeException("Couldn't convert the view to an activity")
    }

    private fun convertToActivity(inputContext: Context): Activity? {
        var context = inputContext
        while(context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
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

// recyclerview-selection 1.1.0+ filters out motion events with InputDevice.SOURCE_UNKNOWN,
// which is what espresso's built-in longClick() emits. Force SOURCE_TOUCHSCREEN.
// See https://issuetracker.google.com/issues/188992115
private fun longClickWithInputSource(): ViewAction = actionWithAssertions(
    GeneralClickAction(
        Tap.LONG,
        GeneralLocation.CENTER,
        Press.FINGER,
        InputDevice.SOURCE_TOUCHSCREEN,
        MotionEvent.BUTTON_PRIMARY
    )
)

private fun eventually(timeoutMs: Long = 10_000, intervalMs: Long = 100, block: () -> Unit) {
    val deadline = System.currentTimeMillis() + timeoutMs
    var lastError: Throwable? = null
    while (System.currentTimeMillis() < deadline) {
        try {
            block()
            return
        } catch (t: Throwable) {
            lastError = t
            Thread.sleep(intervalMs)
        }
    }
    throw lastError ?: AssertionError("eventually() timed out after ${timeoutMs}ms")
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
