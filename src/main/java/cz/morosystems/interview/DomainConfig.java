package cz.morosystems.interview;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Configuration
@EntityScan("cz.morosystems.interview.domain")
@EnableJpaRepositories("cz.morosystems.interview.repository")
@EnableTransactionManagement
class DomainConfig {
}
