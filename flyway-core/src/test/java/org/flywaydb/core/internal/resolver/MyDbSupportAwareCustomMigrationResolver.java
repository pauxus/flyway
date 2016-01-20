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
import org.flywaydb.core.api.FlywayConfiguration;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.dbsupport.DbSupport;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
* Created by Axel on 3/7/14.
*/
public class MyDbSupportAwareCustomMigrationResolver extends MyConfigurationAwareCustomMigrationResolver implements DbSupportAware {


    private DbSupport dbSupport;

    @Override
    public void setDbSupport(DbSupport dbSupport) {
        this.dbSupport = dbSupport;
    }

    @Override
    public List<ResolvedMigration> resolveMigrations() {
        assertDbSupportIsSet();
        return super.resolveMigrations();
    }

    public void assertDbSupportIsSet() {
        assertThat(dbSupport, is(notNullValue()));
    }
}
