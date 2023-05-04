package client.commands.available.commands;

import client.RequestManager;
import client.commands.Command;
import common.data.Person;
import common.network.CommandResult;
import common.network.Request;
import server.PersonCollection;

import javax.xml.bind.JAXBException;

/**
 * print_unique_location : print the unique values of the location field of all items in the collection
 */
public class PrintUniqueLocation extends Command {

    public PrintUniqueLocation(RequestManager requestManager) {
        super(requestManager);
    }

    @Override
    public void execute(String[] args) throws JAXBException {
        if (args.length != 1) {
            System.out.println("Вы неправильно ввели команду");
        } else {
            Request<String> request = new Request<>(getName(), null, null);
            CommandResult result = requestManager.sendRequest(request);
            if (result.status) {
                System.out.println((result.message));
            } else
                System.out.println("Ошибка");
        }
    }
    @Override
    public String getName() {
        return "print_unique_location";
    }

    @Override
    public String getDescription() {
        return "print_unique_location: вывести уникальные значения поля location всех элементов в коллекции";
    }
}
