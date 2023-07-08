package com.example;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;

import java.io.IOException;

/*
* Klasse: AkkaStart
* Startpunkt für das Programm. Hier wird lediglich AkkaMainSystem erstellt
* */

public class AkkaStart {

  public static void main(String[] args) {
    final ActorSystem<AkkaMainSystem.Create> messageMain = ActorSystem.create(AkkaMainSystem.create(), "akkaMainSystem");

    messageMain.tell(new AkkaMainSystem.Create());

    try {
      System.out.println(">>> Press ENTER to exit <<<");
      System.in.read();
    } catch (IOException ignored) {
    } finally {
      messageMain.terminate();
    }
  }

}
