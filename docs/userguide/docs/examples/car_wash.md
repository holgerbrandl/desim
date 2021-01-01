<!--## Car Wash-->

In this example, we'll learn how to wait for resources. The example is adopted from the [SimPy example](https://simpy.readthedocs.io/en/latest/examples/carwash.html).

We simulate a carwash with a limited number of machines and a number of cars that arrive at the carwash to get cleaned. The carwash uses a [resource](../resource.md) to model the limited number of washing machines. It also defines a process for washing a car.

When a car arrives at the carwash, it requests a machine. Once it got one, it starts the carwash’s wash processes and waits for it to finish. It finally releases the machine and leaves.

The cars are generated by a setup process. After creating an initial amount of cars it creates new car processes after a random time interval as long as the simulation continues.

```kotlin
//{!CarWash.kt!}
```