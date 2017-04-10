package com.elbloquecadena;

import java.util.HashMap;
import java.util.Map;

import com.elbloquecadena.console.CCommands;
import com.elbloquecadena.console.Init;
import com.elbloquecadena.util.ConsoleArguments;

public class CommandLineInterface {

    private static final Map<String, CCommands> map = new HashMap<>();

    static {
        map.put("-init", new Init());
        map.put("-help", new Help());
    }

    public static void main(String[] args) {
        ConsoleArguments console = new ConsoleArguments(args);
        if (args.length == 0)
            help();
        else {
            CCommands cmd = map.get(args[0]);
            if (cmd != null)
                cmd.execute(console);
            else
                help();
        }
    }

    private static void help() {
        map.get("-help").execute(new ConsoleArguments(null));
    }

    static class Help implements CCommands {

        @Override
        public void execute(ConsoleArguments args) {

            String cm = args.getString("-help", null);
            if (cm != null) {
                CCommands cmd = map.get("-" + cm);
                if (cmd != null)
                    System.out.println(cmd.getHelpText());
                else
                    System.out.println(getHelpText());
            } else {
                System.out.println("Hello to ElBloqueCadena\nUse the following commands:");
                map.forEach((key, command) -> {
                    System.out.println(key + " " + command.getShortText());
                });
            }

        }

        @Override
        public String getHelpText() {

            return "Prints helpful information about commands.\n" //
                    + "Usage:" //
                    + " -help {subcommand}";

        }

        @Override
        public String getShortText() {
            return " to get more help with commands";
        }

    }

}
