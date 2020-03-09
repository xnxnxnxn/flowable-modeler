package com.shareniu.flowablemodeler7;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@ComponentScan(basePackages = {"org.flowable.ui.modeler","org.flowable.ui.common","com.shareniu"})
public class FlowableModeler7Application {

    public static void main(String[] args) {
        SpringApplication.run(FlowableModeler7Application.class, args);
    }
}
