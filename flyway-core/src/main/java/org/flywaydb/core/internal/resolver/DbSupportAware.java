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
