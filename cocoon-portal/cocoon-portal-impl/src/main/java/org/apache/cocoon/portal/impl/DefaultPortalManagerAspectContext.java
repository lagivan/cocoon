/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.impl;

import java.util.Iterator;
import java.util.Properties;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.portal.PortalManagerAspect;
import org.apache.cocoon.portal.PortalManagerAspectPrepareContext;
import org.apache.cocoon.portal.PortalManagerAspectRenderContext;
import org.apache.cocoon.portal.PortalService;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * The aspect context is passed to every aspect.
 * @since 2.1.8
 * @version $Id$
 */
public final class DefaultPortalManagerAspectContext
    implements PortalManagerAspectRenderContext, PortalManagerAspectPrepareContext {

    private final PortalService service;
    private Iterator iterator;
    private Iterator configIterator;
    private Properties config;

    public DefaultPortalManagerAspectContext(PortalManagerAspectChain chain,
                                             PortalService service) {
        this.service = service;
        this.iterator = chain.getIterator();
        this.configIterator = chain.getConfigIterator();
    }

	/**
	 * @see org.apache.cocoon.portal.PortalManagerAspectPrepareContext#invokeNext()
	 */
	public void invokeNext() 
    throws ProcessingException {
        if (this.iterator.hasNext()) {
            this.config = (Properties)this.configIterator.next();
            final PortalManagerAspect aspect = (PortalManagerAspect) iterator.next();
            aspect.prepare(this, this.service);
        }
    }

    /**
     * @see org.apache.cocoon.portal.PortalManagerAspectRenderContext#getAspectProperties()
     */
    public Properties getAspectProperties() {
        return this.config;
    }

    /**
     * @see org.apache.cocoon.portal.PortalManagerAspectRenderContext#invokeNext(org.xml.sax.ContentHandler, org.apache.avalon.framework.parameters.Parameters)
     */
    public void invokeNext(ContentHandler ch, Properties properties) 
    throws SAXException {
        if (this.iterator.hasNext()) {
            this.config = (Properties)this.configIterator.next();
            final PortalManagerAspect aspect = (PortalManagerAspect) iterator.next();
            aspect.render(this, this.service, ch, properties);
        }
    }
}
