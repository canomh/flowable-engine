/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.camel.variables;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.flowable.engine.TaskService;
import org.flowable.engine.test.Deployment;
import org.flowable.spring.impl.test.SpringFlowableTestCase;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * 
 * @author Saeid Mirzaei
 */
@Tag("camel")
@ContextConfiguration("classpath:generic-camel-flowable-context.xml")
public class CamelVariableTransferTest extends SpringFlowableTestCase {
    @Autowired
    protected CamelContext camelContext;

    @Autowired
    protected TaskService taskService;

    protected MockEndpoint service1;

    @BeforeEach
    public void setUp() throws Exception {
        camelContext.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("direct:startAllProperties")
                        .setProperty("property1", simple("sampleValueForProperty1"))
                        .setProperty("property2", simple("sampleValueForProperty2"))
                        .setProperty("property3", simple("sampleValueForProperty3"))
                        .transform(simple("sampleBody"))
                        .to("log:testVariables?showProperties=true")
                        .to("flowable:testPropertiesProcess?copyVariablesFromProperties=true");

                from("direct:startNoProperties")
                        .setProperty("property1", simple("sampleValueForProperty1"))
                        .setProperty("property2", simple("sampleValueForProperty2"))
                        .setProperty("property3", simple("sampleValueForProperty3"))
                        .transform(simple("sampleBody"))
                        .to("log:testVariables?showProperties=true")
                        .to("flowable:testPropertiesProcess?copyVariablesFromProperties=false");

                from("direct:startFilteredProperties")
                        .setProperty("property1", simple("sampleValueForProperty1"))
                        .setProperty("property2", simple("sampleValueForProperty2"))
                        .setProperty("property3", simple("sampleValueForProperty3"))
                        .to("log:testVariables?showProperties=true")
                        .to("flowable:testPropertiesProcess?copyVariablesFromProperties=(property1|property2)");

                from("direct:startAllHeaders")
                        .setHeader("property1", simple("sampleValueForProperty1"))
                        .setHeader("property2", simple("sampleValueForProperty2"))
                        .setHeader("property3", simple("sampleValueForProperty3"))
                        .to("log:testVariables?showProperties=true");

                from("direct:startNoHeaders")
                        .setHeader("property1", simple("sampleValueForProperty1"))
                        .setHeader("property2", simple("sampleValueForProperty2"))
                        .setHeader("property3", simple("sampleValueForProperty3"))
                        .to("log:testVariables?showProperties=true")
                        .to("flowable:testPropertiesProcess?copyVariablesFromHeader=false");

                from("direct:startFilteredHeaders")
                        .setHeader("property1", simple("sampleValueForProperty1"))
                        .setHeader("property2", simple("sampleValueForProperty2"))
                        .setHeader("property3", simple("sampleValueForProperty3"))
                        .to("log:testVariables?showProperties=true")
                        .to("flowable:testPropertiesProcess?copyVariablesFromHeader=(property1|property2)");

            }
        });
    }

    @AfterEach
    public void tearDown() throws Exception {
        List<Route> routes = camelContext.getRoutes();
        for (Route r : routes) {
            camelContext.stopRoute(r.getId());
            camelContext.removeRoute(r.getId());
        }
    }

    // check that at least all properties are passed from camel to activiti when copyVariablesFromProperties=true is simply true
    @Test
    @Deployment
    public void testCamelPropertiesAll() throws Exception {
        ProducerTemplate tpl = camelContext.createProducerTemplate();
        Exchange exchange = camelContext.getEndpoint("direct:startAllProperties").createExchange();
        tpl.send("direct:startAllProperties", exchange);

        assertThat(taskService).isNotNull();
        assertThat(runtimeService).isNotNull();
        assertThat(taskService.createTaskQuery().count()).isEqualTo(1);
        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task).isNotNull();
        Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
        assertThat(variables)
                .contains(
                        entry("property1", "sampleValueForProperty1"),
                        entry("property2", "sampleValueForProperty2"),
                        entry("property3", "sampleValueForProperty3")
                );
    }

    // check that body will be copied into variables even if copyVariablesFromProperties=true
    @Test
    @Deployment(resources = { "org/flowable/camel/variables/CamelVariableTransferTest.testCamelPropertiesAll.bpmn20.xml" })
    public void testCamelPropertiesAndBody() throws Exception {
        ProducerTemplate tpl = camelContext.createProducerTemplate();
        Exchange exchange = camelContext.getEndpoint("direct:startAllProperties").createExchange();

        tpl.send("direct:startAllProperties", exchange);

        assertThat(taskService).isNotNull();
        assertThat(runtimeService).isNotNull();
        assertThat(taskService.createTaskQuery().count()).isEqualTo(1);
        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task).isNotNull();
        Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
        assertThat(variables)
                .contains(
                        entry("property1", "sampleValueForProperty1"),
                        entry("property2", "sampleValueForProperty2"),
                        entry("property3", "sampleValueForProperty3"),
                        entry("camelBody", "sampleBody")
                );
    }

    @Test
    @Deployment(resources = { "org/flowable/camel/variables/CamelVariableTransferTest.testCamelPropertiesAll.bpmn20.xml" })
    public void testCamelPropertiesFiltered() throws Exception {
        ProducerTemplate tpl = camelContext.createProducerTemplate();
        Exchange exchange = camelContext.getEndpoint("direct:startFilteredProperties").createExchange();
        tpl.send("direct:startFilteredProperties", exchange);

        assertThat(taskService).isNotNull();
        assertThat(runtimeService).isNotNull();
        assertThat(taskService.createTaskQuery().count()).isEqualTo(1);
        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task).isNotNull();
        Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
        assertThat(variables)
                .contains(
                        entry("property1", "sampleValueForProperty1"),
                        entry("property2", "sampleValueForProperty2")
                )
                .doesNotContainKey("property3");
    }

    @Test
    @Deployment(resources = { "org/flowable/camel/variables/CamelVariableTransferTest.testCamelPropertiesAll.bpmn20.xml" })
    public void testCamelPropertiesNone() throws Exception {
        ProducerTemplate tpl = camelContext.createProducerTemplate();
        Exchange exchange = camelContext.getEndpoint("direct:startNoProperties").createExchange();
        tpl.send("direct:startNoProperties", exchange);

        assertThat(taskService).isNotNull();
        assertThat(runtimeService).isNotNull();
        assertThat(taskService.createTaskQuery().count()).isEqualTo(1);
        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task).isNotNull();
        Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
        assertThat(variables)
                .doesNotContainKeys("property1", "property2", "property3");
    }

    @Test
    @Deployment(resources = { "org/flowable/camel/variables/CamelVariableTransferTest.testCamelPropertiesAll.bpmn20.xml" })
    public void testCamelHeadersAll() throws Exception {
        ProducerTemplate tpl = camelContext.createProducerTemplate();
        Exchange exchange = camelContext.getEndpoint("direct:startAllProperties").createExchange();
        tpl.send("direct:startAllProperties", exchange);

        assertThat(taskService).isNotNull();
        assertThat(runtimeService).isNotNull();
        assertThat(taskService.createTaskQuery().count()).isEqualTo(1);
        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task).isNotNull();
        Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
        assertThat(variables)
                .contains(
                        entry("property1", "sampleValueForProperty1"),
                        entry("property2", "sampleValueForProperty2"),
                        entry("property3", "sampleValueForProperty3")
                );
    }

    @Test
    @Deployment(resources = { "org/flowable/camel/variables/CamelVariableTransferTest.testCamelPropertiesAll.bpmn20.xml" })
    public void testCamelHeadersFiltered() throws Exception {
        ProducerTemplate tpl = camelContext.createProducerTemplate();
        Exchange exchange = camelContext.getEndpoint("direct:startFilteredHeaders").createExchange();
        tpl.send("direct:startFilteredHeaders", exchange);

        assertThat(taskService).isNotNull();
        assertThat(runtimeService).isNotNull();
        assertThat(taskService.createTaskQuery().count()).isEqualTo(1);
        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task).isNotNull();
        Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
        assertThat(variables)
                .contains(
                        entry("property1", "sampleValueForProperty1"),
                        entry("property2", "sampleValueForProperty2")
                )
                .doesNotContainKey("property3");
    }

    @Test
    @Deployment(resources = { "org/flowable/camel/variables/CamelVariableTransferTest.testCamelPropertiesAll.bpmn20.xml" })
    public void testCamelHeadersNone() throws Exception {
        ProducerTemplate tpl = camelContext.createProducerTemplate();
        Exchange exchange = camelContext.getEndpoint("direct:startNoHeaders").createExchange();
        tpl.send("direct:startNoHeaders", exchange);

        assertThat(taskService).isNotNull();
        assertThat(runtimeService).isNotNull();
        assertThat(taskService.createTaskQuery().count()).isEqualTo(1);
        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task).isNotNull();
        Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
        assertThat(variables)
                .doesNotContainKeys("property1", "property2", "property3");
    }

}
