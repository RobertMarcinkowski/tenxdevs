package eu.robm15.tenxdevs.repository;

import eu.robm15.tenxdevs.model.Experiment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExperimentRepository extends JpaRepository<Experiment, Integer> {

}
