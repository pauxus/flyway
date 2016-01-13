package org.flywaydb.core;

import org.flywaydb.core.api.ConfigurationAware;
import org.flywaydb.core.api.FlywayConfiguration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.junit.Assert;

import java.util.Collection;

import static org.junit.Assert.assertNotNull;

/**
 * Dummy implementation for Resolvers to check configuration injection.
 */
public class FlywayResolverImpl implements MigrationResolver, ConfigurationAware {

    private FlywayConfiguration flywayConfiguration;

    @Override
    public void setFlywayConfiguration(FlywayConfiguration flywayConfiguration) {

        this.flywayConfiguration = flywayConfiguration;
    }

    @Override
    public Collection<ResolvedMigration> resolveMigrations() {
        return null;
    }

    public void assertFlywayConfigurationSet() {
        assertNotNull("Configuration must have been set", flywayConfiguration);
    }
}
