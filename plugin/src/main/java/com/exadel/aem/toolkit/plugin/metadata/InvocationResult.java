/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exadel.aem.toolkit.plugin.metadata;

class InvocationResult {
    public static final InvocationResult NOT_DONE = new InvocationResult(false, null);

    private final boolean done;
    private final Object result;

    private InvocationResult(boolean done, Object result) {
        this.done = done;
        this.result = result;
    }

    public boolean isDone() {
        return done;
    }

    public Object getResult() {
        return result;
    }

    public static InvocationResult done(Object result) {
        return new InvocationResult(true, result);
    }
}
