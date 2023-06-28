package com.example;

import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;

import java.util.List;

public class Task extends AbstractBehavior<Task.Message> {

    public interface Message {};

    private final List<Integer> numbers;

    private Task(ActorContext<Task.Message> context, List<Integer> numbers) {
        super(context);
        this.numbers = numbers;
    }

    public List<Integer> getNumbers() {
        return numbers;
    }

    @Override
    public Receive<Message> createReceive() {
        return null;
    }
}
