package app.example.di

import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import com.squareup.metro.ContributesTo
import com.squareup.metro.SingleIn
import com.squareup.metro.Provides
import com.squareup.metro.Multibinding

/**
 * Metro providers that contribute Circuit dependencies.
 */
@ContributesTo(AppScope::class)
interface CircuitModule {
    /**
     * Metro multi-binding method that provides a set of Presenter.Factory instances.
     */
    @Multibinding(allowEmpty = true)
    val presenterFactories: Set<Presenter.Factory>

    /**
     * Metro multi-binding method that provides a set of Ui.Factory instances.
     */
    @Multibinding(allowEmpty = true)
    val viewFactories: Set<Ui.Factory>

    /**
     * Provides a singleton instance of Circuit with presenter and ui configured.
     */
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
}
