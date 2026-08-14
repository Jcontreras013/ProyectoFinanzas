package com.proyectofinanzas.android

import android.app.Application
import com.proyectofinanzas.android.data.AppContainer

class SistemaContableApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }
}
