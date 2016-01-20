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
package org.flywaydb.core.internal.resolver.spring;

import org.flywaydb.core.api.*;
import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.MigrationInfoProvider;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.MigrationInfoHelper;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.util.*;
import org.flywaydb.core.internal.util.scanner.Scanner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Migration resolver for Spring Jdbc migrations. The classes must have a name like V1 or V1_1_3 or V1__Description
 * or V1_1_3__Description.
 */
public class SpringJdbcMigrationResolver implements MigrationResolver, ConfigurationAware {

    private FlywayConfiguration flywayConfiguration;
    private Scanner scanner;
    private List<ResolvedMigration> migrations;

    @Override
    public void setFlywayConfiguration(FlywayConfiguration flywayConfiguration) {
        this.flywayConfiguration = flywayConfiguration;
    }

    @Override
    public Collection<ResolvedMigration> resolveMigrations() {
        migrations = new ArrayList<ResolvedMigration>();

        scanner = Scanner.create(flywayConfiguration.getClassLoader());
        for (Location location : new Locations(flywayConfiguration.getLocations()).getLocations()) {
            if (!location.isClassPath()) continue;
            resolveMigrationsFromSingleLocation(location);
        }

        Collections.sort(migrations, new ResolvedMigrationComparator());
        InjectionUtils.injectFlywayConfiguration(migrations, flywayConfiguration);

        return migrations;
    }

    protected Collection<ResolvedMigration> resolveMigrationsFromSingleLocation(Location location) {
        try {
            Class<?>[] classes = scanner.scanForClasses(location, SpringJdbcMigration.class);
            for (Class<?> clazz : classes) {
                SpringJdbcMigration springJdbcMigration = ClassUtils.instantiate(clazz.getName(), flywayConfiguration.getClassLoader());

                ResolvedMigrationImpl migrationInfo = extractMigrationInfo(springJdbcMigration);
                migrationInfo.setPhysicalLocation(ClassUtils.getLocationOnDisk(clazz));
                migrationInfo.setExecutor(new SpringJdbcMigrationExecutor(springJdbcMigration));

                migrations.add(migrationInfo);
            }
        } catch (Exception e) {
            throw new FlywayException("Unable to resolve Spring Jdbc Java migrations in location: " + location, e);
        }

        return migrations;
    }

    /**
     * Extracts the migration info from this migration.
     *
     * @param springJdbcMigration The migration to analyse.
     * @return The migration info.
     */
    /* private -> testing */ ResolvedMigrationImpl extractMigrationInfo(SpringJdbcMigration springJdbcMigration) {
        Integer checksum = null;
        if (springJdbcMigration instanceof MigrationChecksumProvider) {
            MigrationChecksumProvider checksumProvider = (MigrationChecksumProvider) springJdbcMigration;
            checksum = checksumProvider.getChecksum();
        }

        MigrationVersion version;
        String description;
        if (springJdbcMigration instanceof MigrationInfoProvider) {
            MigrationInfoProvider infoProvider = (MigrationInfoProvider) springJdbcMigration;
            version = infoProvider.getVersion();
            description = infoProvider.getDescription();
            if (!StringUtils.hasText(description)) {
                throw new FlywayException("Missing description for migration " + version);
            }
        } else {
            String shortName = ClassUtils.getShortName(springJdbcMigration.getClass());
            String prefix;
            if (shortName.startsWith("V") || shortName.startsWith("R")) {
                prefix = shortName.substring(0, 1);
            } else {
                throw new FlywayException("Invalid Jdbc migration class name: " + springJdbcMigration.getClass().getName()
                        + " => ensure it starts with V or R," +
                        " or implement org.flywaydb.core.api.migration.MigrationInfoProvider for non-default naming");
            }
            Pair<MigrationVersion, String> info = MigrationInfoHelper.extractVersionAndDescription(shortName, prefix, "__", "");
            version = info.getLeft();
            description = info.getRight();
        }

        ResolvedMigrationImpl resolvedMigration = new ResolvedMigrationImpl();
        resolvedMigration.setVersion(version);
        resolvedMigration.setDescription(description);
        resolvedMigration.setScript(springJdbcMigration.getClass().getName());
        resolvedMigration.setChecksum(checksum);
        resolvedMigration.setType(MigrationType.SPRING_JDBC);
        return resolvedMigration;
    }
}
