# Conduktor Technical Assessment (cta)

## High Level Reflections

I think I more or less implemented the spec as required, although I'm probably not quite "storing the person record as-is". My solution parses the list as json and then prints it out to a string again. The formatting is slightly different as a result (eg the indentation). With more time I would have tested that the records otherwise match exactly (special characters etc). It wouldn't surprise me if there are subtle differences.

My output format was somewhat random. I thought it would be handy to include the record metadata, so I threw that in but left the timestamp out so the tests would produce more consistent results. I prevaricated over how to represent the record in the server response. I tried out encoding it as embedded JSON, but for some reason I couldn't quickly figure out it rendered score as an Int instead of a Decimal so I left it rendering an escaped string instead. I left the code to render as embedded JSON commented out.

I could have created a single Kafka Consumer that is reused on every HTTP request but it isn't thread safe so the small amount of extra work to create a new one for each request felt important to do.

## Instructions to run tests

My tests assume that the following have been run locally,

```
docker run -p 9092:9092 apache/kafka:3.7.0
kafka-topics.sh --bootstrap-server localhost:9092 --topic first_topic --create --partitions 3 --replication-factor 1
sbt "runMain cta.app.RecordsLoader"
```

This isn't ideal (tests should be self supporting) but suited me for the time constraints.

`sbt test` should then work. It's still possible that my snapshot tests will produce different outcomes on different computers, however.

## Instructions to run server

Once the instructions to run tests steps have been followed you can run the server locally with

```
sbt "runMain cta.app.CtaConsumerServer"
```

## Reflections on Implementation

- The code has a much lower test coverage than I would normally be happy with. The Kafka Java Clients are tricky. I think it would be possible to write good tests against mocks of them but I basically ran out of time. I put one test in to demonstrate the direction I could have taken the tests. 
- I hardcoded a number of things (eg partition count, server location) instead of using the admin api or allowing configuration to be injected
- I'm not doing any input validation. There are lots of inputs which would break the server, eg negative numbers.
- I'm not doing any error handling except printing the error and returning a generic response. In a production server I would take more care with how i log errors and potentially return slightly more helpful error responses.

## Performance Notes

- Every API request is going to open a new TCP connection with Kafka which will be sub-optimal for batching if large numbers of calls
- There would be performance benefits in creating a pool of Kafka Consumers and reusing them.

