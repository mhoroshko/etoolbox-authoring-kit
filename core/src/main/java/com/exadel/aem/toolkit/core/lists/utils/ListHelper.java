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
package com.exadel.aem.toolkit.core.lists.utils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.lists.ListConstants;
import com.exadel.aem.toolkit.core.lists.models.SimpleListItem;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Contains methods for manipulations with Exadel Toolbox Lists
 */
public class ListHelper {

    /**
     * Default (instantiation-restricting) constructor
     */
    private ListHelper() {
    }

    /* --------------
       Public methods
       -------------- */

    /**
     * Retrieves a collection of Sling {@link Resource}s representing list entries stored under given {@code path}
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @return List of resources. If the path provided is invalid or cannot be resolved, an empty list
     * is returned
     */
    public static List<Resource> getResourceList(ResourceResolver resourceResolver, String path) {
        return getList(resourceResolver, path, Resource.class);
    }

    /**
     * Retrieves a collection of {@link SimpleListItem} values representing list entries stored under given {@code path}
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @return List of {@link SimpleListItem}s. If the path provided is invalid or cannot be resolved, an empty list
     * is returned
     */
    public static List<SimpleListItem> getList(ResourceResolver resourceResolver, String path) {
        return getList(resourceResolver, path, SimpleListItem.class);
    }

    /**
     * Retrieves a collection of items representing list entries stored under given {@code path} adapted to the provided
     * {@code itemType}
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @param itemType         {@code Class} reference representing type of entries required
     * @param <T>              Type of list entries, must be one adaptable from a Sling {@code Resource}
     * @return List of {@code <T>}-typed instances. If the path provided is invalid or cannot be resolved, or else
     * a non-adaptable {@code itemType} is given, an empty list is returned
     */
    public static <T> List<T> getList(ResourceResolver resourceResolver, String path, Class<T> itemType) {
        return getItemsStream(resourceResolver, path)
            .map(getMapperFunction(itemType))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Creates a list of entries under given {@code path} based on list of {@link SimpleListItem}
     * @param resourceResolver Sling {@link ResourceResolver} instance used to create the list
     * @param path             JCR path of the items list
     * @param listItems        List of {@link SimpleListItem}
     * @return Created page containing list of entries
     * @throws WCMException         If a page cannot be created
     * @throws PersistenceException If a page cannot be deleted or list item cannot be created
     */
    public static Page createList(@Nonnull ResourceResolver resourceResolver, String path, List<SimpleListItem> listItems)
        throws WCMException, PersistenceException {
        return createList(resourceResolver, path, listItems, SimpleListItem.class);
    }

    /**
     * Creates a list of entries under given {@code path} based on list of models.
     * @param resourceResolver Sling {@link ResourceResolver} instance used to create the list
     * @param path             JCR path of the items list
     * @param values           List of models
     * @param <T>              Model type
     * @return Created page containing list of entries
     * @throws PersistenceException If a page cannot be created
     * @throws WCMException         If a page cannot be deleted or list item cannot be created
     */
    public static <T> Page createList(@Nonnull ResourceResolver resourceResolver, String path, List<T> values, Class<T> itemType)
        throws PersistenceException, WCMException {
        BiFunction<Object, ObjectMapper, Resource> mapper = ListResourceUtils.getMapper(itemType);
        ObjectMapper objectMapper = new ObjectMapper();

        List<Resource> resources = ListUtils.emptyIfNull(values).stream()
            .map(value -> mapper.apply(value, objectMapper))
            .collect(Collectors.toList());

        return createResourceList(resourceResolver, path, resources);
    }

    /**
     * Creates a list of entries under given {@code path} based on key-value map. Each entry is a separate list item.
     * @param resourceResolver Sling {@link ResourceResolver} instance used to create the list
     * @param path             JCR path of the items list
     * @param values           Key-value map
     * @return Created page containing list of entries
     * @throws WCMException         If a page cannot be created
     * @throws PersistenceException If a page cannot be deleted or list item cannot be created
     */
    public static Page createList(@Nonnull ResourceResolver resourceResolver, String path, Map<String, Object> values)
        throws WCMException, PersistenceException {
        return createResourceList(resourceResolver, path, ListResourceUtils.mapToListItemResources(values));
    }

    /**
     * Creates a list of entries under given {@code path} based on list of {@link Resource}
     * @param resourceResolver Sling {@link ResourceResolver} instance used to create the list
     * @param path             JCR path of the items list
     * @param resources        List of {@link Resource}
     * @return Created page containing list of entries or ${@code null} if page cannot be created
     * @throws WCMException         If a page cannot be created
     * @throws PersistenceException If a page cannot be deleted or list item cannot be created
     */
    public static Page createResourceList(@Nonnull ResourceResolver resourceResolver, String path, Collection<Resource> resources)
        throws WCMException, PersistenceException {
        if (StringUtils.isBlank(path)) {
            throw new IllegalArgumentException("Path cannot be blank!");
        }

        Resource pageResource = resourceResolver.getResource(path);
        if (pageResource != null) {
            resourceResolver.delete(pageResource);
        }

        Page listPage = ListPageUtils.createListPage(resourceResolver, path);
        Resource list = resourceResolver.create(listPage.getContentResource(), ListConstants.NN_LIST, ListResourceUtils.LIST_PROPERTIES);

        for (Resource resource : resources) {
            Map<String, Object> filteredProperties = ListResourceUtils.excludeSystemProperties(resource.getValueMap());
            filteredProperties.put(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ListConstants.LIST_ITEM_RESOURCE_TYPE);
            resourceResolver.create(list, ResourceUtil.createUniqueChildName(list, CoreConstants.PN_LIST_ITEM), filteredProperties);
        }

        return listPage;
    }

    /**
     * Retrieves a collection of list entries stored under given {@code path} that is transformed into a key-value map.
     * The keys represent {@code jcr:title} property of the underlying resource while the values are the underlying
     * resources themselves. If several items have the same {@code jcr:title}, the last one is effective
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @return Map representing title-to-value pairs. If the path provided is invalid or cannot be resolved, an empty
     * map is returned
     */
    public static Map<String, Resource> getResourceMap(ResourceResolver resourceResolver, String path) {
        return getMap(resourceResolver, path, JcrConstants.JCR_TITLE, Resource.class);
    }

    /**
     * Retrieves a collection of list entries stored under given {@code path} that is transformed into a key-value map.
     * The keys represent the attribute of the underlying resources specified by the given {@code keyName} while the
     * values are the underlying resources themselves. If several items have the same {@code jcr:title}, the last one
     * is effective
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @param keyName          Item resource property that holds the key of the resulting map
     * @return Map representing title-to-value pairs. If the path provided is invalid or cannot be resolved, an empty
     * map is returned
     */
    public static Map<String, Resource> getResourceMap(ResourceResolver resourceResolver, String path, String keyName) {
        return getMap(resourceResolver, path, keyName, Resource.class);
    }

    /**
     * Retrieves a collection of list entries stored under given {@code path} that is transformed into a key-value map.
     * The keys represent {@code jcr:title} property of the underlying resource while the value represents {@code value}
     * property. If several items have the same {@code jcr:title}, the last one is effective
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @return Map representing title-to-value pairs. If the path provided is invalid or cannot be resolved, an empty
     * map is returned
     */
    public static Map<String, String> getMap(ResourceResolver resourceResolver, String path) {
        return getMap(
            resourceResolver,
            path,
            JcrConstants.JCR_TITLE,
            resource -> resource.getValueMap().get(CoreConstants.PN_VALUE, StringUtils.EMPTY));
    }

    /**
     * Retrieves a collection of list entries stored under given {@code path} that is transformed into a key-value map.
     * Keys represent the attribute of the underlying resources specified by the given {@code keyName}. Values are the
     * underlying resources themselves as adapted to the provided {@code itemType} model. If several items have the same
     * key, the last one is effective
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @param keyName          Item resource property that holds the key of the resulting map
     * @param itemType         {@code Class} reference representing type of map values required
     * @param <T>              Type of map values; must be one adaptable from a Sling {@code Resource}
     * @return Map containing {@code <T>}-typed instances. If the path provided is invalid or cannot be resolved,
     * or else a non-adaptable model {@code itemType} is given, an empty map is returned
     */
    public static <T> Map<String, T> getMap(ResourceResolver resourceResolver, String path, String keyName, Class<T> itemType) {
        return getMap(resourceResolver, path, keyName, getMapperFunction(itemType));
    }


    /* -----------------------
       Private utility methods
       ----------------------- */

    /**
     * Retrieves a {@code Map} collected from list entries under the provided path. Map keys are defined by
     * the {@code keyName}, while the values are defined by the provided {@code mapper} function
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @param keyName          Item resource property that holds the key of the resulting map
     * @param mapper           {@code Function} that converts a list item resource into the object of required type
     * @param <T>              Type of map values
     * @return Map containing {@code <T>}-typed instances
     */
    private static <T> Map<String, T> getMap(
        ResourceResolver resourceResolver,
        String path,
        String keyName,
        Function<Resource, T> mapper) {
        return getItemsStream(resourceResolver, path)
            .map(resource -> new ImmutablePair<>(
                resource.getValueMap().get(keyName, StringUtils.EMPTY),
                mapper.apply(resource)))
            .filter(pair -> pair.getRight() != null)
            .collect(Collectors.toMap(
                ImmutablePair::getLeft,
                ImmutablePair::getRight,
                (a, b) -> b,
                LinkedHashMap::new));
    }

    /**
     * Retrieves a {@code ResourceResolver} instance and gets  a sequence of {@code Resource}s under the provided path
     * as a Java {@code Stream} for further processing
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @return {@code Stream} or resource objects, or an empty stream
     */
    private static Stream<Resource> getItemsStream(ResourceResolver resourceResolver, String path) {
        if (resourceResolver == null) {
            return Stream.empty();
        }
        Resource listResource = getListResource(resourceResolver, path);
        return listResource != null
            ? StreamSupport.stream(Spliterators.spliteratorUnknownSize(listResource.listChildren(), Spliterator.ORDERED), false)
            : Stream.empty();
    }

    /**
     * Retrieves an Exadel Toolbox List page resource by the provided JCR {@code path}
     * @param resourceResolver {@code ResourceResolver} used to retrieve the resource
     * @param path             JCR path of the items list
     * @return {@code Resource object, or null if the resource resolver is missing or the path in unresolvable}
     */
    private static Resource getListResource(ResourceResolver resourceResolver, String path) {
        if (resourceResolver == null || StringUtils.isBlank(path)) {
            return null;
        }
        return Optional.of(resourceResolver)
            .map(resolver -> resolver.adaptTo(PageManager.class))
            .map(pageManager -> pageManager.getPage(path))
            .map(Page::getContentResource)
            .map(contentRes -> contentRes.getChild(ListConstants.NN_LIST))
            .orElse(null);
    }

    /**
     * Retrieves a {@code Function} used to convert a {@code Resource} object into the required value type
     * @param itemType {@code Class} reference representing type of entries required
     * @param <T>      Type of value
     * @return {@code Function} object
     */
    private static <T> Function<Resource, T> getMapperFunction(Class<T> itemType) {
        if (Resource.class.equals(itemType)) {
            return itemType::cast;
        }
        return resource -> resource.adaptTo(itemType);
    }
}
