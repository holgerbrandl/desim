//Bank3ClerksResources.kt
package org.kalasim.examples.bank.resources

import org.kalasim.*
import org.kalasim.misc.display
import org.koin.core.component.get


class Customer(private val clerks: Resource) : Component() {

    override fun process() = sequence {
        request(clerks)
        hold(30)
        release(clerks) // not really required
    }
}


fun main() {
    val env = configureEnvironment {
        add { Resource("clerks", capacity = 3) }
    }.apply {
        ComponentGenerator(iat = uniform(5.0, 15.0)) { Customer(get()) }
    }.run(3000)

    env.get<Resource>().apply {
        printInfo()

        claimedQuantityMonitor.display()
        requesters.queueLengthMonitor.display()

        printStatistics()
    }
}

