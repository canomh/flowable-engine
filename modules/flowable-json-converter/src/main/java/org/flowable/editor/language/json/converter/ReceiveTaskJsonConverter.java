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
package org.flowable.editor.language.json.converter;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.BaseElement;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.ReceiveTask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class ReceiveTaskJsonConverter extends BaseBpmnJsonConverter {

    public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap, Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {

        fillJsonTypes(convertersToBpmnMap);
        fillBpmnTypes(convertersToJsonMap);
    }

    public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
        convertersToBpmnMap.put(STENCIL_TASK_RECEIVE, ReceiveTaskJsonConverter.class);
        convertersToBpmnMap.put(STENCIL_TASK_RECEIVE_EVENT, ReceiveTaskJsonConverter.class);
    }

    public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        convertersToJsonMap.put(ReceiveTask.class, ReceiveTaskJsonConverter.class);
    }

    @Override
    protected String getStencilId(BaseElement baseElement) {
        if (baseElement.getExtensionElements().get("eventType") != null && baseElement.getExtensionElements().get("eventType").size() > 0) {
            String eventType = baseElement.getExtensionElements().get("eventType").get(0).getElementText();
            if (StringUtils.isNotEmpty(eventType)) {
                return STENCIL_TASK_RECEIVE_EVENT;
            }
        }
        return STENCIL_TASK_RECEIVE;
    }

    @Override
    protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement, BpmnJsonConverterContext converterContext) {
        addEventRegistryProperties((FlowElement) baseElement, propertiesNode);
    }

    @Override
    protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap,
        BpmnJsonConverterContext converterContext) {
        ReceiveTask task = new ReceiveTask();

        String stencilId = BpmnJsonConverterUtil.getStencilId(elementNode);
        if (STENCIL_TASK_RECEIVE_EVENT.equals(stencilId)) {
            addReceiveEventExtensionElements(elementNode, task);
        }

        return task;
    }
}
