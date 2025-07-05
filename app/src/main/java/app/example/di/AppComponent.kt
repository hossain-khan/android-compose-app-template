package app.example.di

import android.app.Activity
import android.content.Context
import app.example.data.ExampleEmailValidator
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.Multibinding
import dev.zacsweers.metro.createGraph
import javax.inject.Provider

@DependencyGraph
@SingleIn(AppScope::class)
interface AppGraph {
    val activityProviders: Map<Class<out Activity>, @JvmSuppressWildcards Provider<Activity>>

    @Provides
    @SingleIn(AppScope::class)
    fun provideCircuit(
        presenterFactories: Set<Presenter.Factory>,
        uiFactories: Set<Ui.Factory>,
    ): Circuit {
        return Circuit.Builder()
            .addPresenterFactories(presenterFactories)
            .addUiFactories(uiFactories)
            .build()
    }

    @DependencyGraph.Factory
    interface Factory {
        fun create(
            @ApplicationContext context: Context,
        ): AppGraph
    }

    companion object {
        fun create(context: Context): AppGraph = createGraph<AppGraph> { create(context) }
    }
}
