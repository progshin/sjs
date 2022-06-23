package triple.sjs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import java.sql.SQLException;

@SpringBootApplication
public class SjsApplication {

	public static void main(String[] args) throws SQLException {
		SpringApplication.run(SjsApplication.class, args);

	}

}
