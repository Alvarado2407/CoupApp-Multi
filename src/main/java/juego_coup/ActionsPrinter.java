package juego_coup;

public class ActionsPrinter {
   public static final int ACTION_INCOME = 1;
   public static final int ACTION_FOREIGN_AID = 2;
   public static final int ACTION_COUP = 3;
   public static final int ACTION_TAX = 4;
   public static final int ACTION_ASSASSINATE = 5;
   public static final int ACTION_STEAL = 6;
   public static final int ACTION_EXCHANGE = 7;
   public static final int ACTION_VIEW_INFLUENCES = 8;
   public static final int ACTION_VIEW_RULES = 9;
   public static final int NUM_ACTIONS = 9;

   public ActionsPrinter() {
   }

   public static String welcomeMessage() {
      return "¡Bienvenido a Coup! Puedes leer las reglas aquí: https://www.qugs.org/rules/r131357.pdf.\nEstás conectado al servidor. Por favor, espera a que el juego comience.";
   }

   public static String instructions() {
      return "Puedes leer las reglas aquí: https://www.qugs.org/rules/r131357.pdf.\nUtiliza tu teclado para ingresar el número correspondiente a la acción elegida.";
   }

   public static String getActions() {
      String actions = "1. Ingreso (Income)\n2. Ayuda Extranjera (Foreign Aid)\n3. Golpe de Estado (Coup) (necesitas 7 fichas)\n4. Impuestos (Tax) [DUQUE]\n5. Asesinato (Assassinate) [ASESINO] (necesitas 3 fichas)\n6. Robar (Steal) [CAPITAN]\n7. Intercambio (Exchange) [EMBAJADOR]\n8. Ver tus influencias\n9. Ver instrucciones y reglas";
      return actions;
   }

   public static String promptForAction() {
      return "\n--- ES TU TURNO ---\n" + getActions() + "\nSelecciona el número de la acción (1-9): ";
   }

   public static String actionToString(int actionCode) {
      switch (actionCode) {
         case 1:
            return "Ingreso (Income)";
         case 2:
            return "Ayuda Extranjera (Foreign Aid)";
         case 3:
            return "Golpe de Estado (Coup)";
         case 4:
            return "Impuestos (Tax) [DUQUE]";
         case 5:
            return "Asesinar (Assassinate) [ASESINO]";
         case 6:
            return "Robar (Steal) [CAPITAN]";
         case 7:
            return "Intercambio (Exchange) [EMBAJADOR]";
         default:
            return "Acción Desconocida (" + actionCode + ")";
      }
   }
}
