/*
 * The code is copyright ©2021
 */

package com.foo.credible.converters

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.postgresql.util.PGobject
import java.io.IOException
import javax.json.Json
import javax.json.JsonObject
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = true)
class JsonBConverter : AttributeConverter<JsonObject, Any> {
    override fun convertToDatabaseColumn(objectValue: JsonObject): Any {
        return try {
            val out = PGobject()
            out.type = "jsonb"
            out.value = objectValue.toString()
            out
        } catch (e: Exception) {
            throw IllegalArgumentException("Unable to serialize to json field ", e)
        }
    }

    override fun convertToEntityAttribute(dataValue: Any): JsonObject {
        return try {
            if (dataValue is PGobject && dataValue.type == "jsonb") {
                mapper.readerFor(object : TypeReference<JsonObject?>() {}).readValue(dataValue.value)
            } else Json.createObjectBuilder().build()
        } catch (e: IOException) {
            throw IllegalArgumentException("Unable to deserialize to json field ", e)
        }
    }

    companion object {
        private const val serialVersionUID = 1L
        private val mapper = ObjectMapper()
    }
}
