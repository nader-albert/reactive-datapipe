The reactive data pipe is a logical channel of continuous data processing, that can dynamically scale up/out and shrink back 
according to the incoming data influx, and hence react to the incoming load. 

The application is built in scala, and relies on the Akka implementation to the Actor model, in addition to few other Akka modules like Akka-Remote, Akka-Cluster, Akka-Persistence and the experimental Akka-Stream. 

It starts with a data source, that can be connected to any source of data stream, twitter feed could be an example. It ends with a data sink, which consumes the outcoming processed data and pushes it to a configurable set of destinations.

Each Sink corresponds to a single destination. Several Sinks could be simultaneously connected to the same data pipe. The data will then translated, transformed and processed once and published to many places 

Each data element passing through the pipe, is called [Data Pill], and hence the pipe is called a pipe of data pills
The pipe construction is designed to be lazy, meaning that it doesn't ignited until a Sink is connected to it.
