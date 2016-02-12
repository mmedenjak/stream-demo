package com.ingemark.stream.util;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class IngemarkHibernateNamingStrategy extends ImprovedNamingStrategy implements PhysicalNamingStrategy {
    private static Identifier underscorize(Identifier name) {
        final String s = addUnderscores(name.toString());
        return Identifier.toIdentifier(s.endsWith("_") ? s : s + "_");
    }

    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return underscorize(name);
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return underscorize(name);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return underscorize(name);
    }
}
