package com.example.tourscan.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage

object SupabaseClient {

    private const val SUPABASE_URL = "https://xbgsfgfwddghmebxhqkb.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_cOEWj-ZEIEqv72M5MdIJ6Q_MZmqEFZk"

    const val BUCKET_NAME = "tourscan_images"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Storage)
    }
}
