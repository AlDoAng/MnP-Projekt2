package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

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
        ActorRef<Queue.Message> queue = this.getContext().spawn(Queue.create(), "queue");
        ActorRef<Scheduler.Message> scheduler = this.getContext().spawn(Scheduler.create(queue ), "scheduler");

        for (int i = 0; i < 5; i++) {
            ActorRef<Task.Message> task = this.getContext().spawn(Task.create(scheduler, queue, i+1), "task_" + (i+1));
            queue.tell(new Queue.AddTask(task));
        }


        scheduler.tell(new Scheduler.StartProgram());

        //#create-actors
        return this;
    }
}
