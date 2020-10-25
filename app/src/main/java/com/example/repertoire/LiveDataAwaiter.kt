package com.example.repertoire

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class LiveDataAwaiter <T> (private val liveData: LiveData<T>) {
    init {
        val observer = object : Observer<T> {
            override fun onChanged(x: T?) {
                data = x
                latch.countDown()
                liveData.removeObserver(this)
            }
        }
        liveData.observeForever(observer)
    }

    fun getOrAwaitValue(time: Long = 2, timeUnit: TimeUnit = TimeUnit.SECONDS): T?
    {
        if (latch.await(time, timeUnit)) return data
        else throw TimeoutException("LiveData value was never set.")
    }

    private var data: T? = null
    val latch = CountDownLatch(1)
}
