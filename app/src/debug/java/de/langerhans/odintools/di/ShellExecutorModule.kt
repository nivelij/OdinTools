package de.langerhans.odintools.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.langerhans.odintools.tools.FakeShellExecutor
import de.langerhans.odintools.tools.ShellExecutor
import javax.inject.Singleton

/**
 * Debug binding for [ShellExecutor].
 *
 * On real hardware (the AYN PServer vendor binder is present) we use the REAL executor, so a
 * debug build installed on an actual Odin 2 behaves exactly like release. Only when the binder
 * is absent (emulator / non-Odin device) do we fall back to [FakeShellExecutor], which emulates
 * a "virtual Odin 2" over an in-memory store. Release builds always use the real executor.
 */
@Module
@InstallIn(SingletonComponent::class)
object ShellExecutorModule {

    @Provides
    @Singleton
    fun provideShellExecutor(): ShellExecutor {
        val real = ShellExecutor()
        return if (real.pServerAvailable) real else FakeShellExecutor()
    }
}
