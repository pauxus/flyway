package org.flywaydb.core;

import org.flywaydb.core.api.resolver.MigrationResolver;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class InjectionTest {

    @Test
    public void customCallbacksMustContainFlywayConfiguration() {
        Properties properties = createProperties("callback");

        FlywayCallbackImpl callback = new FlywayCallbackImpl();

        final Flyway flyway = new Flyway();
        flyway.configure(properties);
        flyway.setCallbacks(callback);

        callback.assertFlywayConfigurationSet();
    }

    @Test
    public void customCallbacksViaPropertiesMustContainFlywayConfiguration() {
        Properties properties = createProperties("callback");
        properties.setProperty("flyway.callbacks", FlywayCallbackImpl.class.getName());

        final Flyway flyway = new Flyway();
        flyway.configure(properties);

        FlywayCallbackImpl callback = (FlywayCallbackImpl) flyway.getCallbacks()[0];
        callback.assertFlywayConfigurationSet();
    }

    @Test
    public void customResolversMustContainFlywayConfiguration() {
        Properties properties = createProperties("resolver");

        FlywayResolverImpl resolver = new FlywayResolverImpl();

        final Flyway flyway = new Flyway();
        flyway.configure(properties);
        flyway.setResolvers(resolver);

        resolver.assertFlywayConfigurationSet();
    }

    @Test
    public void customResolversViaPropertiesMustContainFlywayConfiguration() {
        Properties properties = createProperties("resolver");
        properties.setProperty("flyway.resolvers", FlywayResolverImpl.class.getName());

        final Flyway flyway = new Flyway();
        flyway.configure(properties);

        for (MigrationResolver resolver : flyway.getResolvers()) {
            if (resolver instanceof FlywayResolverImpl) {
                ((FlywayResolverImpl) resolver).assertFlywayConfigurationSet();
                return;
            }
        }

        Assert.fail("Flyway instance does not contain expected instance of FlywayResolverImpl.");
    }

    private Properties createProperties(String name) {
        Properties properties = new Properties();
        properties.setProperty("flyway.user", "sa");
        properties.setProperty("flyway.password", "");
        properties.setProperty("flyway.url", "jdbc:h2:mem:flyway_test_injection_" + name + ";DB_CLOSE_DELAY=-1");
        properties.setProperty("flyway.driver", "org.h2.Driver");
        properties.setProperty("flyway.locations", "migration/dbsupport/h2/sql/domain");
        properties.setProperty("flyway.validateOnMigrate", "false");
        return properties;
    }

}
