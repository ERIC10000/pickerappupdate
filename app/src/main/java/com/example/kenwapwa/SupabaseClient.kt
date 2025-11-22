package com.example.kenwapwa

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.SessionSource
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

val supabase = createSupabaseClient(
    supabaseUrl = "https://gpnkysawiietekujccir.supabase.co",
    supabaseKey = "sb_publishable_XvU1mb48E8Qpneb8Ps9hmw_9nHbqTGc"
) {
    install(Postgrest)
    install(Storage)
}