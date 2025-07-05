package app.example.di

import android.app.Activity
import android.content.Context
import app.example.data.ExampleEmailValidator
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import com.squareup.metro.DependencyGraph
import com.squareup.metro.SingleIn
import com.squareup.metro.Provides
import com.squareup.metro.Multibinding
import com.squareup.metro.createGraph
import javax.inject.Provider

@DependencyGraph
@SingleIn(AppScope::class)
interface AppGraph {
    val activityProviders: Map<Class<out Activity>, @JvmSuppressWildcards Provider<Activity>>

    // Circuit dependencies
    @Multibinding(allowEmpty = true)
    val presenterFactories: Set<Presenter.Factory>

    @Multibinding(allowEmpty = true)
    val uiFactories: Set<Ui.Factory>

    @Provides
    fun provideEmailValidator(): ExampleEmailValidator = ExampleEmailValidator()

    @SingleIn(AppScope::class)
    @Provides
    fun provideCircuit(
        presenterFactories: @JvmSuppressWildcards Set<Presenter.Factory>,
        uiFactories: @JvmSuppressWildcards Set<Ui.Factory>,
    ): Circuit =
        Circuit
            .Builder()
            .addPresenterFactories(presenterFactories)
            .addUiFactories(uiFactories)
            .build()

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
