package app.example.di

import app.example.data.ExampleEmailRepository
import app.example.data.ExampleEmailRepositoryImpl
import app.example.data.ExampleEmailValidator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

/**
 * Example of Metro 0.5.0 @BindingContainer feature for better dependency organization.
 * 
 * Binding containers provide a way to group related bindings together in a more
 * organized manner than traditional modules. This is particularly useful for
 * organizing data layer dependencies.
 * 
 * Key benefits:
 * - Better code organization through logical grouping
 * - Cleaner separation of concerns
 * - Easier to understand dependency relationships
 * 
 * Note: This is an experimental feature in Metro 0.5.0.
 * See https://zacsweers.github.io/metro/dependency-graphs#binding-containers
 */
@BindingContainer
@SingleIn(AppScope::class)
interface DataBindingContainer {
    
    /**
     * Provides the email validator instance.
     * 
     * Note: In Metro 0.5.0, this would normally be handled by @ContributesBinding
     * on the implementation class (ExampleEmailRepositoryImpl), but this shows
     * how binding containers can explicitly provide dependencies.
     */
    @Provides
    @SingleIn(AppScope::class)
    fun provideEmailValidator(): ExampleEmailValidator = ExampleEmailValidator()
    
    /**
     * Alternative way to bind the repository using @ContributesBinding.
     * This is kept commented to show the different approaches available.
     * 
     * The actual binding is done via @ContributesBinding annotation
     * on ExampleEmailRepositoryImpl class.
     */
    // @Binds
    // fun bindEmailRepository(impl: ExampleEmailRepositoryImpl): ExampleEmailRepository
}