/*
 * The code is copyright ©2021
 */

package com.foo.credible.converters

import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = false)
class StringToListConverter : AttributeConverter<List<String>, String> {

    companion object {
        private const val serialVersionUID = 1L
    }

    override fun convertToDatabaseColumn(attribute: List<String>?): String = if (attribute!!.isNotEmpty()) attribute
        .joinToString() else ""

    override fun convertToEntityAttribute(dbData: String?): List<String> = dbData?.split(',') ?: emptyList()
}
