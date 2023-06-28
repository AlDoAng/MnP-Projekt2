package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

class Worker extends AbstractBehavior<Worker.Message> {

    public interface Message {};

    //message from task
    public record TaskMessage(ActorRef<Scheduler.Message> msgFrom, ActorRef<Task.Message> taskElmt, ActorRef<Task.Message> taskObj, int action) implements Message {  }


    public static Behavior<Message> create(ActorRef<Scheduler.Message> msgFrom, ActorRef<Task.Message> taskElmt, ActorRef<Task.Message> taskObj, int action) {
        return Behaviors.setup(context -> action == 0 ? new Worker(context, msgFrom, taskElmt, taskObj, 0) : new Worker(context, msgFrom, taskElmt, taskObj, 1) );
    }

    private final ActorRef<Scheduler.Message> msgFrom;
    private final ActorRef<Task.Message> taskElmt;
    private final ActorRef<Task.Message> taskObj;
    private final int action;


    private Worker(ActorContext<Message> context, ActorRef<Scheduler.Message> msgFrom, ActorRef<Task.Message> taskElmt, ActorRef<Task.Message> taskObj, int action) {
        super(context);
        this.msgFrom = msgFrom;
        this.taskElmt = taskElmt;
        this.taskObj = taskObj;
        this.action = action;
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(TaskMessage.class, this::onExampleMessage)
                .build();
    }

    private Behavior<Message> onExampleMessage(TaskMessage msg) {
        getContext().getLog().info("I got an action: {}", this.action);
        return this;
    }
}
