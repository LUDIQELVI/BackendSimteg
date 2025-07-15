package SimTeg.simulateur.BACKEND;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import SimTeg.simulateur.BACKEND.Repository.RoleRepository;
import SimTeg.simulateur.BACKEND.User.Role;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner runner(RoleRepository roleRepository){
		return args -> {
			if (roleRepository.findByName("ADMIN").isEmpty()){
				roleRepository.save(Role.builder()
						.name("ADMIN")
						.build());
			}
		};
	}
}
