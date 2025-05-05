package br.com.prill.smpp.component;

import br.com.prill.smpp.manager.SMSCServerManager;
import br.com.prill.smpp.service.DeliverSmService;
import br.com.prill.smpp.service.SubmitSmservice;
import com.cloudhopper.smpp.impl.DefaultSmppServer;
import com.cloudhopper.smpp.SmppServerConfiguration;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SmppServerComponent {

    private DefaultSmppServer smppServer;

    @Value("${smpp.server.port}")
    int smppServerPort;

    @Value("${smpp.server.max-connection-size}")
    int maxConnectionSize;

    @Value("${smpp.server.password}")
    String password;

    @PostConstruct
    public void start() {
        try {
            SmppServerConfiguration config = new SmppServerConfiguration();
            config.setPort(smppServerPort);
            config.setMaxConnectionSize(maxConnectionSize);
            config.setNonBlockingSocketsEnabled(true);

            smppServer = new DefaultSmppServer(config, new SMSCServerManager(new DeliverSmService(), new SubmitSmservice(), password));
            smppServer.start();
            log.info("SMPP Server started on port {}", config.getPort());
        } catch (Exception e) {
            log.error("Failed to start SMPP Server", e);
        }
    }

    @PreDestroy
    public void stop() {
        if (smppServer != null) {
            log.info("Stopping SMPP Server...");
            smppServer.stop();
        }
    }
}

