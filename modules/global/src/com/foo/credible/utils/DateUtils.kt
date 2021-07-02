/*
 * The code is copyright ©2021
 */

package com.foo.credible.utils

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    private const val ZULU_DATE_FORMAT = """yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"""

    /**
     * Converts a UTC date string  to java.util.Date object
     *
     * @param dateFormat
     * @param timeZone
     * @return
     */
    fun String.toUTC(
        dateFormat: String = ZULU_DATE_FORMAT,
        timeZone: TimeZone = TimeZone.getTimeZone("UTC")
    ): Date {
        val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
        parser.timeZone = timeZone
        return parser.parse(this)
    }

    /**
     * Converts a UTC java.util.Date object to string
     *
     * @param dateFormat
     * @param timeZone
     * @return
     */
    fun Date.formatTo(
        dateFormat: String = ZULU_DATE_FORMAT,
        timeZone: TimeZone = TimeZone.getTimeZone("UTC")
    ): String {
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        formatter.timeZone = timeZone
        return formatter.format(this)
    }

    /**
     * Converts a LocalDateTime to  UTC 'yyyy-MM-dd HH:mm:s' string
     *
     * @param dateFormat
     * @param timeZone
     * @return
     */
    fun LocalDateTime.formatTo(
        dateFormat: String = ZULU_DATE_FORMAT,
        timeZone: ZoneOffset = ZoneOffset.UTC
    ): String {
        val formatter = DateTimeFormatter.ofPattern(dateFormat, Locale.getDefault())
        return this.format(formatter.withZone(timeZone))
    }
}
