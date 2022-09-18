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
package com.exadel.aem.toolkit.plugin.maven;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.exadel.aem.toolkit.plugin.base.TestConstants.RESOURCE_FOLDER_COMMON;
import static com.exadel.aem.toolkit.plugin.base.TestConstants.RESOURCE_FOLDER_COMPONENT;
import static com.exadel.aem.toolkit.plugin.base.TestConstants.RESOURCE_FOLDER_DEPENDSON;
import static com.exadel.aem.toolkit.plugin.base.TestConstants.RESOURCE_FOLDER_WIDGET;

import com.exadel.aem.toolkit.plugin.base.TestConstants;

public class PluginContextRenderingRule extends PluginContextRule {

    private static final Logger LOG = LoggerFactory.getLogger(PluginContextRenderingRule.class);

    static final String INSTANTIATION_EXCEPTION_MESSAGE = "Could not start testing class {}";
    static final String CLEANUP_EXCEPTION_MESSAGE = "Could not complete testing class {}";

    private static final String KEYWORD_ANNOTATION = "Annotation";
    private static final String KEYWORD_DEPENDSON = "DependsOn";
    private static final String KEYWORD_WIDGET = "Widget";

    private static final String SUFFIX_PATTERN = "(Widget|Annotation)$";

    private final FileSystem fileSystem;

    public PluginContextRenderingRule(FileSystem fileSystem) {
        super();
        this.fileSystem = fileSystem;
    }

    public void test(Class<?> testable) {
        String subfolderName = RESOURCE_FOLDER_COMPONENT;
        if (testable.getSimpleName().endsWith(KEYWORD_WIDGET)) {
            subfolderName = RESOURCE_FOLDER_WIDGET;
        } else if (testable.getSimpleName().startsWith(KEYWORD_DEPENDSON)) {
            subfolderName = RESOURCE_FOLDER_DEPENDSON;
        } else if (testable.getSimpleName().endsWith(KEYWORD_ANNOTATION)) {
            subfolderName = RESOURCE_FOLDER_COMMON;
        }
        test(testable,
            subfolderName,
            StringUtils.uncapitalize(RegExUtils.removePattern(testable.getSimpleName(), SUFFIX_PATTERN)));
    }

    public void test(Class<?> testable, String... pathElements) {
        test(testable, null, Paths.get(TestConstants.CONTENT_ROOT_PATH, pathElements).toAbsolutePath(), null);
    }

    @SuppressWarnings("SameParameterValue")
    public void test(Class<?> testable, String createdFilesPath, Path sampleFilesPath) {
        test(testable, createdFilesPath, sampleFilesPath, null);
    }

    @SuppressWarnings("SameParameterValue")
    public void test(Class<?> testable, Path sampleFilesPath, Consumer<FileSystem> preparation) {
        test(testable, null, sampleFilesPath, preparation);
    }

    private void test(Class<?> testable, String createdFilesPath, Path sampleFilesPath, Consumer<FileSystem> preparation) {
        if (preparation != null) {
            preparation.accept(fileSystem);
        }
        Path effectivePath = createdFilesPath != null
            ? fileSystem.getPath(TestConstants.PACKAGE_ROOT_PATH + createdFilesPath)
            : null;
        try {
            boolean result = FileRenderingUtil.doRenderingTest(
                fileSystem,
                testable.getName(),
                effectivePath,
                sampleFilesPath);
            Assert.assertTrue(result);
        } catch (ClassNotFoundException cnfEx) {
            LOG.error(INSTANTIATION_EXCEPTION_MESSAGE, testable.getName(), cnfEx);
        } catch (IOException ioEx) {
            LOG.error(CLEANUP_EXCEPTION_MESSAGE, testable.getName(), ioEx);
        }
    }
}
