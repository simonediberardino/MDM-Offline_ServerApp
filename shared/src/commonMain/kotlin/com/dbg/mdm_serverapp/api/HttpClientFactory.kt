package com.dbg.mdm_serverapp.api

import io.ktor.client.HttpClient

expect fun createHttpClient(): HttpClient
