package klalumiere.repertoire

import android.content.Context

object TranspositionPreference {
    private const val PREFS_NAME = "repertoire_prefs"
    private const val KEY_TRANSPOSITION = "transposition_semitones"
    const val MIN = 0
    const val MAX = 11

    fun get(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_TRANSPOSITION, 0)
            .coerceIn(MIN, MAX)

    fun set(context: Context, value: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_TRANSPOSITION, value.coerceIn(MIN, MAX))
            .apply()
    }
}
