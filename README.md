The reactive data pipe is a logical channel of continuous data processing, thar can dynamically scale up/out and shrink back 
according to the incoming data influx. It starts with a data source, that can be connected to any source of data stream, twitter feed could 
be an example. It ends with a data sink, which consumes the outcoming processed data and pushes to a configurable set of destinations.
Each Sink corresponds to a single destination. Several Sinks could be simultaneously connected to the same data pipe. The data will then translated, transformed and processed once and published to many places 
