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

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.mysql.MySQLDbSupport;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.flywaydb.core.internal.resolver.FlywayConfigurationForTests.create;
import static org.flywaydb.core.internal.resolver.FlywayConfigurationForTests.createWithLocations;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for CompositeMigrationResolver.
 */
public class CompositeMigrationResolverSmallTest {
    @Test
    public void resolveMigrationsMultipleLocations() {
        FlywayConfigurationForTests config = createWithLocations("migration/subdir/dir2", "migration.outoforder", "migration/subdir/dir1");
        config.setResolvers(new MyCustomMigrationResolver());

        MigrationResolver migrationResolver = new CompositeMigrationResolver(null, config);

        Collection<ResolvedMigration> migrations = migrationResolver.resolveMigrations();
        List<ResolvedMigration> migrationList = new ArrayList<ResolvedMigration>(migrations);

        assertEquals(4, migrations.size());
        assertEquals("First", migrationList.get(0).getDescription());
        assertEquals("Late arrivals", migrationList.get(1).getDescription());
        assertEquals("Virtual Migration", migrationList.get(2).getDescription());
        assertEquals("Add foreign key", migrationList.get(3).getDescription());
    }

    @Test
    public void replaceResolverWithSubclass() {
        FlywayConfigurationForTests config = createWithLocations("migration/subdir");
        config.setSkipDefaultResolvers(true);
        config.setResolvers(new CustomSqlMigrationResolver());

        MigrationResolver migrationResolver = new CompositeMigrationResolver(null, config);

        Collection<ResolvedMigration> migrations = migrationResolver.resolveMigrations();
        List<ResolvedMigration> migrationList = new ArrayList<ResolvedMigration>(migrations);

        assertEquals(3, migrations.size());
        assertEquals("99.1", migrationList.get(0).getVersion().getVersion());
        assertEquals("99.1.1", migrationList.get(1).getVersion().getVersion());
        assertEquals("99.2.0", migrationList.get(2).getVersion().getVersion());
    }

    @Test
    public void customResolversHaveConfigurationInjected() {
        MyConfigurationAwareCustomMigrationResolver configResolver = new MyConfigurationAwareCustomMigrationResolver();
        MyDbSupportAwareCustomMigrationResolver dbSupportResolver = new MyDbSupportAwareCustomMigrationResolver();
        FlywayConfigurationForTests config = create();
        config.setResolvers(configResolver, dbSupportResolver);
        DbSupport dbSupport = new MySQLDbSupport(null);
        MigrationResolver migrationResolver = new CompositeMigrationResolver(dbSupport, config);

        migrationResolver.resolveMigrations();

        configResolver.assertFlywayConfigurationIsSet();
        dbSupportResolver.assertFlywayConfigurationIsSet();
        dbSupportResolver.assertDbSupportIsSet();
    }


    /**
     * Checks that migrations are properly collected, eliminating all exact duplicates.
     */
    @Test
    public void collectMigrations() {
        MigrationResolver migrationResolver = new MigrationResolver() {
            public List<ResolvedMigration> resolveMigrations() {
                List<ResolvedMigration> migrations = new ArrayList<ResolvedMigration>();

                migrations.add(createTestMigration(MigrationType.SPRING_JDBC, "1", "Description", "Migration1", 123));
                migrations.add(createTestMigration(MigrationType.SPRING_JDBC, "1", "Description", "Migration1", 123));
                migrations.add(createTestMigration(MigrationType.SQL, "2", "Description2", "Migration2", 1234));
                return migrations;
            }
        };
        Collection<MigrationResolver> migrationResolvers = new ArrayList<MigrationResolver>();
        migrationResolvers.add(migrationResolver);

        Collection<ResolvedMigration> migrations = CompositeMigrationResolver.collectMigrations(migrationResolvers);
        assertEquals(2, migrations.size());
    }

    /**
     * Checks that migrations are properly collected, eliminating all exact duplicates.
     */
    @Test
    public void collectMigrationsExactDuplicatesInDifferentLocations() {
        MigrationResolver migrationResolver = new MigrationResolver() {
            public List<ResolvedMigration> resolveMigrations() {
                ResolvedMigrationImpl testMigration = createTestMigration(MigrationType.SQL, "2", "Description2", "Migration2", 1234);
                testMigration.setPhysicalLocation("abc");
                ResolvedMigrationImpl testMigration2 = createTestMigration(MigrationType.SQL, "2", "Description2", "Migration2", 1234);
                testMigration2.setPhysicalLocation("xyz");

                List<ResolvedMigration> migrations = new ArrayList<ResolvedMigration>();
                migrations.add(testMigration);
                migrations.add(testMigration2);
                return migrations;
            }
        };
        MigrationResolver migrationResolver2 = new MigrationResolver() {
            public List<ResolvedMigration> resolveMigrations() {
                List<ResolvedMigration> migrations = new ArrayList<ResolvedMigration>();

                ResolvedMigrationImpl testMigration = createTestMigration(MigrationType.SQL, "2", "Description2", "Migration2", 1234);
                testMigration.setPhysicalLocation("def");
                migrations.add(testMigration);
                return migrations;
            }
        };
        Collection<MigrationResolver> migrationResolvers = Arrays.asList(migrationResolver, migrationResolver2);
        Collection<ResolvedMigration> migrations = CompositeMigrationResolver.collectMigrations(migrationResolvers);
        assertEquals(1, migrations.size());
    }

    @Test
    public void checkForIncompatibilitiesMessage() {
        ResolvedMigrationImpl migration1 = createTestMigration(MigrationType.SQL, "1", "First", "V1__First.sql", 123);
        migration1.setPhysicalLocation("target/test-classes/migration/validate/V1__First.sql");

        ResolvedMigrationImpl migration2 = createTestMigration(MigrationType.SPRING_JDBC, "1", "Description", "Migration1", 123);
        migration2.setPhysicalLocation("Migration1");

        List<ResolvedMigration> migrations = new ArrayList<ResolvedMigration>();
        migrations.add(migration1);
        migrations.add(migration2);

        try {
            CompositeMigrationResolver.checkForIncompatibilities(migrations);
        } catch (FlywayException e) {
            assertTrue(e.getMessage().contains("target/test-classes/migration/validate/V1__First.sql"));
            assertTrue(e.getMessage().contains("Migration1"));
        }
    }

    /**
     * Makes sure no validation exception is thrown.
     */
    @Test
    public void checkForIncompatibilitiesNoConflict() {
        List<ResolvedMigration> migrations = new ArrayList<ResolvedMigration>();
        migrations.add(createTestMigration(MigrationType.SPRING_JDBC, "1", "Description", "Migration1", 123));
        migrations.add(createTestMigration(MigrationType.SQL, "2", "Description2", "Migration2", 1234));

        CompositeMigrationResolver.checkForIncompatibilities(migrations);
    }

    /**
     * Creates a migration for our tests.
     *
     * @param aMigrationType The migration type.
     * @param aVersion       The version.
     * @param aDescription   The description.
     * @param aScript        The script.
     * @param aChecksum      The checksum.
     * @return The new test migration.
     */
    private ResolvedMigrationImpl createTestMigration(final MigrationType aMigrationType, final String aVersion, final String aDescription, final String aScript, final Integer aChecksum) {
        ResolvedMigrationImpl migration = new ResolvedMigrationImpl();
        migration.setVersion(MigrationVersion.fromVersion(aVersion));
        migration.setDescription(aDescription);
        migration.setScript(aScript);
        migration.setChecksum(aChecksum);
        migration.setType(aMigrationType);
        return migration;
    }

}
