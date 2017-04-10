package com.elbloquecadena.console;

import com.elbloquecadena.util.ConsoleArguments;

public interface CCommands {

    public void execute(ConsoleArguments args);

    public String getHelpText();
    
    public String getShortText();

}
