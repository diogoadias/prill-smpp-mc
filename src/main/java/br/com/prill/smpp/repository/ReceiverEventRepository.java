package br.com.prill.smpp.repository;

import br.com.prill.smpp.entity.ReceiverEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiverEventRepository extends MongoRepository<ReceiverEvent, String> {
}
