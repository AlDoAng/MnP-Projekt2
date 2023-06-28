package com.example;

import akka.actor.Actor;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import scala.jdk.FunctionWrappers;

import java.util.List;
import java.util.Random;

public class Task extends AbstractBehavior<Task.Message> {

    public interface Message {};

    public record NumberWorkers(ActorRef<Scheduler.Message> replyTo, int numberFreeWorkers) implements Message {}
    public record CalcResult(ActorRef<Worker.Message> worker, int result, int index, int action, boolean lastElement) implements  Message{};
    public record FinalResult(ActorRef<Worker.Message> worker, int result) implements Message {};
    private final List<Integer> numbers;
    private final ActorRef<Scheduler.Message> scheduler;
    private final ActorRef<Queue.Message> queueActorRef;
    private int result;

    public static Behavior<Message> create(ActorRef<Scheduler.Message> schedulerActorRef, ActorRef<Queue.Message> queue){
        return Behaviors.setup(context -> new Task(context, schedulerActorRef, queue));
    }

    private Task(ActorContext<Message> context, ActorRef<Scheduler.Message> scheduler, ActorRef<Queue.Message> queue) {
        super(context);
        this.result = 0;
        this.numbers = createList();
        this.scheduler = scheduler;
        this.queueActorRef = queue;
    }

    public List<Integer> getNumbers() {
        return numbers;
    }
    public List<Integer> createList() {
        Random random = new Random(System.currentTimeMillis());
        int randomLength = random.nextInt(10 - 4 +1) + 4;
        for (int i = 0; i<randomLength; i++){
            int randomElement = random.nextInt(6-1+1)+1;
            numbers.add(randomElement);
        }
        return numbers;
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(NumberWorkers.class, this::onNumberWorkers)
                .onMessage(CalcResult.class, this::onCalcResult)
                .onMessage(FinalResult.class, this::onFinalResult)
                .build();
    }

    private Behavior<Message> onFinalResult(FinalResult msg) {
        this.result = msg.result;
        this.getContext().getLog().info("Final result: " + result);
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

    private Behavior<Message> onNumberWorkers(NumberWorkers msg) {
        if (msg.numberFreeWorkers < this.numbers.size() + 1){
            this.queueActorRef.tell(new Queue.AddTask(this.getContext().getSelf()));
        }else {
            int size = this.numbers.size();
            for (int i = 0; i < size-1; i++) {
                this.getContext().spawn(Worker.create(this.getContext().getSelf(), this.numbers.get(i), i,0, false,this.numbers), "Worker " + i + 1);
            }
            this.getContext().spawn(Worker.create(this.getContext().getSelf(), this.numbers.get(size-1), size-1,0,  true,this.numbers), "Worker " + size );
        }
        return this;
    }


}
