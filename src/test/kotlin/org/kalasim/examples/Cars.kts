//Cars.kts
import org.kalasim.Component
import org.kalasim.createSimulation

class Car : Component() {

    override fun process() = sequence {
        // drive around for an hour
        yield(hold(1.0))
        // and terminate when reaching the destination
        yield(terminate())
    }
}

createSimulation(enableTraceLogger = true) {
    Car()
}.run(5.0)