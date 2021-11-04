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
package com.exadel.aem.toolkit.api.annotations.injectors;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.exadel.aem.toolkit.api.annotations.injectors.models.TestModelSelectors;

import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class RequestSelectorsInjectorIntegrationTest {

    @Rule
    public final AemContext context = new AemContext();

    private TestModelSelectors testModel;

    @Before
    public void beforeTest() {
        context.addModelsForClasses(TestModelSelectors.class);
        context.load().json("/com/exadel/aem/toolkit/api/annotations/injectors/page.json", "/content");
        context.registerInjectActivateService(new RequestSelectorsInjector());
        context.currentResource("/content/test");
    }

    @Test
    public void shouldReturnSelectorsString() {
        context.requestPathInfo().setSelectorString("selector1");
        testModel = context.request().adaptTo(TestModelSelectors.class);

        assertNotNull(testModel);
        assertEquals("selector1", testModel.getSelectorsString());
    }

    @Test
    public void shouldReturnSelectorsStringFromCollection() {
        context.requestPathInfo().setSelectorString("selector1.selector2");
        testModel = context.request().adaptTo(TestModelSelectors.class);

        assertNotNull(testModel);
        assertEquals(Arrays.asList("selector1", "selector2"), testModel.getSelectorsCollection());
    }

    @Test
    public void shouldReturnSelectorsStringFromList() {
        context.requestPathInfo().setSelectorString("selector1.selector2");
        testModel = context.request().adaptTo(TestModelSelectors.class);

        assertNotNull(testModel);
        assertEquals(Arrays.asList("selector1", "selector2"), testModel.getSelectorsList());
    }

    @Test
    public void shouldReturnSelectorsStringArray() {
        context.requestPathInfo().setSelectorString("selector1.selector2");
        testModel = context.request().adaptTo(TestModelSelectors.class);

        assertNotNull(testModel);
        assertArrayEquals(new String[]{"selector1", "selector2"}, testModel.getSelectorsArrayString());
    }

    @Test
    public void shouldReturnNullIfSelectorsIsEmpty() {
        testModel = context.request().adaptTo(TestModelSelectors.class);

        assertNotNull(testModel);
        assertNull(testModel.getSelectorsString());
    }

    @Test
    public void shouldReturnNullIfWrongType() {
        context.requestPathInfo().setSelectorString("selector1");
        testModel = context.request().adaptTo(TestModelSelectors.class);

        assertNotNull(testModel);
        assertNull(testModel.getSelectorsArrayInt());
        assertNull(testModel.getSelectorsListInt());
        assertNull(testModel.getSelectorsListModel());
        assertNull(testModel.getSelectorsSet());
        assertNull(testModel.getSelectorsTestModel());
        assertEquals(0, testModel.getSelectorsInt());
    }
}
