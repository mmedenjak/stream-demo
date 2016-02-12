package com.ingemark.stream.config;

import com.ingemark.stream.util.IngemarkHibernateNamingStrategy;
import com.ingemark.stream.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

import static org.hibernate.cfg.AvailableSettings.*;
import static org.springframework.transaction.support.AbstractPlatformTransactionManager.SYNCHRONIZATION_NEVER;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class DataConfig implements TransactionManagementConfigurer {
    @Autowired
    private DataSource dataSource;

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        final LocalSessionFactoryBean b = new LocalSessionFactoryBean();
        b.setPackagesToScan("com.ingemark.gdsnsynccore.model.db", "com.ingemark.stream.model.db");
        b.setDataSource(dataSource);
        b.setPhysicalNamingStrategy(new IngemarkHibernateNamingStrategy());
        b.setHibernateProperties(Util.props(
                USE_NEW_ID_GENERATOR_MAPPINGS, "true",
                HBM2DDL_AUTO, "update",
                ORDER_INSERTS, "true",
                ORDER_UPDATES, "true",
                MAX_FETCH_DEPTH, "0",
                STATEMENT_FETCH_SIZE, "200",
                STATEMENT_BATCH_SIZE, "50",
                BATCH_VERSIONED_DATA, "true",
                USE_STREAMS_FOR_BINARY, "true",
                USE_SQL_COMMENTS, "true"
        ));
        return b;
    }

    @Bean
    public PlatformTransactionManager hibernateTxManager() {
        return Util.with(
                new HibernateTransactionManager(sessionFactory().getObject()),
                m -> m.setAllowResultAccessAfterCompletion(true),
                m -> m.setTransactionSynchronization(SYNCHRONIZATION_NEVER));
    }

    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return hibernateTxManager();
    }

    @Bean
    public TransactionTemplate txTemplate() {
        return new TransactionTemplate(hibernateTxManager());
    }

    @Bean
    public DataSource dataSource() {
        return new JndiDataSourceLookup().getDataSource("jdbc/stream");
    }
}