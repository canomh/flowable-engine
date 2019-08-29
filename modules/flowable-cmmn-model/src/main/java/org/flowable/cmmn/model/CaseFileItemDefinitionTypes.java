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
package org.flowable.cmmn.model;

/**
 * @author Joram Barrez
 */
public interface CaseFileItemDefinitionTypes {

    // From the CMMN spec. Note the XSD/WSDL/UML types are not supported

    String TYPE_CMIS_FOLDER = "http://www.omg.org/spec/CMMN/DefinitionType/CMISFolder";
    String TYPE_CMIS_DOCUMENT = "http://www.omg.org/spec/CMMN/DefinitionType/CMISDocument";
    String TYPE_CMIS_RELATIONSHIP = "http://www.omg.org/spec/CMMN/DefinitionType/CMISRelationship";
    String TYPE_UNKNOWN = "http://www.omg.org/spec/CMMN/DefinitionType/Unknown";
    String TYPE_UNSPECIFIED = "http://www.omg.org/spec/CMMN/DefinitionType/Unspecified";

    // Flowable specific
    String TYPE_FOLDER = "http://flowable.org/cmmn/DefinitionType/Folder";
    String TYPE_FILE = "http://flowable.org/cmmn/DefinitionType/File";
}
