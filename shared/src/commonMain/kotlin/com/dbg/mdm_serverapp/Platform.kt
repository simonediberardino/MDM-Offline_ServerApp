package com.dbg.mdm_serverapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform