package com.prj.nosql.repository;

import com.prj.nosql.model.Classe;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ClasseRepository extends MongoRepository<Classe, String> {
    List<Classe> findByNiveau(Classe.Niveau niveau);
}