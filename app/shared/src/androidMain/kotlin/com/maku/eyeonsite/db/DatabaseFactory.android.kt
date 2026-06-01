@file:Suppress("ktlint:standard:filename")

package com.maku.eyeonsite.db

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

fun createDatabase(context: Context): EyeOnSiteDatabase {
    val driver =
        AndroidSqliteDriver(
            schema = EyeOnSiteDatabase.Schema,
            context = context,
            name = "eyeonsite.db",
            callback =
                object : AndroidSqliteDriver.Callback(EyeOnSiteDatabase.Schema) {
                    override fun onUpgrade(
                        db: androidx.sqlite.db.SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int,
                    ) {
                        // v1→v2: storage_url on pending_upload + create pending_evidence (matches 1.sqm)
                        if (oldVersion < 2) {
                            db.execSQL("ALTER TABLE pending_upload ADD COLUMN storage_url TEXT")
                            db.execSQL(
                                """
                                CREATE TABLE IF NOT EXISTS pending_evidence (
                                    id TEXT NOT NULL PRIMARY KEY,
                                    task_id TEXT NOT NULL,
                                    inspector_uid TEXT NOT NULL,
                                    notes TEXT NOT NULL,
                                    photo_count INTEGER NOT NULL,
                                    created_at_ms INTEGER NOT NULL
                                )
                                """.trimIndent(),
                            )
                        }
                        // v2→v3: attestation on pending_evidence + upload_status on pending_upload (matches 2.sqm)
                        if (oldVersion < 3) {
                            db.execSQL("ALTER TABLE pending_evidence ADD COLUMN attestation_provider TEXT NOT NULL DEFAULT 'NONE'")
                            db.execSQL("ALTER TABLE pending_evidence ADD COLUMN attestation_verdict TEXT NOT NULL DEFAULT 'UNKNOWN'")
                            db.execSQL("ALTER TABLE pending_evidence ADD COLUMN attestation_checked_at_ms INTEGER NOT NULL DEFAULT 0")
                            db.execSQL("ALTER TABLE pending_upload ADD COLUMN upload_status TEXT NOT NULL DEFAULT 'QUEUED'")
                        }
                        // v3→v4: escrow_status on cached_task (matches 3.sqm)
                        if (oldVersion < 4) {
                            db.execSQL("ALTER TABLE cached_task ADD COLUMN escrow_status TEXT NOT NULL DEFAULT 'UNFUNDED'")
                        }
                    }
                },
        )
    return EyeOnSiteDatabase(driver)
}
