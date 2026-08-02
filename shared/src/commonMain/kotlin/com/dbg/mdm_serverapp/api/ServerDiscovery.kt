package com.dbg.mdm_serverapp.api

/**
 * Finds the MDM server on the LAN.
 * Tries localhost first, then UDP broadcast discovery.
 */
expect suspend fun discoverServerBaseUrl(): String?
