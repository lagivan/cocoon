/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.forms.event;

import org.apache.cocoon.forms.formmodel.Widget;

/**
 * An event raised when a repeater widget row is added, removed or reordered.
 * 
 * @version $Id$
 */
public class RepeaterEvent extends WidgetEvent {

    private int row = -1;
    private RepeaterEventAction action = null;

    /**
     * @param sourceWidget The repeater widget dispatching the event.
     * @param action The action for which this event is being triggered.
     */
    public RepeaterEvent(Widget sourceWidget, RepeaterEventAction action) {
        super(sourceWidget);
        this.action = action;
    }
    
    /**
     * @param sourceWidget The repeater widget dispatching the event.
     * @param action The action for which this event is being triggered.
     * @param row The row index for which the event is being generated. 
     */
    public RepeaterEvent(Widget sourceWidget, RepeaterEventAction action, int row) {
        super(sourceWidget);
        this.action = action;        
        this.row = row;
    }

    /**
     * @return Returns the row index for which the event has been generated.
     */
    public int getRow() {
        return row;
    }
    /**
     * @return Returns the action.
     */
    public RepeaterEventAction getAction() {
        return action;
    }
}
