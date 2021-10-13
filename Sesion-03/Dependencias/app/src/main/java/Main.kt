//class Engine {
//    fun start() {
//        println("engine.start()")
//    }
//}
//
//class Car {
//    private val engine = Engine()
//
//    fun start() {
//        engine.start()
//    }
//}
//
//fun main() {
//    val car = Car()
//    car.start()
//}

class Engine {
    fun start() {
        println("engine.start()")
    }
}

// Dependencias por el constructor
class Car(private val engine: Engine) {
    fun start() {
        engine.start()
    }
}
// Dependencias por el método set
class Car2{
    lateinit var engine: Engine

    fun start() {
        engine.start()
    }
}

fun main() {
    val engine = Engine()
    // Dependencias por el constructor
    val car = Car(engine)
    car.start()

    // Dependencias por el método set
    val car2 = Car2()
    car2.engine = Engine()
    car.start()
}

