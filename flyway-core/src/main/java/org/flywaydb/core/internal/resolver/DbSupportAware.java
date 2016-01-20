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
package org.flywaydb.core.internal.resolver;

import org.flywaydb.core.api.ConfigurationAware;
import org.flywaydb.core.internal.dbsupport.DbSupport;

/**
 * Injection interface for DbSupport. If a Resolver implements this interface, the current DbSupport instance
 * is automatically injected into it (during the creation of the {@link CompositeMigrationResolver}).
 *
 * Note that this interface is not official API and does prone to changes.
 */
public interface DbSupportAware extends ConfigurationAware {

    void setDbSupport(DbSupport dbSupport);

}
