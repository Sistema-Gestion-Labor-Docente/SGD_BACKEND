package co.edu.unicauca.sgd.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import co.edu.unicauca.sgd.api.domain.Plan;

public interface PlanRepository  extends JpaRepository<Plan, Integer>, JpaSpecificationExecutor<Plan> {

}
