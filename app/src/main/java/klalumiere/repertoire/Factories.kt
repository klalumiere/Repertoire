package klalumiere.repertoire

import android.content.Context
import androidx.activity.result.ActivityResultRegistry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.io.Closeable

object DispatchersFactory {
    private var injected: CoroutineDispatcher? = null
    class InjectForTests(rhs: CoroutineDispatcher) : AutoCloseable, Closeable {
        init {
            injected = rhs
        }

        override fun close() {
            injected = null
        }
    }

    fun createIODispatcher(): CoroutineDispatcher {
        return injected ?: Dispatchers.IO
    }

    fun createDefaultDispatcher(): CoroutineDispatcher {
        return injected ?: Dispatchers.Default
    }
}

object RepertoireContentResolverFactory {
    private var injected: RepertoireContentResolver? = null
    class InjectForTests(rhs: RepertoireContentResolver): AutoCloseable, Closeable {
        init {
            injected = rhs
        }
        override fun close() {
            injected = null
        }
    }

    fun create(context: Context): RepertoireContentResolver {
        return injected ?: NativeContentResolver(context)
    }
}

object AddSongsLauncherActivityResultRegistryFactory {
    private var injected: ActivityResultRegistry? = null
    class InjectForTests(rhs: ActivityResultRegistry): AutoCloseable, Closeable {
        init {
            injected = rhs
        }
        override fun close() {
            injected = null
        }
    }

    fun create(): ActivityResultRegistry? {
        return injected
    }
}
