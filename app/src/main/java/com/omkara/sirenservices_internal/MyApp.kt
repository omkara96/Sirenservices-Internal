package com.omkara.sirenservices_internal

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

class MyApp : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)   // 25% of available RAM
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("coil_cache"))
                    .maxSizePercent(0.05) // 5% of disk storage
                    .build()
            }
            .build()
    }
}
