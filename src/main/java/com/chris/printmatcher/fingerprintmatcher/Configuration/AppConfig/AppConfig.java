package com.chris.printmatcher.fingerprintmatcher.Configuration.AppConfig;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.sql.DataSource;

@Configuration
@EnableSwagger2
public class AppConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.chris.printmatcher.fingerprintmatcher.Controllers"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(fig_match_metaData());
    }

    private ApiInfo fig_match_metaData() {

        return new ApiInfoBuilder()
                .title("FINGER PRINT MATCHER API")
                .description("A finger print matcher app that works with U.are.U finger print scanner and uses " +
                        "a comparison algorithm developed by source afis")
                .version("1.0.0")
                .license("KrisE Signed App")
                .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0\"")
                .contact(new Contact("KRIS E. I.", "http://no_website.com", "christopher.eteka.200986@unn.edu.ng"))
                .build();
    }

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        return new HikariDataSource(config);
    }
}
