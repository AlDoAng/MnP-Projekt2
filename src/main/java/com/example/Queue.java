package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.LinkedList;

public class Queue extends AbstractBehavior<Queue.Message> {
    public interface Message {}
    private final java.util.Queue<ActorRef<Task.Message>> taskQueue;
    public record GetFirstTask(ActorRef<Scheduler.Message> replyTo) implements Message {}
    public record RemoveFirstTask(ActorRef<Scheduler.Message> replyTo, ActorRef<Task.Message> removeTask) implements  Message {}
    public record AddTask(ActorRef<Task.Message> replyTo) implements  Message {}

    private Queue(ActorContext<Message> context) {
        super(context);
        this.taskQueue = new LinkedList<>();
    }

    public static Behavior<Message> create() {
        return Behaviors.setup(Queue::new);
    }

    @Override
    public Receive<Queue.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(AddTask.class, this::onAddTask)
                .onMessage(GetFirstTask.class, this::onGetFirstTask)
                .onMessage(RemoveFirstTask.class, this::onRemoveFirstTask)
                .build();
    }

    public Behavior<Message> onAddTask(AddTask msg){
        taskQueue.add(msg.replyTo);
        return this;
    }

    public Behavior<Message> onGetFirstTask(GetFirstTask msg){
        ActorRef<Task.Message> firstTaskInQueue = this.taskQueue.peek();
        if (firstTaskInQueue == null){
            msg.replyTo.tell(new Scheduler.NoElementInQueue(this.getContext().getSelf()));
            return Behaviors.stopped();
        }else{
            msg.replyTo.tell(new Scheduler.FirstTaskInQueue(this.getContext().getSelf(), firstTaskInQueue));
        }
        return this;
    }

    public Behavior<Message> onRemoveFirstTask(RemoveFirstTask msg){
        msg.replyTo.tell(new Scheduler.StartProgram());
        this.taskQueue.remove(msg.removeTask);
        return this;
    }
}
