package br.com.fa7.airplanetickets.configuracoes;

import java.beans.PropertyVetoException;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.EmptyInterceptor;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import br.com.fa7.airplanetickets.modelo.auditoria.AuditInterceptor;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "br.com.fa7.airplanetickets.modelo.repositorios")
@EnableJpaAuditing
public class ConfiguracaoBD {

	@Bean
	public DataSource dataSource() throws IllegalStateException, PropertyVetoException {
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		dataSource.setDriverClass("org.postgresql.Driver");
		dataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/airplanetickets");
		dataSource.setUser("postgres");
		dataSource.setPassword("postgres");

		// Inicio C3P0 -
		// http://www.mchange.com/projects/c3p0/index.html#maxIdleTime
		dataSource.setInitialPoolSize(5);
		dataSource.setMinPoolSize(5);
		dataSource.setMaxPoolSize(20);
		dataSource.setAcquireIncrement(1);
		dataSource.setMaxIdleTime(300);
		dataSource.setMaxStatements(50);
		dataSource.setIdleConnectionTestPeriod(1000);
		dataSource.setPreferredTestQuery("SELECT 1"); // http://stackoverflow.com/questions/3668506/efficient-sql-test-query-or-validation-query-that-will-work-across-all-or-most
		// Fim C3P0

		return dataSource;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws Exception {
		LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactoryBean.setDataSource(dataSource());
		entityManagerFactoryBean.setPackagesToScan("br.com.fa7.airplanetickets.modelo.entidades");
		entityManagerFactoryBean.setPersistenceProviderClass(HibernatePersistenceProvider.class);
		entityManagerFactoryBean.setJpaDialect(hibernateJpaDialect());

		Properties jpaProperties = new Properties();
		jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
		jpaProperties.put("hibernate.ejb.naming_strategy", "org.hibernate.cfg.ImprovedNamingStrategy");
		jpaProperties.put("hibernate.show_sql", "true");
		jpaProperties.put("hibernate.format_sql", "true");
		jpaProperties.put("hibernate.hbm2ddl.auto", "update");

		jpaProperties.put("hibernate.ejb.interceptor", hibernateInterceptor()); // adicionando
																				// auditoria

		entityManagerFactoryBean.setJpaProperties(jpaProperties);
		return entityManagerFactoryBean;
	}

	@Bean
	public HibernateJpaDialect hibernateJpaDialect() {
		return new HibernateJpaDialect();
	}

	@Bean
	public JpaTransactionManager transactionManager() throws Exception {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
		return transactionManager;
	}

	@Bean
	public EmptyInterceptor hibernateInterceptor() {
		return new AuditInterceptor();
	}

//	@Bean
//	public DataSourceInitializer dataSourceInitializer() throws Exception{
//		ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
//		resourceDatabasePopulator.addScript(new ClassPathResource("/data.sql"));
//
//		DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
//		dataSourceInitializer.setDataSource(dataSource());
//		dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);
//		return dataSourceInitializer;
//	}

}
