package com.example.temporalworkflow;

import com.example.temporalworkflow.model.User;
import com.example.temporalworkflow.workflows.UserActivityImpl;
import com.example.temporalworkflow.workflows.UserWorkflow;
import com.example.temporalworkflow.workflows.UserWorkflowImpl;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsRequest;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;
import java.util.stream.Collectors;

@SpringBootApplication
public class TemporalWorkflowApplication {

	private static final String USER_APPROVAL_TASK_QUEUE = "USER_APPROVAL_TASK_QUEUE";

	public static void main(String[] args) {
		SpringApplication.run(TemporalWorkflowApplication.class, args);
	}

	@Bean
	WorkflowClient workflowClient() {
		WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
		return WorkflowClient.newInstance(service);
	}

	@Bean
	ApplicationListener<ApplicationReadyEvent> onApplicationReady(WorkflowClient client) {
		return event -> {
			WorkerFactory factory = WorkerFactory.newInstance(client);
			Worker worker = factory.newWorker(USER_APPROVAL_TASK_QUEUE);
			// This Worker hosts both Workflow and Activity implementations.
			// Workflows are stateful so a type is needed to create instances.
			worker.registerWorkflowImplementationTypes(UserWorkflowImpl.class);
			// Activities are stateless and thread safe so a shared instance is used.
			worker.registerActivitiesImplementations(new UserActivityImpl());
			// Start listening to the Task Queue.
			factory.start();
		};
	}

	@Bean
	RouterFunction routerFunction(WorkflowClient client) {
		return RouterFunctions.route()
				.POST("/user", serverRequest -> {
					var uuid = UUID.randomUUID().toString();
					//fire and forget workflow.process(user)
					return serverRequest
							.bodyToMono(User.class)
							.doOnNext(user -> Mono.fromRunnable(() -> {
								var option = WorkflowOptions
										.newBuilder()
										.setWorkflowId(uuid)
										.setTaskQueue(USER_APPROVAL_TASK_QUEUE)
										//.setSearchAttributes(Map.of())
										.build();
								var workflow = client.newWorkflowStub(UserWorkflow.class, option);
								workflow.process(user);
							}).subscribeOn(Schedulers.boundedElastic()).subscribe())
							.flatMap(user -> ServerResponse.ok().bodyValue(uuid));
				})
				.PATCH("/user/{id}/approve", serverRequest -> {
					var id = serverRequest.pathVariable("id");
					var workflow = client.newWorkflowStub(UserWorkflow.class, id);
					workflow.approved();
					return ServerResponse.ok().build();
				})
				.PATCH("/user/{id}/rejected", serverRequest -> {
					var id = serverRequest.pathVariable("id");
					var workflow = client.newWorkflowStub(UserWorkflow.class, id);
					workflow.rejected();
					return ServerResponse.ok().build();
				})
				.GET("/user/{id}", serverRequest -> {
					var id = serverRequest.pathVariable("id");
					return Mono.fromCallable(() -> {
						var workflow = client.newWorkflowStub(UserWorkflow.class, id);
						return workflow.getStatus();
					}).flatMap(user -> ServerResponse.ok().body(Mono.just(user), User.class))
							.onErrorResume(t -> ServerResponse.notFound().build());
				})
				.GET("/all", serverRequest -> {

					//TODO move this to a service
					ListWorkflowExecutionsRequest listWorkflowExecutionRequest =
							ListWorkflowExecutionsRequest.newBuilder()
									.setNamespace(client.getOptions().getNamespace())
									.setQuery("WorkflowType='UserWorkflow'")
									.build();
					ListWorkflowExecutionsResponse listWorkflowExecutionsResponse = client
							.getWorkflowServiceStubs()
							.blockingStub()
							.listWorkflowExecutions(listWorkflowExecutionRequest);


					var workflowIds = listWorkflowExecutionsResponse
							.getExecutionsList()
							.stream()
							.map(e -> e.getExecution().getWorkflowId())
							.collect(Collectors.toList());

//					System.out.println(workflowIds);
//					return ServerResponse.noContent().build();

					return Flux.fromIterable(workflowIds)
							.flatMap(id -> Mono.fromCallable(() -> {
								try {
									var workflow = client.newWorkflowStub(UserWorkflow.class, id);
									return workflow.getStatus();
								} catch (Exception e) {
									return null;
								}

							}))
							.collectList()
							.flatMap(users -> ServerResponse.ok().body(Mono.just(users), User.class));

				})
				.build();
	}
}


