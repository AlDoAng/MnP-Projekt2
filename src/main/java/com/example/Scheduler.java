package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Scheduler extends AbstractBehavior<Scheduler.Message>{

    public interface Message {}
    public record TaskEnde(ActorRef<Task.Message> replyTo, int numberWorkersFree) implements Message{}
    public record StartProgram() implements Message{}
    public record NoElementInQueue(ActorRef<Queue.Message> msgFrom) implements Message {}
    public record FirstTaskInQueue(ActorRef<Queue.Message> msgFrom, ActorRef<Task.Message> firstTaskInQueue) implements Message {}
    public record TaskIsStarted(ActorRef<Task.Message> repplyTo, int numberWorkers) implements Message {}
    //enthält Tasks, wenn es mehr als 20 Workers laufen

    //es dürfen maximal 20 Workers laufen
    private final int maxWorkers = 20;
    private int freeWorkers = 20;
    private final ActorRef<Queue.Message> queueActorRef;
    private int numberCalculatedWorkers = 0;


    private Scheduler(ActorContext<Message> context, ActorRef<Queue.Message> queue) {
        super(context);
        queueActorRef = queue;
    }

    public static Behavior<Message> create(ActorRef<Queue.Message> queueActorRef) {

        return Behaviors.setup(context -> new Scheduler(context, queueActorRef));
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(TaskEnde.class, this::onTaskEnde)
                .onMessage(StartProgram.class, this::onStartProgram)
                .onMessage(NoElementInQueue.class, this::onNoElementInQueue)
                .onMessage(FirstTaskInQueue.class, this::onFirstTaskInQueue)
                .onMessage(TaskIsStarted.class, this::onTaskIsStarted)
                .build();
    }

    private Behavior<Message> onTaskEnde(TaskEnde msg) {
        this.numberCalculatedWorkers += 1;
        this.freeWorkers += msg.numberWorkersFree;
        if (numberCalculatedWorkers == 20){
            this.getContext().getLog().info("All tasks were calculated. Press ENTER to exit");
            //return Behaviors.stopped();
        }
        return this;
    }


    private  Behavior<Message> onStartProgram(StartProgram msg){
        this.queueActorRef.tell(new Queue.GetFirstTask(this.getContext().getSelf()));

        return this;
    }

    private  Behavior<Message> onNoElementInQueue(NoElementInQueue msg){
        //TODO: stop the program cause no elements left
        return this;

    }

    private  Behavior<Message> onFirstTaskInQueue(FirstTaskInQueue msg){
        msg.firstTaskInQueue.tell(new Task.TryToStart(this.getContext().getSelf(), freeWorkers));
        return this;
    }

    private Behavior<Message> onTaskIsStarted(TaskIsStarted msg){
        this.freeWorkers = freeWorkers - msg.numberWorkers;
        this.queueActorRef.tell(new Queue.RemoveFirstTask(this.getContext().getSelf(), msg.repplyTo));
//        if (this.freeWorkers > 0)
//            this.getContext().getSelf().tell(new Scheduler.StartProgram());
        return this;
    }
}
