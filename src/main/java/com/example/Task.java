package com.example;

import akka.actor.Actor;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.ArrayList;
import java.util.Random;

public class Task extends AbstractBehavior<Task.Message> {

    public interface Message {}

    public record CalcResult(ActorRef<Worker.Message> worker, int result, int index, int action, boolean lastElement) implements  Message{}
    public record FinalResult(ActorRef<Worker.Message> worker, int result) implements Message {}
    public record TryToStart(ActorRef<Scheduler.Message> replyTo, int numberOfFreeWorkers) implements Message {}
    private ArrayList<Integer> numbers;
    private final ActorRef<Scheduler.Message> scheduler;
    private final ActorRef<Queue.Message> queueActorRef;
    private int result;
    private final int taskNumber;

    public static Behavior<Message> create(ActorRef<Scheduler.Message> schedulerActorRef, ActorRef<Queue.Message> queue, int taskNumber){
        return Behaviors.setup(context -> new Task(context, schedulerActorRef, queue, taskNumber));
    }

    private Task(ActorContext<Message> context, ActorRef<Scheduler.Message> scheduler, ActorRef<Queue.Message> queue, int taskNumber) {
        super(context);
        this.result = 0;
        this.numbers = createList();
        this.scheduler = scheduler;
        this.queueActorRef = queue;
        this.taskNumber = taskNumber;
    }

    public ArrayList<Integer> getNumbers() {
        return numbers;
    }
    public ArrayList<Integer> createList() {
        Random random = new Random(System.currentTimeMillis());
        int randomLength = random.nextInt(10 - 4 +1) + 4;
        this.numbers = new ArrayList<>();
        for (int i = 0; i<randomLength; i++){
            int randomElement = random.nextInt(6-1+1)+1;
            numbers.add(randomElement);
        }
        return numbers;
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(CalcResult.class, this::onCalcResult)
                .onMessage(FinalResult.class, this::onFinalResult)
                .onMessage(TryToStart.class, this::onTryToStart)
                .build();
    }

    private Behavior<Message> onFinalResult(FinalResult msg) {
        this.result = msg.result;
        this.getContext().getLog().info("Final result: " + result);
        this.scheduler.tell(new Scheduler.TaskEnde(this.getContext().getSelf(), this.numbers.size() + 1));
        return this;
    }

    private Behavior<Message> onCalcResult(CalcResult msg) {
        if (msg.action == 0){
            this.numbers.set(msg.index, msg.result);
        }
        if (msg.lastElement){
            msg.worker.tell(new Worker.Calculate(this.getContext().getSelf(), 1,-1,1,    false, this.numbers));
        }
        return this;
    }

    private Behavior<Message> onTryToStart(TryToStart msg){
        if (msg.numberOfFreeWorkers >= this.numbers.size() + 1){
            msg.replyTo.tell(new Scheduler.TaskIsStarted(this.getContext().getSelf(), this.numbers.size() + 1));
            int size = this.numbers.size();
            this.getContext().getLog().info("Task number " + taskNumber+" started calculation");
            for (int i = 0; i < size-1; i++) {
                this.getContext().spawn(Worker.create(this.getContext().getSelf(), this.numbers.get(i), i,0, false,this.numbers), "Worker_" + (i + 1));
            }
            this.getContext().spawn(Worker.create(this.getContext().getSelf(), this.numbers.get(size-1), size-1,0,  true,this.numbers), "Worker_" + size );
        }else {
            //wenn Task nicht gestartet werden kann, wird nichts gemacht,
            //weil Task noch nicht aus der Queue entfernt wurde und liegt als erste da
        }
        return this;
    }

}
