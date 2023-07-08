package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
/*
 * Klasse: AkkaMainSystem
 * Initialisierung der nötigen Aktoren: 1 Queue, 1 Scheduler und 20 Tasks
 * */
public class AkkaMainSystem extends AbstractBehavior<AkkaMainSystem.Create> {

    public static class Create {
    }

    public static Behavior<Create> create() {
        return Behaviors.setup(AkkaMainSystem::new);
    }

    private AkkaMainSystem(ActorContext<Create> context) {
        super(context);
    }

    @Override
    public Receive<Create> createReceive() {
        return newReceiveBuilder().onMessage(Create.class, this::onCreate).build();
    }

    private Behavior<Create> onCreate(Create command) {
        //#create-actors
        //Eine Queue und ein Scheduler werden hier erstellt
        ActorRef<Queue.Message> queue = this.getContext().spawn(Queue.create(), "queue");
        ActorRef<Scheduler.Message> scheduler = this.getContext().spawn(Scheduler.create(queue ), "scheduler");

        //For-loop erstellt 20 Tasks
        for (int i = 0; i < 20; i++) {
            ActorRef<Task.Message> task = this.getContext().spawn(Task.create(scheduler, queue, i+1), "task_" + (i+1));
            queue.tell(new Queue.AddTask(task));
        }

        // Scheduler schickt an sich eine "Start message" Nachricht
        scheduler.tell(new Scheduler.StartProgram());

        //#create-actors
        return this;
    }
}
