package org.kalasim;

/**
 * @param preemptive If a component requests from a preemptive resource, it may bump component(s) that are claiming from
the resource, provided these have a lower priority = higher value). If component is bumped, it releases the resource and is the activated, thus essentially stopping the current action (usually hold or passivate). Therefore, it is necessary that a component claiming from a preemptive resource should check
whether the component is bumped or still claiming at any point where they can be bumped.
 */
open class Resource(
    name: String? = null,
    var capacity: Double = 1.0,
    val preemptive: Boolean = false,
    val anonymous: Boolean = false
) : SimulationEntity(name = name) {


    var minq: Double = Double.MAX_VALUE

    // should we this make readonly from outside?
    val requesters = ComponentQueue<Component>()
    val claimers = ComponentQueue<Component>()

    var claimedQuantity = 0.0
        set(x) {
            field = x

            claimedQuantityMonitor.addValue(x)
            availableQuantityMonitor.addValue(capacity - claimedQuantity)
            occupancyMonitor.addValue(if (capacity < 0) 0 else claimedQuantity / capacity)
            capacityMonitor.addValue(capacity)

            printTrace("claim ${claimedQuantity} from $name")
        }


    var availableQuantity = 0
        set(x) {
            field = x
            availableQuantityMonitor.addValue(x)
        }


    // todo TBD should we initialize these monitoring by tallying the intial state?
    val capacityMonitor = NumericLevelMonitor("Capacity of ${super.name}", initialValue = capacity)

    val claimedQuantityMonitor = NumericLevelMonitor("Claimed quantity of ${this.name}")
    val availableQuantityMonitor = NumericLevelMonitor("Available quantity of ${this.name}", initialValue = capacity)
    val occupancyMonitor = NumericLevelMonitor("Occupancy of ${this.name}")


    init {
        printTrace("create ${this.name} with capcacity ${capacity} " + if (anonymous) "anonymous" else "")
    }

    fun availableQuantity(): Double = capacity - claimedQuantity


    fun tryRequest(): Boolean {
        val iterator = requesters.q.iterator()

        if (anonymous) {
            // TODO trying not implemented

            iterator.forEach {
                it.component.tryRequest()
            }
        } else {
            while (iterator.hasNext()) {
                //try honor as many requests as possible
                if (minq > (capacity - claimedQuantity + EPS)) {
                    break
                }
                iterator.next().component.tryRequest()
            }
        }

        return true
    }

    /** releases all claims or a specified quantity
     *
     * @param  quantity  quantity to be released. If not specified, the resource will be emptied completely.
     * For non-anonymous resources, all components claiming from this resource will be released.
     */
    fun release(quantity: Double? = null) {
        // TODO Split resource types into QuantityResource and Resource or similar
        if (anonymous) {
            val q = quantity ?: claimedQuantity

            claimedQuantity = -q
            if (claimedQuantity < EPS) claimedQuantity = 0.0

            // done within decrementing claimedQuantity
//            occupancyMonitor.addValue(if (capacity <= 0) 0 else claimedQuantity / capacity)
//            availableQuantityMonitor.addValue(capacity - claimedQuantity)

        } else {
            require(quantity != null) { "quantity missing for non-anonymous resource" }

            while (requesters.isNotEmpty()) {
                requesters.poll().release(this)
            }
        }
    }

    fun removeRequester(component: Component) {
        requesters.remove(component)
        if (requesters.isEmpty()) minq = Double.MAX_VALUE
    }

    /** prints a summary of statistics of a resource */
    fun printStatistics() {
        TODO()

    }

    override val info: Snapshot
        get() = ResourceInfo(this)
}

class ResourceInfo(resource: Resource) : Snapshot() {
    val name: String = resource.name
    val creationTime: Double = resource.creationTime

    val claimedQuantity: Double = resource.claimedQuantity
    val capacity = resource.capacity

    // use a dedicated type here to see null prios in json
    val claimedBy = resource.claimers.q.toList().map { it.component.name to it.priority }

    data class ReqComp(val component:String, val quantity:Double?)
    val requestingComponents = resource.requesters.q.toList().map {
        ReqComp(it.component.name,it.component.requests[resource])
    }
}
