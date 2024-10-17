package ru.lanit.minobr.keycloak.journals.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.lanit.minobr.keycloak.journals.models.ObjectNsi;

import java.util.UUID;

@Repository
public interface ObjectNsiRepository extends JpaRepository<ObjectNsi, UUID> {

    @Query(value = "SELECT * FROM eca_proxy.object_nsi WHERE uid = :uid limit 1", nativeQuery = true)
    ObjectNsi findByUuid(@Param("uid") String uid);

}

//@Repository
//public interface ObjectNsiRepository extends JpaRepository<ObjectNsi, UUID> {
//
//    @Query(value = "SELECT * FROM eca_proxy.object_nsi WHERE uid = :uid limit 1", nativeQuery = true)
//    ObjectNsi findByUuid(@Param("uid") String uid);
//
//}