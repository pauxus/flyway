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
package org.flywaydb.core.internal.resolver.sql;

import org.flywaydb.core.api.FlywayConfiguration;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.resolver.DbSupportAware;
import org.flywaydb.core.internal.resolver.MigrationInfoHelper;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.util.*;
import org.flywaydb.core.internal.util.scanner.Resource;
import org.flywaydb.core.internal.util.scanner.Scanner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.zip.CRC32;

/**
 * Migration resolver for sql files on the classpath. The sql files must have names like
 * V1__Description.sql or V1_1__Description.sql.
 */
public class SqlMigrationResolver implements MigrationResolver, DbSupportAware {
    /**
     * Database-specific support.
     */
    private DbSupport dbSupport;

    private FlywayConfiguration flywayConfiguration;
    private List<ResolvedMigration> migrations;
    private Scanner scanner;
    private PlaceholderReplacer placeholderReplacer;

    @Override
    public void setFlywayConfiguration(FlywayConfiguration flywayConfiguration) {
        this.flywayConfiguration = flywayConfiguration;
    }

    @Override
    public void setDbSupport(DbSupport dbSupport) {
        this.dbSupport = dbSupport;
    }

    @Override
    public Collection<ResolvedMigration> resolveMigrations() {
        migrations = new ArrayList<ResolvedMigration>();
        scanner = Scanner.create(flywayConfiguration.getClassLoader());

        placeholderReplacer = createPlaceholderReplacer(flywayConfiguration);
        for (Location location : new Locations(flywayConfiguration.getLocations()).getLocations()) {
            scanForMigrations(location, flywayConfiguration.getSqlMigrationPrefix(), flywayConfiguration.getSqlMigrationSeparator(), flywayConfiguration.getSqlMigrationSuffix());
            scanForMigrations(location, flywayConfiguration.getRepeatableSqlMigrationPrefix(), flywayConfiguration.getSqlMigrationSeparator(), flywayConfiguration.getSqlMigrationSuffix());
        }

        Collections.sort(migrations, new ResolvedMigrationComparator());

        return migrations;
    }

    private PlaceholderReplacer createPlaceholderReplacer(FlywayConfiguration config) {
        if (config.isPlaceholderReplacement()) {
            return new PlaceholderReplacer(config.getPlaceholders(), config.getPlaceholderPrefix(), config.getPlaceholderSuffix());
        }
        return PlaceholderReplacer.NO_PLACEHOLDERS;
    }

    public void scanForMigrations(Location location, String prefix, String separator, String suffix) {
        for (Resource resource : scanner.scanForResources(location, prefix, suffix)) {
            Pair<MigrationVersion, String> info =
                    MigrationInfoHelper.extractVersionAndDescription(resource.getFilename(), prefix, separator, suffix);

            ResolvedMigrationImpl migration = new ResolvedMigrationImpl();
            migration.setVersion(info.getLeft());
            migration.setDescription(info.getRight());
            migration.setScript(extractScriptName(resource, location));
            migration.setChecksum(calculateChecksum(resource.loadAsBytes()));
            migration.setType(MigrationType.SQL);
            migration.setPhysicalLocation(resource.getLocationOnDisk());
            migration.setExecutor(new SqlMigrationExecutor(dbSupport, resource, placeholderReplacer, flywayConfiguration.getEncoding()));
            migrations.add(migration);
        }
    }

    /**
     * Extracts the script name from this resource.
     *
     * @param resource The resource to process.
     * @return The script name.
     */
    /* private -> for testing */ String extractScriptName(Resource resource, Location location) {
        if (location.getPath().isEmpty()) {
            return resource.getLocation();
        }

        return resource.getLocation().substring(location.getPath().length() + 1);
    }

    /**
     * Calculates the checksum of these bytes.
     *
     * @param bytes The bytes to calculate the checksum for.
     * @return The crc-32 checksum of the bytes.
     */
    private static int calculateChecksum(byte[] bytes) {
        final CRC32 crc32 = new CRC32();
        crc32.update(bytes);
        return (int) crc32.getValue();
    }
}
