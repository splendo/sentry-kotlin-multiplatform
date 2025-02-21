package io.sentry.kotlin.multiplatform.extensions

import cocoapods.Sentry.SentryId
import io.sentry.kotlin.multiplatform.CocoaSentryEvent
import io.sentry.kotlin.multiplatform.SentryEvent

internal fun CocoaSentryEvent.applyKmpEvent(kmpEvent: SentryEvent): CocoaSentryEvent {
    kmpEvent.level?.let { level = it.toCocoaSentryLevel() }
    kmpEvent.platform?.let { platform = it }
    kmpEvent.message?.toCocoaMessage()?.let { message = it }
    kmpEvent.logger?.let { logger = it }
    fingerprint = kmpEvent.fingerprint
    kmpEvent.release?.let { releaseName = it }
    kmpEvent.environment?.let { environment = it }
    kmpEvent.user?.toCocoaUser()?.let { user = it }
    kmpEvent.serverName?.let { serverName = it }
    kmpEvent.dist?.let { dist = it }
    breadcrumbs = kmpEvent.breadcrumbs.map { it.toCocoaBreadcrumb() }.toMutableList()
    tags = kmpEvent.tags.toMutableMap()
    eventId = SentryId(kmpEvent.eventId.toString())
    return this
}
