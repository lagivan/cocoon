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
package org.apache.cocoon.forms.formmodel;

import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Builds {UnionDefinition}s.
 *
 * @version $Id$
 */
public final class UnionDefinitionBuilder extends AbstractContainerDefinitionBuilder {

    public WidgetDefinition buildWidgetDefinition(Element element) throws Exception {
        UnionDefinition definition = new UnionDefinition();
        super.setupDefinition(element, definition);
        definition.setCaseWidgetId(DomHelper.getAttribute(element, "case"));
        setDisplayData(element, definition);

        setupContainer(element,"widgets",definition);

        definition.makeImmutable();
        return definition;
    }
    // TODO: Need to add code somewhere to build a selection list for the case widget.
}

