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
package org.apache.cocoon.components.jxforms.validation.schematron;

/**
 * Represents a Schematron report element.
 *
 * <report test="when">message</report>
 * is equivalent to
 * <assert test="not(when)">message</assert>
 *
 * example:
 * <report test="bone">This dog has a bone.</report>
 *
 * @author  Ivelin Ivanov, ivelin@acm.org, ivelin@iname.com
 * @version CVS $Id: Report.java,v 1.2 2004/03/05 13:01:58 bdelacretaz Exp $
 */
public class Report extends Assert {
}
