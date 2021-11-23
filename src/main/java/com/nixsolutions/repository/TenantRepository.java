package com.nixsolutions.repository;

import com.nixsolutions.model.Tenant;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;


@Repository
public interface TenantRepository extends CrudRepository<Tenant, String> {

}
