/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.apache.cocoon.woody.datatype.validationruleimpl;

import org.apache.cocoon.woody.datatype.ValidationRule;
import org.w3c.dom.Element;

/**
 * Builds {@link EmailValidationRule}s.
 * 
 * @version $Id: EmailValidationRuleBuilder.java,v 1.3 2004/03/05 13:02:30 bdelacretaz Exp $
 */
public class EmailValidationRuleBuilder extends AbstractValidationRuleBuilder {
    public ValidationRule build(Element validationRuleElement) throws Exception {
        EmailValidationRule rule = new EmailValidationRule();
        buildFailMessage(validationRuleElement, rule);
        return rule;
    }
}
