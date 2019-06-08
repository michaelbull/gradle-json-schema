package com.github.michaelbull.gradle.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule
import org.everit.json.schema.Schema
import org.everit.json.schema.ValidationException
import org.everit.json.schema.Validator
import org.everit.json.schema.loader.SchemaLoader
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File
import javax.inject.Inject

open class ValidateSchema @Inject constructor(
    objects: ObjectFactory
) : DefaultTask() {

    init {
        group = GROUP
    }

    @InputFile
    val schema = objects.fileProperty()

    @InputDirectory
    val inputDir = objects.directoryProperty()

    @OutputFile
    val report = objects.fileProperty()

    @Internal
    var inputMapper = ObjectMapper()
        .registerModule(JsonOrgModule())

    @Input
    var reportWriter = ObjectMapper()
        .registerModule(JsonOrgModule())
        .writerWithDefaultPrettyPrinter()

    private val validatorBuilder = Validator.ValidatorBuilder()

    fun validator(action: Action<in Validator.ValidatorBuilder>) {
        action.execute(validatorBuilder)
    }

    @TaskAction
    fun validate() {
        val schema = schema.get().asFile.readSchema()
        val validator = validatorBuilder.build()
        val exceptions = inputDir.get().asFile.validateDir(validator, schema)
        val reportFile = report.get().asFile

        if (exceptions.isEmpty()) {
            reportFile.writeJsonObject(JSONObject())
        } else {
            try {
                ValidationException.throwFor(schema, exceptions)
            } catch (ex: ValidationException) {
                reportFile.writeJsonObject(ex.toJSON())
                ex.allMessages.forEach(logger::error)
                throw ex
            }
        }
    }

    private fun File.readSchema(): Schema {
        return inputStream().use { input ->
            val tokener = JSONTokener(input)
            val schema = JSONObject(tokener)
            SchemaLoader.load(schema)
        }
    }

    private fun File.writeJsonObject(jsonObject: JSONObject) {
        bufferedWriter().use { writer ->
            reportWriter.writeValue(writer, jsonObject)
        }
    }

    private fun File.validateDir(validator: Validator, schema: Schema): List<ValidationException> {
        return walk()
            .filter { it.isFile }
            .map { it.readJsonObject() }
            .mapNotNull { it.validate(validator, schema) }
            .toList()
    }

    private fun File.readJsonObject(): JSONObject {
        return bufferedReader().use { reader ->
            inputMapper.readValue(reader, JSONObject::class.java)
        }
    }

    private fun JSONObject.validate(validator: Validator, schema: Schema): ValidationException? {
        return try {
            validator.performValidation(schema, this)
            null
        } catch (ex: ValidationException) {
            ex
        }
    }

    companion object {
        const val GROUP = "JSON Schema"
    }
}
