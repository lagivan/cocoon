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
package org.apache.cocoon.components.blocks;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.SitemapTestCase;
import org.apache.cocoon.environment.mock.MockEnvironment;

public class BlocksManagerTestCase extends SitemapTestCase {

    public void testCreate() throws ServiceException {
        BlocksManager blocks = (BlocksManager)this.lookup(BlocksManager.ROLE);
        this.release(blocks);
    }

    public void testMount() throws Exception {
        BlocksManager blocks = (BlocksManager)this.lookup(BlocksManager.ROLE);
        MockEnvironment env = getEnvironment("/test1/test");
        blocks.process(env);
        getLogger().info("Output: " + new String(env.getOutput(), "UTF-8"));
        this.release(blocks);
    }

    public void testBlockId() throws Exception {
        BlocksManager blocks = (BlocksManager)this.lookup(BlocksManager.ROLE);
        MockEnvironment env = getEnvironment("test");
        blocks.process("test1id", env);
        getLogger().info("Output: " + new String(env.getOutput(), "UTF-8"));
        this.release(blocks);
    }

    public void testBlocksSource() throws Exception {
        process("test");
    }

    public void testBlockSource1() throws Exception {
        process("test2");
    }

    public void testBlockSource2() throws Exception {
        process("test3");
    }
}
