package juego_coup;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ClientHandler implements Runnable {
   private final Socket clientSocket;
   private final CoupServer server;
   public final Player player;
   private final int playerNum;
   private ObjectOutputStream out;
   private ObjectInputStream in;
   public final BlockingQueue<Command> commandQueue = new LinkedBlockingQueue();
   private static final String ROLE_DUKE = "Duke";
   private static final String ROLE_ASSASSIN = "Assassin";
   private static final String ROLE_CAPTAIN = "Captain";
   private static final String ROLE_AMBASSADOR = "Ambassador";
   private static final String ROLE_CONTESSA = "Contessa";
   private static final long TIMEOUT_PHASE_MS = 10000L;

   public ClientHandler(Socket socket, CoupServer server, Player player) {
      this.clientSocket = socket;
      this.server = server;
      this.player = player;
      this.playerNum = player.getPlayerNum();
   }

   public void run() {
      try {
         this.out = new ObjectOutputStream(this.clientSocket.getOutputStream());
         this.in = new ObjectInputStream(this.clientSocket.getInputStream());
         this.send(new Command(4, this.player.getName() + " unido. Esperando a otros jugadores."));

         while(this.clientSocket.isConnected()) {
            Object obj = this.in.readObject();
            if (obj instanceof Command) {
               this.processClientCommand((Command)obj);
            }
         }
      } catch (IOException var12) {
         PrintStream var10000 = System.out;
         String var10001 = this.player.getName();
         var10000.println(var10001 + " se ha desconectado. " + var12.getMessage());
      } catch (ClassNotFoundException var13) {
         System.err.println("Error de deserialización en " + this.player.getName());
      } finally {
         try {
            if (this.in != null) {
               this.in.close();
            }

            if (this.out != null) {
               this.out.close();
            }

            if (this.clientSocket != null) {
               this.clientSocket.close();
            }
         } catch (IOException var11) {
         }

      }

   }

   private void processClientCommand(Command command) {
      switch (command.getType()) {
         case 20:
            this.handleAction(command);
            break;
         case 21:
         case 22:
         case 23:
            this.server.registerPhaseResponse(command);
            break;
         case 24:
         case 25:
         case 26:
            this.commandQueue.add(command);
            break;
         default:
            this.server.sendToPlayer(this.playerNum, new Command(1, "Comando no reconocido."));
      }

   }

   private void handleAction(Command actionCommand) {
      Game game = this.server.getGame();
      int action = actionCommand.getActionCode();
      int target = actionCommand.getTargetPlayerNum();
      if (this.playerNum != game.getCurrentPlayerIndex()) {
         this.send(new Command(1, "¡No es tu turno!"));
      } else if (this.player.isOut()) {
         this.send(new Command(1, "Estás fuera del juego."));
      } else if (this.player.mustCoup() && action != 103) {
         this.send(new Command(1, "Debes hacer Coup con 10 o más fichas."));
      } else {
         synchronized(game) {
            try {
               if (action != 101) {
                  if (action == 103) {
                     if (this.player.getTokens() < 7) {
                        this.send(new Command(1, "Necesitas 7 fichas para Coup."));
                        return;
                     }

                     game.checkValidTarget(this.playerNum, target);
                     game.payTokens(this.playerNum, 7);
                     CoupServer var11 = this.server;
                     String var10004 = this.player.getName();
                     var11.broadcast(new Command(1, var10004 + " hace Golpe de Estado contra " + game.getPlayers()[target].getName()));
                     (new Thread(() -> {
                        this.promptInfluenceLoss(target, "por Coup", true);
                     })).start();
                     return;
                  }

                  (new Thread(() -> {
                     this.startChallengePhase(actionCommand);
                  })).start();
                  return;
               }

               game.receiveTokens(this.playerNum, 1);
               this.server.broadcast(new Command(1, this.player.getName() + " toma 1 ficha (Income)."));
               game.advanceTurn();
               this.server.broadcastGameState();
               ((ClientHandler)this.server.getHandlers().get(game.getCurrentPlayerIndex())).notifyTurnStart();
            } catch (IllegalArgumentException var8) {
               this.send(new Command(1, "Error de acción: " + var8.getMessage()));
               return;
            } catch (Exception var9) {
               PrintStream var10000 = System.err;
               String var10001 = this.player.getName();
               var10000.println("Error procesando acción de " + var10001 + ": " + var9.getMessage());
               return;
            }

         }
      }
   }

   private void startChallengePhase(Command actionCommand) {
      this.server.setCurrentAction(actionCommand);
      Iterator var2 = this.server.getHandlers().iterator();

      while(var2.hasNext()) {
         ClientHandler handler = (ClientHandler)var2.next();
         if (handler.playerNum != this.playerNum && !handler.player.isOut()) {
            String var10004 = ActionsPrinter.actionToString(actionCommand.getActionCode());
            handler.send(new Command(11, var10004 + " de " + this.player.getName() + "? (1=Desafiar, 0=Pasar)"));
         }
      }

      List<Command> challengeResponses = this.waitForPhaseResponses(10000L);
      Command challengeResult = this.resolvePhase(challengeResponses, 21, 23);
      this.server.clearCurrentAction();
      if (challengeResult != null) {
         this.resolveChallenge(actionCommand, challengeResult);
      } else {
         int action = actionCommand.getActionCode();
         if (action != 102 && action != 106 && action != 105) {
            this.executeAction(actionCommand);
         } else {
            this.startBlockPhase(actionCommand);
         }

      }
   }

   private void resolveChallenge(Command originalAction, Command challengeResponse) {
      Game game = this.server.getGame();
      int actingPlayerNum = originalAction.getSenderPlayerNum();
      int challengerPlayerNum = challengeResponse.getSenderPlayerNum();
      String requiredRole = this.getRequiredRole(originalAction.getActionCode());
      Player actingPlayer = game.getPlayers()[actingPlayerNum];
      Player challenger = game.getPlayers()[challengerPlayerNum];
      this.server.broadcast(new Command(1, String.format("¡Desafío! %s debe probar que tiene la carta %s.", actingPlayer.getName(), requiredRole)));
      synchronized(game) {
         int influenceIndex = actingPlayer.checkInfluence(requiredRole);
         if (influenceIndex > 0) {
            this.server.broadcast(new Command(1, String.format("¡Prueba exitosa! %s tenía el %s. Desafiante %s pierde una influencia.", actingPlayer.getName(), requiredRole, challenger.getName())));
            game.shuffleAndReplaceInfluence(actingPlayerNum, influenceIndex);
            (new Thread(() -> {
               this.promptInfluenceLoss(challengerPlayerNum, "por perder el desafío", true);
            })).start();
            (new Thread(() -> {
               this.executeAction(originalAction);
            })).start();
         } else {
            this.server.broadcast(new Command(1, String.format("¡Desafío exitoso! %s NO tenía el %s. %s pierde una influencia.", actingPlayer.getName(), requiredRole, actingPlayer.getName())));
            (new Thread(() -> {
               this.promptInfluenceLoss(actingPlayerNum, "por mentir y perder el desafío", true);
            })).start();
         }

      }
   }

   private void startBlockPhase(Command actionCommand) {
      this.server.setCurrentAction(actionCommand);
      Iterator var2 = this.server.getHandlers().iterator();

      while(var2.hasNext()) {
         ClientHandler handler = (ClientHandler)var2.next();
         if (!handler.player.isOut()) {
            String var10004 = ActionsPrinter.actionToString(actionCommand.getActionCode());
            handler.send(new Command(12, "¿Bloquear " + var10004 + " de " + this.player.getName() + "? (1=Bloquear, 0=Pasar)"));
         }
      }

      List<Command> blockResponses = this.waitForPhaseResponses(10000L);
      Command blockResult = this.resolvePhase(blockResponses, 22, 23);
      this.server.clearCurrentAction();
      if (blockResult != null) {
         this.resolveBlock(actionCommand, blockResult);
      } else {
         this.executeAction(actionCommand);
      }
   }

   private void resolveBlock(Command originalAction, Command blockResponse) {
      Game game = this.server.getGame();
      int actingPlayerNum = originalAction.getSenderPlayerNum();
      int blockerPlayerNum = blockResponse.getSenderPlayerNum();
      String blockRole = this.getBlockRole(originalAction.getActionCode());
      Player actingPlayer = game.getPlayers()[actingPlayerNum];
      Player blocker = game.getPlayers()[blockerPlayerNum];
      this.server.broadcast(new Command(1, String.format("%s intenta bloquear con un %s. ¿%s desafía el bloqueo?", blocker.getName(), blockRole, actingPlayer.getName())));
      ClientHandler actingHandler = (ClientHandler)this.server.getHandlers().get(actingPlayerNum);
      actingHandler.send(new Command(13, String.format("¿Desafías el bloqueo de %s con %s? (1=Desafiar, 0=Aceptar)", blocker.getName(), blockRole)));

      Command challengeBlockChoice;
      try {
         challengeBlockChoice = (Command)actingHandler.commandQueue.poll(10000L, TimeUnit.MILLISECONDS);
      } catch (InterruptedException var15) {
         Thread.currentThread().interrupt();
         challengeBlockChoice = null;
      }

      synchronized(game) {
         boolean challengeBlock = challengeBlockChoice != null && challengeBlockChoice.getType() == 25;
         if (challengeBlock) {
            this.server.broadcast(new Command(1, String.format("¡%s DESAFÍA el bloqueo de %s!", actingPlayer.getName(), blocker.getName())));
            int influenceIndex = blocker.checkBlockInfluence(blockRole);
            if (influenceIndex > 0) {
               this.server.broadcast(new Command(1, String.format("¡Bloqueo validado! %s tenía el %s. Actuante %s pierde una influencia.", blocker.getName(), blockRole, actingPlayer.getName())));
               game.shuffleAndReplaceInfluence(blockerPlayerNum, influenceIndex);
               (new Thread(() -> {
                  this.promptInfluenceLoss(actingPlayerNum, "por perder el desafío al bloqueo", true);
               })).start();
            } else {
               this.server.broadcast(new Command(1, String.format("¡Bloqueo falso! %s NO tenía el %s. Bloqueador %s pierde una influencia.", blocker.getName(), blockRole, blocker.getName())));
               (new Thread(() -> {
                  this.promptInfluenceLoss(blockerPlayerNum, "por mentir en el bloqueo", false);
               })).start();
               (new Thread(() -> {
                  this.executeAction(originalAction);
               })).start();
            }
         } else {
            this.server.broadcast(new Command(1, "Bloqueo aceptado. La acción original se detiene."));
            game.advanceTurn();
            this.server.broadcastGameState();
            ((ClientHandler)this.server.getHandlers().get(game.getCurrentPlayerIndex())).notifyTurnStart();
         }

      }
   }

   private void promptInfluenceLoss(int targetPlayerNum, String reason, boolean advanceTurnAfter) {
      ClientHandler targetHandler = (ClientHandler)this.server.getHandlers().get(targetPlayerNum);
      Game game = this.server.getGame();
      if (game.getPlayers()[targetPlayerNum].getNumInfluence() <= 0) {
         System.err.println("Error: El jugador ya no tiene influencias activas.");
      } else {
         CoupServer var10000 = this.server;
         String var10004 = game.getPlayers()[targetPlayerNum].getName();
         var10000.broadcast(new Command(1, var10004 + " debe perder una influencia " + reason + "."));

         try {
            targetHandler.send(new Command(15, "Pierde influencia: " + reason));
            Command choice = (Command)targetHandler.commandQueue.poll(60L, TimeUnit.SECONDS);
            int choiceIndex = 1;
            if (choice != null && choice.getType() == 26) {
               choiceIndex = choice.getChoiceIndex();
            } else {
               this.server.broadcast(new Command(1, targetHandler.player.getName() + " no respondió, pierde aleatoriamente."));
            }

            synchronized(game) {
               game.loseInfluence(targetPlayerNum, choiceIndex);
               var10000 = this.server;
               var10004 = game.getPlayers()[targetPlayerNum].getName();
               var10000.broadcast(new Command(1, var10004 + " perdió influencia."));
               if (game.checkWin()) {
                  this.server.broadcast(new Command(3, "Fin del juego. Ganador: " + game.getWinner().getName()));
                  return;
               }
            }
         } catch (InterruptedException var13) {
            Thread.currentThread().interrupt();
         }

         if (advanceTurnAfter) {
            synchronized(game) {
               game.advanceTurn();
               this.server.broadcastGameState();
               ((ClientHandler)this.server.getHandlers().get(game.getCurrentPlayerIndex())).notifyTurnStart();
            }
         }

      }
   }

   private void executeAction(Command actionCommand) {
      Game game = this.server.getGame();
      int action = actionCommand.getActionCode();
      int target = actionCommand.getTargetPlayerNum();
      synchronized(game) {
         CoupServer var10000;
         String var10004;
         switch (action) {
            case 102:
               game.receiveTokens(this.playerNum, 2);
               this.server.broadcast(new Command(1, this.player.getName() + " toma 2 fichas (Ayuda Extranjera)."));
            case 103:
            default:
               break;
            case 104:
               game.receiveTokens(this.playerNum, 3);
               this.server.broadcast(new Command(1, this.player.getName() + " toma 3 fichas (Impuestos)."));
               break;
            case 105:
               game.payTokens(this.playerNum, 3);
               var10000 = this.server;
               var10004 = this.player.getName();
               var10000.broadcast(new Command(1, var10004 + " asesina a " + game.getPlayers()[target].getName() + "."));
               (new Thread(() -> {
                  this.promptInfluenceLoss(target, "por Asesinato", true);
               })).start();
               return;
            case 106:
               game.stealTokens(this.playerNum, target, 2);
               var10000 = this.server;
               var10004 = this.player.getName();
               var10000.broadcast(new Command(1, var10004 + " roba 2 fichas de " + game.getPlayers()[target].getName() + "."));
               break;
            case 107:
               (new Thread(() -> {
                  this.handleAmbassadorExchange(this.playerNum);
               })).start();
               return;
         }

         game.advanceTurn();
         this.server.broadcastGameState();
         ((ClientHandler)this.server.getHandlers().get(game.getCurrentPlayerIndex())).notifyTurnStart();
      }
   }

   private void handleAmbassadorExchange(int playerNum) {
      ClientHandler handler = (ClientHandler)this.server.getHandlers().get(playerNum);
      Game game = this.server.getGame();

      try {
         List chosenIndices;
         synchronized(game) {
            chosenIndices = game.startExchange(playerNum);
            this.server.broadcast(new Command(1, handler.player.getName() + " ha robado 2 cartas extra."));
            Command prompt = new Command(14, "Elige las 2 cartas a quedarte (0-3):");
            Influence[] poolArray = (Influence[])chosenIndices.toArray(new Influence[0]);
            prompt.setCardPool(poolArray);
            handler.send(prompt);
         }

         Command choiceCommand = (Command)handler.commandQueue.poll(60L, TimeUnit.SECONDS);
         if (choiceCommand == null || choiceCommand.getType() != 24) {
            this.server.broadcast(new Command(1, handler.player.getName() + " no respondió a tiempo. Pierde las cartas extra."));
            return;
         }

         chosenIndices = choiceCommand.getChosenIndices();
         List<Influence> keptInfluences = new ArrayList();
         List<Influence> returnedInfluences = new ArrayList();
         List pool;
         synchronized(game) {
            pool = game.startExchange(playerNum);
         }

         for(int i = 0; i < pool.size(); ++i) {
            if (chosenIndices.contains(i)) {
               keptInfluences.add((Influence)pool.get(i));
            } else {
               returnedInfluences.add((Influence)pool.get(i));
            }
         }

         synchronized(game) {
            game.finishExchange(playerNum, keptInfluences, returnedInfluences);
            this.server.broadcast(new Command(1, handler.player.getName() + " ha completado el Intercambio."));
            game.advanceTurn();
            this.server.broadcastGameState();
            ((ClientHandler)this.server.getHandlers().get(game.getCurrentPlayerIndex())).notifyTurnStart();
         }
      } catch (InterruptedException var15) {
         Thread.currentThread().interrupt();
      }

   }

   private String getRequiredRole(int actionCode) {
      switch (actionCode) {
         case 104:
            return "Duke";
         case 105:
            return "Assassin";
         case 106:
            return "Captain";
         case 107:
            return "Ambassador";
         default:
            return null;
      }
   }

   private String getBlockRole(int actionCode) {
      switch (actionCode) {
         case 102:
            return "Duke";
         case 103:
         case 104:
         default:
            return null;
         case 105:
            return "Contessa";
         case 106:
            return "Captain o Ambassador";
      }
   }

   private List<Command> waitForPhaseResponses(long timeoutMs) {
      synchronized(this.server.getPhaseLock()) {
         List var5;
         try {
            long startTime = System.currentTimeMillis();
            long endTime = startTime + timeoutMs;

            for(long waitTime = timeoutMs; waitTime > 0L && this.server.getPhaseResponses().isEmpty(); waitTime = endTime - System.currentTimeMillis()) {
               this.server.getPhaseLock().wait(waitTime);
            }

            List var10 = this.server.getPhaseResponses();
            return var10;
         } catch (InterruptedException var16) {
            Thread.currentThread().interrupt();
            var5 = this.server.getPhaseResponses();
         } finally {
            this.server.clearPhaseResponses();
         }

         return var5;
      }
   }

   private Command resolvePhase(List<Command> responses, int successType, int passType) {
      Iterator var4 = responses.iterator();

      Command response;
      do {
         if (!var4.hasNext()) {
            return null;
         }

         response = (Command)var4.next();
      } while(response.getType() != successType);

      return response;
   }

   public void notifyTurnStart() {
      this.send(new Command(10, "¡Es tu turno! Elige una acción."));
   }

   public void send(Command command) {
      try {
         synchronized(this.out) {
            this.out.writeObject(command);
            this.out.flush();
         }
      } catch (IOException var5) {
         PrintStream var10000 = System.err;
         String var10001 = this.player.getName();
         var10000.println("Error enviando comando a " + var10001 + ": " + var5.getMessage());
      }

   }

   public void sendGameState(Game currentGameState) {
      try {
         synchronized(this.out) {
            Command stateCommand = new Command(2, "Actualización del estado del juego.");
            this.out.writeObject(stateCommand);
            this.out.flush();
            this.out.writeObject(currentGameState);
            this.out.flush();
         }
      } catch (IOException var6) {
         PrintStream var10000 = System.err;
         String var10001 = this.player.getName();
         var10000.println("Error enviando estado de juego a " + var10001 + ": " + var6.getMessage());
      }

   }

   public Player getPlayer() {
      return this.player;
   }

   public int getPlayerNum() {
      return this.playerNum;
   }
}
