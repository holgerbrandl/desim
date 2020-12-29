//Atm.kt
package org.kalasim.examples

import org.apache.commons.math3.distribution.ExponentialDistribution
import org.kalasim.Component
import org.kalasim.ComponentGenerator
import org.kalasim.Resource
import org.kalasim.analytics.display
import org.kalasim.createSimulation

//https://youtrack.jetbrains.com/issue/KT-44062


fun main() {

    createSimulation(false) {

        val lambda = 1.5
        val mu = 1.0
        val rho = lambda / mu
        println(
            "rho is ${rho}. With rho>1 the system would be unstable, " +
                    "because there are more arrivals then the atm can serve."
        )

        val atm = Resource("atm", 1)

        class Customer : Component() {
            val ed = ExponentialDistribution(rg, mu)

            override fun process() = sequence {
                yield(request(atm))

                yield(hold(ed.sample()))
                release(atm)
            }
        }

        ComponentGenerator(iat = ExponentialDistribution(rg, lambda)) {
            Customer()
        }

        run(2000)

        atm.occupancyMonitor.display()
        atm.requesters.queueLengthMonitor.display()
        atm.requesters.lengthOfStayMonitor.display()

        println(atm.requesters.lengthOfStayMonitor.statistics())
    }
}