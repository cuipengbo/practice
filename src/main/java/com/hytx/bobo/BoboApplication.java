package com.hytx.bobo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
//@EnableEurekaClient
//因dao层使用@mapper注解，此处加不加@maperscan都可以（自己理解） tk....包
//@MapperScan("com.hytx.bobo.dao")
@ComponentScan(basePackages = {"com.hytx.bobo"})
public class BoboApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoboApplication.class, args);
//		new SpringApplicationBuilder(BoboApplication.class).build(args).run();
	}

}
