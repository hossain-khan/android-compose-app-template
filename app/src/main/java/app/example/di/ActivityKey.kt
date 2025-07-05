package app.example.di

import android.app.Activity
import com.squareup.metro.IntoMap
import kotlin.reflect.KClass

/**
 * A Metro map key annotation used for registering a [Activity] into the dependency graph.
 */
@IntoMap
annotation class ActivityKey(
    val value: KClass<out Activity>,
)
