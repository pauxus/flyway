/**
 * Copyright 2010-2015 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.util;

import org.flywaydb.core.api.ConfigurationAware;
import org.flywaydb.core.api.FlywayConfiguration;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.resolver.DbSupportAware;

/**
 * Utility class for interfaced based injection.
 */
public class InjectionUtils {

    private InjectionUtils() {
        // Utility class
    }

    /**
     * Injects the given flyway configuration into the target object if target implements the
     * {@link ConfigurationAware} interface. Does nothing if target is not configuration aware.
     * @param target The object to inject the configuration into.
     * @param configuration The configuration to inject.
     */
    public static <T> T injectFlywayConfiguration(T target, FlywayConfiguration configuration) {
        if (target instanceof ConfigurationAware) {
            ((ConfigurationAware) target).setFlywayConfiguration(configuration);
        }
        return target;
    }

    /**
     * Injects the given flyway configuration into the target object if target implements the
     * {@link } interface. Does nothing if target is not configuration aware.
     * @param target The object to inject the configuration into.
     * @param dbSupport The dbSupport instance to inject.
     */
    public static <T> T injectDbSupport(T target, DbSupport dbSupport) {
        if (target instanceof DbSupportAware) {
            ((DbSupportAware) target).setDbSupport(dbSupport);
        }
        return target;
    }

    public static synchronized <T> T instantiateAndInjectConfiguration(String className, ClassLoader classLoader, FlywayConfiguration config) throws Exception {
        T result = ClassUtils.instantiate(className, classLoader);
        injectFlywayConfiguration(result, config);
        return result;
    }

    /**
     * Injects the given configuration and DbSupport in to all elements of the List.
     * @param targets The List containing the Objects to be in injected into
     * @param configuration The configuration to inject
     * @param dbSupport The DbSupport to inject
     * @param <T> The type of the elements (usually Callback or MigrationResolver)
     */
    public static synchronized  <T> void injectFlywayConfiguration(Iterable<T> targets, FlywayConfiguration configuration, DbSupport dbSupport) {
        for (T target : targets) {
            injectFlywayConfiguration(target, configuration);
            injectDbSupport(target, dbSupport);
        }
    }
}
