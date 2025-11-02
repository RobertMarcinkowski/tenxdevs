package eu.robm15.tenxdevs.controller;

import eu.robm15.tenxdevs.model.Experiment;
import eu.robm15.tenxdevs.repository.ExperimentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TenxdevsController {

    @Autowired
    private ExperimentRepository experimentRepository;

    @GetMapping("/tenxdevs")
    public String tenxdevs(@RequestParam(value = "name", defaultValue = "World") String name) {
        var exp = new Experiment();
        exp.setName(name);
        experimentRepository.save(exp);
        return String.format("Docker test 7. Hello %s!", name);
    }

}
