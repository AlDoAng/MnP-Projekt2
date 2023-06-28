package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.util.ClassLoaderObjectInputStream;

import java.util.List;

class Worker extends AbstractBehavior<Worker.Message> {

    public interface Message {};
    public record Calculate(ActorRef<Task.Message> replyTo, int number, int index, int action, boolean lastElement, List<Integer> list) implements Message {};
    private int number;
    private int action;
    private int index;
    private boolean lastElement;
    private List<Integer> listNumbers;
    private ActorRef<Task.Message> replyTo;

    private Worker(ActorContext<Message> context, ActorRef<Task.Message> actorRef, int number, int index, int action, boolean lastElement, List<Integer> list) {
        super(context);
        this.action = action;
        this.number = number;
        this.index = index;
        this.replyTo = actorRef;
        this.lastElement = lastElement;
        this.listNumbers = list;
        this.getContext().getSelf().tell(new Worker.Calculate(replyTo,number,index,action,lastElement,list));
    }

    public static Behavior<Message> create(ActorRef<Task.Message> actorRef,int number, int index,int action, boolean lastElement, List<Integer> list) {
        return Behaviors.setup(context -> new Worker(context, actorRef, number, index,action, lastElement, list));
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(Calculate.class, this::onCalculate)
                .build();
    }

    private  Behavior<Message> onCalculate(Calculate msg) {
        if (msg.action == 0) {
            int res = msg.number + 1;
            replyTo.tell(new Task.CalcResult(getContext().getSelf(), res, msg.index, msg.action, msg.lastElement));
        } else{
            int res = 1;
            for (int i = 0; i < msg.list.size(); i++) {
                res = res * msg.list.get(i);
            }
            replyTo.tell(new Task.FinalResult(this.getContext().getSelf(),res));
        }
        return Behaviors.stopped();
    }
}
