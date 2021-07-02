/*
 * The code is copyright ©2021
 */

package com.foo.credible.helpers

import com.anzi.credible.dto.ActivityDto
import com.anzi.credible.entity.Activity
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object ActivityHelper {
    fun buildDtoForList(activity: Activity): ActivityDto = ObjectMapper().registerKotlinModule()
        .readValue(activity.details, ActivityDto::class.java)
}
