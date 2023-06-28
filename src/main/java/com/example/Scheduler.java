package com.example;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import java.util.LinkedList;
import java.util.Queue;

public class Scheduler extends AbstractBehavior<Scheduler.Message>{

    //bekomt Tasks Liste
    //erstellt Worker für jedes Element der Liste
    //es dürfen maximal 20 Workers laufen

    public interface Message {};

    //enthält Tasks, wenn es mehr als 20 Workers laufen
    private final Queue<Task> taskQueue;
    private final int maxWorkers = 20;
    private int currentWorkers = 0;

    private Scheduler(ActorContext<Message> context) {
        super(context);
        this.taskQueue = new LinkedList<>();
    }

    public static Behavior<Message> create() {
        return Behaviors.setup(context -> new Scheduler(context));
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .build();
    }


}
