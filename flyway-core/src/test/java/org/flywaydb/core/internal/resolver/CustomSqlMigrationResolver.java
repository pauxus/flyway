package org.flywaydb.core.internal.resolver;


import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.resolver.sql.SqlMigrationResolver;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.scanner.Resource;

public class CustomSqlMigrationResolver extends SqlMigrationResolver {

    @Override
    protected Pair<MigrationVersion, String> extractVersionAndDescription(String prefix, String separator, String suffix, Resource resource) {
        Pair<MigrationVersion, String> result = super.extractVersionAndDescription(prefix, separator, suffix, resource);

        return Pair.of(MigrationVersion.fromVersion("99." + result.getLeft().getVersion()), result.getRight());
    }
}
