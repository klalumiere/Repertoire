package klalumiere.repertoire

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class LiveDataGetOrAwaitValueTest {
    @Test
    fun getOrAwaitValue() {
        val data = MutableLiveData<Int>()
        data.postValue(5)
        assertEquals(5, data.getOrAwaitValue())
    }


    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()
}
