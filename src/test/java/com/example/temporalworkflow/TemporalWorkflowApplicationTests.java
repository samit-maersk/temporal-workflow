package com.example.temporalworkflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;

@SpringBootTest
@Testcontainers
class TemporalWorkflowApplicationTests {

	@Container
	static DockerComposeContainer<?> TEMPORAL =
			new DockerComposeContainer(new File("docker-compose.yml"))
					//.waitingFor("temporal", Wait.forListeningPort())
					.withLocalCompose(true)
					.waitingFor("temporal", Wait.forLogMessage(".*\"Namespace\":\"default\".*", 1));
	@Test
	void contextLoads() {
	}

}
