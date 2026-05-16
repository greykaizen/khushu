package com.kaizen.khushu

import androidx.graphics.shapes.RoundedPolygon
import org.junit.Test
import kotlin.reflect.full.primaryConstructor

class ApiTest {
    @Test
    fun testPoly() {
        println(RoundedPolygon::class.constructors.map { it.parameters.map { p -> "${p.name}: ${p.type}" } })
        println(RoundedPolygon.Companion::class.members.map { it.name })
    }
}
