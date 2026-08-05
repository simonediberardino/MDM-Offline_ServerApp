package com.dbg.mdm_serverapp.data.network

import io.ktor.client.HttpClient

expect fun createHttpClient(): HttpClient
