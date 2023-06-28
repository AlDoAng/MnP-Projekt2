package com.example;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

class Worker extends AbstractBehavior<Worker.Message> {

    public interface Message {};

    private final int taskId;

    private Worker(ActorContext<Message> context, int taskId) {
        super(context);
        this.taskId = taskId;
    }

    public static Behavior<Message> create() {
        return Behaviors.setup(context -> new Worker(context, 0));
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .build();
    }
}
