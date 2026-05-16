import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.CornerRounding
fun main() {
    val methods = RoundedPolygon.Companion::class.java.methods.map { it.name }
    println(methods.joinToString(", "))
}
