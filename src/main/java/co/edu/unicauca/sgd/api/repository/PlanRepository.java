package co.edu.unicauca.sgd.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sgd.api.domain.Plan;

@Repository
public interface PlanRepository  extends JpaRepository<Plan, Integer>, JpaSpecificationExecutor<Plan> {

}
