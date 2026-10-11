package com.knowledge.sporedoc

import android.app.Application
import com.knowledge.sporedoc.di.AppContainer

/** Owns the one [AppContainer] instance for the process. */
class SporeDocApp : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
