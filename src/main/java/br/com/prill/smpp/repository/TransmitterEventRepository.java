package br.com.prill.smpp.repository;

import br.com.prill.smpp.entity.TransmitterEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransmitterEventRepository extends MongoRepository<TransmitterEvent, String> {
}
