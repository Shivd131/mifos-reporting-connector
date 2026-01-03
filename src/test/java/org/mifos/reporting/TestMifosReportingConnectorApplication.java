package org.mifos.reporting;

import org.springframework.boot.SpringApplication;

public class TestMifosReportingConnectorApplication {

	public static void main(String[] args) {
		SpringApplication.from(MifosReportingConnectorApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
