package client.commands.available.commands;

import client.ReadManager;
import client.RequestManager;
import client.commands.Command;
import client.commands.CommandManager;
import common.network.CommandResult;
import common.network.Request;
import server.PersonCollection;

/**
 * help : print help for available commands
 */
public class Help extends Command {

    public Help(RequestManager requestManager) {
        super(requestManager);
    }
    @Override
    public void execute(String[] args) {
        if (args.length > 1) {
            System.out.println("Проблема с аргументом, обратитесь к команде help");
        } else {
            Request<String> request = new Request<>(getName(), null,null);
            CommandResult result = requestManager.sendRequest(request);
            if (result.status) {
                System.out.println((result.message));
            } else
                System.out.println("Ошибка");
        }
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "help: вывести справку по доступным командам";
    }
}
