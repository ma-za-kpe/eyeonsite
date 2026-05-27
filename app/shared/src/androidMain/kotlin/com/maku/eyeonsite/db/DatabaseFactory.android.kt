@file:Suppress("ktlint:standard:filename")

package com.maku.eyeonsite.db

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

fun createDatabase(context: Context): EyeOnSiteDatabase {
    val driver = AndroidSqliteDriver(EyeOnSiteDatabase.Schema, context, "eyeonsite.db")
    return EyeOnSiteDatabase(driver)
}
