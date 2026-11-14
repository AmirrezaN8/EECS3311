package career;

import java.time.Instant;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AgentEventPublisher {
    public record AgentEvent(Instant at,String type,String message) {}
    @FunctionalInterface public interface ProgressObserver {void onEvent(AgentEvent event);}
    private final CopyOnWriteArrayList<ProgressObserver> observers=new CopyOnWriteArrayList<>();
    public void attach(ProgressObserver o){observers.add(o);}
    public void detach(ProgressObserver o){observers.remove(o);}
    public void publish(String type,String message){AgentEvent e=new AgentEvent(Instant.now(),type,message);for(var o:observers)try{o.onEvent(e);}catch(RuntimeException ignored){/* A display failure must not abort saved work. */}}
}
