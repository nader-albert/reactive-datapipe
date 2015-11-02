The reactive data pipe is a logical channel of continuous data processing, that can dynamically scale up/out and shrink back 
according to the incoming data influx, and hence react to variations in the incoming load.

The application is built in scala, and relies on the Akka implementation to the Actor model, in addition to few other Akka modules like Akka-Remote, Akka-Cluster, Akka-Persistence and the experimental Akka-Stream. 

It starts with a data source, that can be connected to any source of data stream, twitter feed could be an example. It ends with a data sink, which consumes the outcoming processed data and pushes it to a configurable set of destinations.

Each Sink corresponds to a single destination. Several Sinks could be simultaneously connected to the same data pipe. The data will then translated, transformed and processed once and published to several places

Each data element passing through the pipe, is called [Data Pill], and hence the pipe is called a pipe of data pills
The pipe construction is designed to be lazy, meaning that it doesn't ignited until a Sink is connected to it.

###TO DO List

## Introduce Akka-Persistence
1. Enable Reliable Messaging Between the different distributed components.
2. Should be able to re-run the list of events that occured in the past, only from a predefined snapshot 

## Integtrate with a NoSQL DB.
1. To persist accumulative intermediate results during spark job processing.
2. MongoDB, could be used to store the processed Pills in Json Documents
3. Rely on a Reactive, asynchronous access to a persistent storage, ReactiveMongo is a good candidate.

## Rely on well defined Akka Routing strategies
1. Stop relying on trivial random routing logic, similar to the one used in the pipe-transformer module ```processingEngines(jobCounter % processingEngines.size) ! ProcessPill(pill) ``` 
