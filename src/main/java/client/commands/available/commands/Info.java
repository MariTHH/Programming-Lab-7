package client.commands.available.commands;

import client.RequestManager;
import client.commands.Command;
import common.network.CommandResult;
import common.network.Request;
import server.PersonCollection;

import javax.xml.bind.JAXBException;

/**
 * info :
 * output to the standard output stream information about the collection
 * (type, initialization date, number of items, etc.)
 */
public class Info extends Command {

    public Info(RequestManager requestManager) {
        super(requestManager);
    }

    @Override
    public void execute(String[] args) throws JAXBException {
        if (args.length > 1) {
            System.out.println("Вы неправильно ввели команду");
        } else {
            PersonCollection personCollection = new PersonCollection();
            //personCollection.loadCollection();
            Request<String> request = new Request<>(getName(), null,null);
            CommandResult result = requestManager.sendRequest(request);
            if (result.status) {
                System.out.println((result.message));
            }
        }
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDescription() {
        return "info: вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)";
    }
}
