package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.LinkedList;
/*
 * Klasse: Queue
 * Eine Warteschlange für alle Tasks. Direkt nach der Erstellung eines Task wird dieser Task
 * direkt in die Schlange hinzugefügt und nacheinander abarbeitet
 * */
public class Queue extends AbstractBehavior<Queue.Message> {
    public interface Message {}
    private final java.util.Queue<ActorRef<Task.Message>> taskQueue;

    /*
    * 3 Nachrichten mit entsprechenden Funktionalitäten
    * 1. Nachricht für die Funktion: den ersten Task in Queue abrufen
    * 2. Nachricht für die Funktion: den ersten Task in Queue löschen
    * 3. Nachricht für die Funktion: einen Task in Queue hinzufügen
    * */
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

    // 1. Funktion: den ersten Task in Queue abrufen
    public Behavior<Message> onAddTask(AddTask msg){
        taskQueue.add(msg.replyTo);
        return this;
    }

    // 2. Funktion: den ersten Task in Queue löschen, wenn Queue nicht leer ist
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

    // Funktion: einen Task in Queue hinzufügen
    public Behavior<Message> onRemoveFirstTask(RemoveFirstTask msg){
        msg.replyTo.tell(new Scheduler.StartProgram());
        this.taskQueue.remove(msg.removeTask);
        return this;
    }
}
