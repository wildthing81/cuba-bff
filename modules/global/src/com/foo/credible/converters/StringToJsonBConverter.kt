/*
 * The code is copyright ©2021
 */

package com.foo.credible.converters

import java.io.IOException
import javax.persistence.AttributeConverter
import javax.persistence.Converter
import org.postgresql.util.PGobject
import com.fasterxml.jackson.databind.ObjectMapper

@Converter(autoApply = false)
class StringToJsonBConverter : AttributeConverter<String, Any> {
    override fun convertToDatabaseColumn(objectValue: String?): Any {
        return try {
            val out = PGobject()
            out.type = "jsonb"
            out.value = objectValue ?: mapper.createArrayNode().toString()
            out
        } catch (e: Exception) {
            throw IllegalArgumentException("Unable to serialize to json field ", e)
        }
    }

    override fun convertToEntityAttribute(dataValue: Any?): String {
        return try {
            if (dataValue is PGobject && dataValue.type == "jsonb") {
                dataValue.value
            } else ObjectMapper().createArrayNode().toString()
        } catch (e: IOException) {
            throw IllegalArgumentException("Unable to deserialize to json field ", e)
        }
    }

    companion object {
        private const val serialVersionUID = 1L
        private val mapper = ObjectMapper()
    }
}
