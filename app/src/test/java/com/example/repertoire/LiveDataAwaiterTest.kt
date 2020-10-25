package com.example.repertoire

import android.os.Looper.getMainLooper
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [28]) // >= 29 not supported by Android Studio right now
class LiveDataAwaiterTest {
    @Test
    fun getOrAwaitValue() {
        val data = MutableLiveData<Int>()
        val awaiter = LiveDataAwaiter(data)
        data.postValue(5)
        executeQueuedRunnables()
        assertEquals(5, awaiter.getOrAwaitValue())
    }

    
    private fun executeQueuedRunnables() {
        shadowOf(getMainLooper()).idle()
    }
}
