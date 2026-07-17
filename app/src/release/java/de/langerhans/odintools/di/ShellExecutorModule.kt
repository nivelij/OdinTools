package de.langerhans.odintools.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.langerhans.odintools.tools.ShellExecutor

/**
 * Release binding: the real [ShellExecutor] that talks to the AYN PServer vendor binder.
 * Unscoped, matching the original `@Inject constructor()` behaviour.
 */
@Module
@InstallIn(SingletonComponent::class)
object ShellExecutorModule {

    @Provides
    fun provideShellExecutor(): ShellExecutor = ShellExecutor()
}
