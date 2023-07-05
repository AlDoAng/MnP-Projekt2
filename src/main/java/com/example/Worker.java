package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.List;

class Worker extends AbstractBehavior<Worker.Message> {

    public interface Message {}
    public record Calculate(ActorRef<Task.Message> replyTo, int number, int index, int action, boolean lastElement, List<Integer> list) implements Message {}


    private Worker(ActorContext<Message> context) {
        super(context);
    }

    public static Behavior<Message> create() {
        return Behaviors.setup(Worker::new);
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
            msg.replyTo.tell(new Task.CalcResult(getContext().getSelf(), res, msg.index, msg.action, msg.lastElement));
        } else{
            int res = 1;
            for (int i = 0; i < msg.list.size(); i++) {
                res = res * msg.list.get(i);
            }
            msg.replyTo.tell(new Task.FinalResult(this.getContext().getSelf(),res));
        }
        return Behaviors.stopped();
    }
}
