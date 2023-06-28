package com.example;

import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;

import java.util.List;
import java.util.Random;

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
    public void createList() {
        Random random = new Random(System.currentTimeMillis());
        int randomLength = random.nextInt(10 - 4 +1) + 4;
        for (int i = 0; i<randomLength; i++){
            int randomElement = random.nextInt(6-1+1)+1;
            numbers.add(randomElement);
        }
    }

    @Override
    public Receive<Message> createReceive() {
        return null;
    }
}
