/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.apache.sling.scripting.sightly.impl.utils;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.scripting.sightly.engine.ResourceResolution;
import org.apache.sling.scripting.sightly.render.RenderContext;
import org.jetbrains.annotations.NotNull;

public class ScriptUtils {

    private ScriptUtils() {}

    public static Resource resolveScript(ResourceResolver resolver, RenderContext renderContext, String scriptIdentifier) {
        SlingHttpServletRequest request = BindingsUtils.getRequest(renderContext.getBindings());
        Resource caller = ResourceResolution.getResourceForRequest(resolver, request);
        Resource result = ResourceResolution.getResourceFromSearchPath(caller, scriptIdentifier);
        if (result == null) {
            SlingScriptHelper sling = BindingsUtils.getHelper(renderContext.getBindings());
            if (sling != null) {
                caller = getResource(resolver, sling.getScript().getScriptResource());
                result = ResourceResolution.getResourceFromSearchPath(caller, scriptIdentifier);
            }
        }
        return result;
    }

    private static Resource getResource(@NotNull ResourceResolver resolver, @NotNull Resource resource) {
        String path = resource.getPath();
        if (path.startsWith("/")) {
            Resource resolved = resolver.resolve(path);
            if (ResourceUtil.isNonExistingResource(resolved)) {
                return null;
            }
            return resolved;
        } else {
            for (String sp : resolver.getSearchPath()) {
                String absolutePath = ResourceUtil.normalize(sp + path);
                if (absolutePath != null) {
                    Resource resolved = resolver.resolve(absolutePath);
                    if (!ResourceUtil.isNonExistingResource(resolved)) {
                        return resolved;
                    }
                }
            }
        }
        return null;
    }
}
