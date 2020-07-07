package de.hzg.wpi.waltz.magix.connector;

import de.hzg.wpi.waltz.magix.client.Magix;
import de.hzg.wpi.waltz.magix.client.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 02.07.2020
 */
@Path("/connector")
public class MagixTangoConnectorEndpoint {
    private final Logger logger = LoggerFactory.getLogger(MagixTangoConnectorEndpoint.class);

    private final Magix magix;

    public MagixTangoConnectorEndpoint(Magix magix) {
        this.magix = magix;
        this.magix.observe()
                .filter(message -> "tango".equalsIgnoreCase(message.target))
                .subscribe(this::onEvent);//TODO dispose
    }

    @GET
    public Response get() {
        return Response.ok().build();
    }


    private void onEvent(Message<?> message) {
        logger.debug("Got message with action {}", message.action);

        new TangoAction(
                ActionExecutors.newInstance(message.action),
                Proxy.newProxyInstance(TangoPayload.class.getClassLoader(), new Class[]{TangoPayload.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                        System.out.println("invoke");
                        return null;
                    }
                })).subscribe(response -> {
            magix.broadcast(Message.builder().build());
        });
    }
}
