package klalumiere.repertoire

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.io.Closeable

object DispatchersFactory {
    private var injectedDispatcher: CoroutineDispatcher? = null
    class InjectForTests(dispatcher: CoroutineDispatcher): AutoCloseable, Closeable {
        init {
            injectedDispatcher = dispatcher
        }
        override fun close() {
            injectedDispatcher = null
        }
    }

    fun createIODispatcher(): CoroutineDispatcher {
        return injectedDispatcher ?: Dispatchers.IO
    }

    fun createDefaultDispatcher(): CoroutineDispatcher {
        return injectedDispatcher ?: Dispatchers.Default
    }
}
