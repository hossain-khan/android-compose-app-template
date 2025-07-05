package app.example.di

import android.content.Context
import app.example.data.ExampleEmailValidator
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import dev.zacsweers.metro.scope.MetroGraph
import dev.zacsweers.metro.scope.injector.MembersInjector
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
@ApplicationScope
abstract class AppGraph(private val application: Application) : MetroGraph {

  abstract val circuit: Circuit

  abstract val membersInjector: MembersInjector<MainActivity>

  @ApplicationScope
  @Provides
  fun provideContext(): Context = application

  @ApplicationScope
  @Provides
  fun provideEmailValidator(): ExampleEmailValidator = ExampleEmailValidator()

  @Provides
  fun provideCircuit(
    presenterFactories: Set<Presenter.Factory>,
    uiFactories: Set<Ui.Factory>,
  ): Circuit = Circuit.Builder()
    .addPresenterFactories(presenterFactories)
    .addUiFactories(uiFactories)
    .build()
}
