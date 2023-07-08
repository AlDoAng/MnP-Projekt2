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
/*
Alina Ignatova 226735,
Ha Phuong Ta 230655,
Valeriya Mikhalskaya 229099,
Janis Melon 209928
*/

/*
 * Klasse: Task
 * Es wird eine Anzahl an Workern am Scheduler beantragt
 */
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

    //eine Get-Funktion
    public ArrayList<Integer> getNumbers() {
        return numbers;
    }

    // Funktion: es wird eine Liste zufälligen Integern zwischen 1 und 6 (einschließlich) erstellt
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

    // Funktion: die Funktion wird aufgerufen, wenn alle Berechnungen abgeschlossen sind,
    // das Ergebnis wird ausgegeben und der Task beendet sich
    private Behavior<Message> onFinalResult(FinalResult msg) {
        this.result = msg.result;
        this.getContext().getLog().info("Task" + this.taskNumber + " result: " + result);
        this.scheduler.tell(new Scheduler.TaskEnde(this.getContext().getSelf(), this.numbers.size() + 1));
        return Behaviors.stopped();
    }

    // Funktion: diese Methode ist eine Zwischenmethode und dient als Verwalter für Aktionen
    private Behavior<Message> onCalcResult(CalcResult msg) {
        if (msg.action == 0){
            this.numbers.set(msg.index, msg.result);
        }
        if (msg.lastElement) {
            int sizeInW = this.numbers.size() + 1;
            ActorRef<Worker.Message> worker = this.getContext().spawn(Worker.create(), "Worker_" + this.taskNumber+"_" + sizeInW);
            worker.tell(new Worker.Calculate(this.getContext().getSelf(), 1, -1, 1, false, this.numbers));
        }
        return this;
    }

    // Funktion: die Methode wird aufgerufen, wenn der Task gestartet wird.
    // Wenn es freie Workers gibt, wird der Scheduler informiert und die Workers werden erstellt
    private Behavior<Message> onTryToStart(TryToStart msg){
        if (msg.numberOfFreeWorkers >= this.numbers.size() + 1){
            msg.replyTo.tell(new Scheduler.TaskIsStarted(this.getContext().getSelf(), this.numbers.size() + 1));
            int size = this.numbers.size();
            this.getContext().getLog().info("Task number " + taskNumber+" started calculation");
            for (int i = 0; i < size-1; i++) {
                ActorRef<Worker.Message> worker = this.getContext().spawn(Worker.create(), "Worker_" + this.taskNumber + i+1);
                worker.tell(new Worker.Calculate(this.getContext().getSelf(), this.numbers.get(i), i,0, false,this.numbers));
            }
            ActorRef<Worker.Message> worker = this.getContext().spawn(Worker.create(), "Worker_"+ this.taskNumber + size);
            worker.tell(new Worker.Calculate(this.getContext().getSelf(), this.numbers.get(size-1), size-1,0,  true,this.numbers));
        }else {
            //wenn Task nicht gestartet werden kann, wird nichts gemacht,
            //weil Task noch nicht aus der Queue entfernt wurde und liegt als erste da
            msg.replyTo.tell(new Scheduler.StartProgram());
        }
        return this;
    }

}
