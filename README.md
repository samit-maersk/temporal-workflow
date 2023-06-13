# temporal-workflow

- This is a learning project to get familiar with Temporal Workflow and usage around it.
- Wanted to get started : [https://docs.temporal.io/dev-guide/java/foundations#run-a-dev-cluster](https://docs.temporal.io/dev-guide/java/foundations#run-a-dev-cluster) explain a lot to get started.
- For quick start [https://learn.temporal.io/getting_started/java/dev_environment/](https://learn.temporal.io/getting_started/java/dev_environment/)
- There are various example , which can be found [https://github.com/temporalio?q=samples-&type=all&language=&sort=](https://github.com/temporalio?q=samples-&type=all&language=&sort=)

### Start Temporal Server in Local dev environment

```shell
docker compose up

# if you have temporal CLI installed
temporal server start-dev
```
> .env, dynamicconfig are needed for this docker-compose to work
> Once it is up and running, you can access the UI at http://localhost:3111

### Start Temporal Worker (Which is nothing bit this springboot application)
```shell
./mvnw spring-boot:run
```

To test the flow , Import the postman collection from `postman` folder and run the request in order. 


