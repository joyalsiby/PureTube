package com.puretube.player

import android.app.Application
import com.puretube.player.data.AppDatabase
import com.puretube.player.extractor.OkHttpDownloader
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import java.util.Locale

class PureTubeApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        val locale = Locale.getDefault()
        NewPipe.init(
            OkHttpDownloader(),
            Localization.fromLocale(locale),
            ContentCountry(locale.country.ifEmpty { "US" })
        )
    }
}
