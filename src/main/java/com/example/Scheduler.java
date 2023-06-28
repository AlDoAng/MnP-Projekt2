package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.delivery.DurableProducerQueue;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import java.util.LinkedList;
import java.util.List;

public class Scheduler extends AbstractBehavior<Scheduler.Message>{

    //bekomt Tasks Liste
    //erstellt Worker für jedes Element der Liste
    //es dürfen maximal 20 Workers laufen

    public interface Message {};
    public record NumberWorkers(ActorRef<Task.Message> replyTo) implements Message{};
    public record TaskEnde(ActorRef<Task.Message> replyTo, int numberWorkersFree) implements Message{};

    //enthält Tasks, wenn es mehr als 20 Workers laufen

    private final int maxWorkers = 20;
    private int currentWorkers = 0;


    private Scheduler(ActorContext<Message> context) {
        super(context);
    }

    public static Behavior<Message> create() {

        return Behaviors.setup(context -> new Scheduler(context));
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(NumberWorkers.class, this::onNumberWorkers)
                .onMessage(TaskEnde.class, this::onTaskEnde)
                .build();
    }

    private  Behavior<Message> onTaskEnde(TaskEnde msg) {
        return this;
    }

    private Behavior<Message> onNumberWorkers(NumberWorkers msg) {
        msg.replyTo.tell(new Task.NumberWorkers(this.getContext().getSelf(), maxWorkers-currentWorkers));
        return this;
    }


}
