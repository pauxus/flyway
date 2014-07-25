/**
 * Copyright 2010-2014 Axel Fontaine
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

import java.util.Map;

import javax.sql.DataSource;

import org.flywaydb.core.api.FlywayConfiguration;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.util.Locations;

/**
 * Dummy Implementation of {@link FlywayConfiguration} for unit tests.
 */
public class FlywayConfigurationForTests implements FlywayConfiguration {

    private ClassLoader classLoader;
    private String[] locations;
    private String encoding;
    private String sqlMigrationPrefix;
    private String sqlMigrationSeparator;
    private String sqlMigrationSuffix;
    private MyCustomMigrationResolver[] migrationResolvers;

    public FlywayConfigurationForTests(ClassLoader contextClassLoader, String[] locations, String encoding,
            String sqlMigrationPrefix, String sqlMigrationSeparator, String sqlMigrationSuffix,
            MyCustomMigrationResolver... myCustomMigrationResolver) {
                this.classLoader = contextClassLoader;
                this.locations = locations;
                this.encoding = encoding;
                this.sqlMigrationPrefix = sqlMigrationPrefix;
                this.sqlMigrationSeparator = sqlMigrationSeparator;
                this.sqlMigrationSuffix = sqlMigrationSuffix;
                this.migrationResolvers = myCustomMigrationResolver;
    }

    public FlywayConfigurationForTests(ClassLoader contextClassLoader) {
        classLoader = contextClassLoader;
    }

    @Override
    public FlywayCallback[] getCallbacks() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public DataSource getDataSource() {
        return null;
    }

    @Override
    public MigrationResolver[] getResolvers() {
        return migrationResolvers;
    }

    @Override
    public String getInitDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MigrationVersion getInitVersion() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSqlMigrationSuffix() {
        return sqlMigrationSuffix;
    }

    @Override
    public String getSqlMigrationSeparator() {
        return sqlMigrationSeparator;
    }

    @Override
    public String getSqlMigrationPrefix() {
        return sqlMigrationPrefix;
    }

    @Override
    public String getPlaceholderSuffix() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPlaceholderPrefix() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String> getPlaceholders() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MigrationVersion getTarget() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTable() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getSchemas() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getLocations() {
        return this.locations;
    }

    @Override
    public String getEncoding() {
        return this.encoding;
    }

}
