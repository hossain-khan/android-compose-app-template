package app.example.di

import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.Provides

/**
 * Metro providers that contribute Circuit dependencies.
 */
@ContributesTo(AppScope::class)
interface CircuitModule {
    /**
     * Provides a singleton instance of Circuit with presenter and ui configured.
     */
    @SingleIn(AppScope::class)
    @Provides
    fun provideCircuit(
        presenterFactories: Set<Presenter.Factory>,
        uiFactories: Set<Ui.Factory>,
    ): Circuit =
        Circuit
            .Builder()
            .addPresenterFactories(presenterFactories)
            .addUiFactories(uiFactories)
            .build()
}
